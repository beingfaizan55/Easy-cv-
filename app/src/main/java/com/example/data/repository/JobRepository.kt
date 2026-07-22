package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Job
import com.example.data.model.JobApplication
import com.example.data.model.JobSeedData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class JobRepository(private val context: Context) {
    private val TAG = "JobRepository"
    private val PREFS_NAME = "easy_cv_job_prefs"
    private val JOBS_KEY = "easy_cv_jobs"
    private val APPLICATIONS_KEY = "easy_cv_applications"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jobsListAdapter = moshi.adapter<List<Job>>(
        Types.newParameterizedType(List::class.java, Job::class.java)
    )
    private val appListAdapter = moshi.adapter<List<JobApplication>>(
        Types.newParameterizedType(List::class.java, JobApplication::class.java)
    )

    private val sharedPrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val firestore by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                Log.w(TAG, "Firebase Firestore initialization failed, using local mode only.")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Firestore initialization failed, using local mode only.")
            null
        }
    }

    // In-memory caching/fallback
    private var localJobs: MutableList<Job> = mutableListOf()
    private var localApplications: MutableList<JobApplication> = mutableListOf()

    init {
        loadLocalData()
        if (localJobs.isEmpty()) {
            localJobs.addAll(JobSeedData.sampleJobs)
            saveLocalData()
        }
    }

    private fun loadLocalData() {
        val jobsJson = sharedPrefs.getString(JOBS_KEY, null)
        if (jobsJson != null) {
            try {
                jobsListAdapter.fromJson(jobsJson)?.let {
                    localJobs = it.toMutableList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached jobs", e)
            }
        }
        val appsJson = sharedPrefs.getString(APPLICATIONS_KEY, null)
        if (appsJson != null) {
            try {
                appListAdapter.fromJson(appsJson)?.let {
                    localApplications = it.toMutableList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached applications", e)
            }
        }
    }

    private fun saveLocalData() {
        sharedPrefs.edit()
            .putString(JOBS_KEY, jobsListAdapter.toJson(localJobs))
            .putString(APPLICATIONS_KEY, appListAdapter.toJson(localApplications))
            .apply()
    }

    /**
     * Fetch all jobs from Firestore.
     * Automatically syncs and inserts seed data to Firestore on first launch if Firestore is empty!
     */
    suspend fun getJobs(): List<Job> = withContext(Dispatchers.IO) {
        val fs = firestore
        if (fs == null) {
            Log.d(TAG, "Firestore offline, returning local cache.")
            return@withContext localJobs.sortedByDescending { it.createdAt }
        }

        try {
            val snapshot = fs.collection("jobs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                Log.d(TAG, "Firestore jobs collection is empty. Seeding Firestore with sample jobs...")
                // Seed Firestore
                for (job in JobSeedData.sampleJobs) {
                    fs.collection("jobs").document(job.id).set(job).await()
                }
                localJobs = JobSeedData.sampleJobs.toMutableList()
                saveLocalData()
                return@withContext localJobs
            } else {
                val jobsList = mutableListOf<Job>()
                for (doc in snapshot.documents) {
                    val job = doc.toObject(Job::class.java)
                    if (job != null) {
                        jobsList.add(job.copy(id = doc.id))
                    }
                }
                localJobs = jobsList
                saveLocalData()
                return@withContext jobsList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching jobs from Firestore", e)
            return@withContext localJobs.sortedByDescending { it.createdAt }
        }
    }

    /**
     * Posts a new job.
     */
    suspend fun postJob(job: Job): Boolean = withContext(Dispatchers.IO) {
        // Add to local cache immediately
        localJobs.add(0, job)
        saveLocalData()

        val fs = firestore ?: return@withContext true
        try {
            fs.collection("jobs").document(job.id).set(job).await()
            Log.d(TAG, "Successfully posted job to Firestore: ${job.title}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving posted job to Firestore", e)
            return@withContext true // Still succeeded locally
        }
    }

    /**
     * Submits a job application.
     */
    suspend fun applyToJob(app: JobApplication): Boolean = withContext(Dispatchers.IO) {
        // Add to local cache
        localApplications.add(0, app)
        
        // Find job locally and increment count
        val index = localJobs.indexOfFirst { it.id == app.jobId }
        if (index != -1) {
            localJobs[index] = localJobs[index].copy(applicationsCount = localJobs[index].applicationsCount + 1)
        }
        saveLocalData()

        val fs = firestore ?: return@withContext true
        try {
            // Write application document
            fs.collection("applications").document(app.id).set(app).await()
            
            // Increment applications count on job doc in transaction
            val jobDocRef = fs.collection("jobs").document(app.jobId)
            fs.runTransaction { transaction ->
                val snapshot = transaction.get(jobDocRef)
                val currentCount = snapshot.getLong("applicationsCount") ?: 0
                transaction.update(jobDocRef, "applicationsCount", currentCount + 1)
            }.await()

            Log.d(TAG, "Successfully applied to job ${app.jobId} and updated count.")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying to job in Firestore", e)
            return@withContext true // Local success
        }
    }

    /**
     * Gets user's job applications.
     */
    suspend fun getMyApplications(userId: String): List<JobApplication> = withContext(Dispatchers.IO) {
        val fs = firestore
        if (fs == null) {
            Log.d(TAG, "Firestore offline, returning local apps.")
            return@withContext localApplications.filter { it.userId == userId || it.userId == "guest" || userId.isEmpty() }
        }

        try {
            val snapshot = fs.collection("applications")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val appList = mutableListOf<JobApplication>()
            for (doc in snapshot.documents) {
                val app = doc.toObject(JobApplication::class.java)
                if (app != null) {
                    appList.add(app.copy(id = doc.id))
                }
            }
            
            // Keep local synced with Firestore
            localApplications = appList
            saveLocalData()
            return@withContext appList.sortedByDescending { it.appliedAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting applications from Firestore", e)
            return@withContext localApplications.filter { it.userId == userId || it.userId == "guest" || userId.isEmpty() }.sortedByDescending { it.appliedAt }
        }
    }
}
