package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Resume
import com.example.data.model.PersonalInfo
import com.example.data.model.Experience
import com.example.data.model.Education
import com.example.data.model.Project
import com.example.data.model.Language
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ResumeRepository(private val context: Context) {
    private val TAG = "ResumeRepository"
    private val PREFS_NAME = "easy_cv_prefs"
    private val RESUME_KEY = "easy-cv:v2"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val resumeAdapter = moshi.adapter(Resume::class.java)
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

    /**
     * Load resume from local SharedPreferences.
     */
    suspend fun loadLocalResume(): Resume = withContext(Dispatchers.IO) {
        val json = sharedPrefs.getString(RESUME_KEY, null)
        if (json != null) {
            try {
                resumeAdapter.fromJson(json) ?: Resume()
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing local resume", e)
                Resume()
            }
        } else {
            Resume()
        }
    }

    /**
     * Save resume locally to SharedPreferences.
     */
    suspend fun saveLocalResume(resume: Resume) = withContext(Dispatchers.IO) {
        try {
            val json = resumeAdapter.toJson(resume)
            sharedPrefs.edit().putString(RESUME_KEY, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error serializing local resume", e)
        }
    }

    /**
     * Backup resume to Firestore for a specific user ID.
     */
    suspend fun backupToFirestore(userId: String, resume: Resume) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val jsonMap = mapOf(
                "resumeJson" to resumeAdapter.toJson(resume),
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("resumes").document(userId).set(jsonMap).await()
            Log.d(TAG, "Successfully backed up resume to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up resume to Firestore", e)
        }
    }

    /**
     * Restore resume from Firestore for a specific user ID.
     */
    suspend fun restoreFromFirestore(userId: String): Resume? = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext null
        try {
            val document = fs.collection("resumes").document(userId).get().await()
            if (document.exists()) {
                val json = document.getString("resumeJson")
                if (json != null) {
                    val resume = resumeAdapter.fromJson(json)
                    if (resume != null) {
                        saveLocalResume(resume) // Update local
                        return@withContext resume
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring resume from Firestore", e)
        }
        return@withContext null
    }

    /**
     * Pre-populate a comprehensive sample resume instantly.
     */
    fun getSampleResume(): Resume {
        return Resume(
            personalInfo = PersonalInfo(
                fullName = "James Carter",
                email = "james.carter@email.com",
                phone = "(555) 019-2834",
                location = "Austin, TX",
                linkedin = "linkedin.com/in/jamescarter-dev",
                website = "jamescarter.dev",
                summary = "Innovative Senior Android Engineer with 6+ years of experience designing and optimizing high-performance mobile applications. Proven track record of leveraging modern frameworks like Jetpack Compose, Kotlin Coroutines, and Room DB to streamline client-side state engines and reduce application rendering latency by 35%. Empathetic team leader committed to architectural excellence and robust clean code practices."
            ),
            experience = listOf(
                Experience(
                    company = "FinTech Labs",
                    title = "Senior Mobile Engineer",
                    startDate = "Jan 2023",
                    endDate = "Present",
                    isCurrent = true,
                    description = "- Spearheaded the migration of the flagship wallet application to 100% Jetpack Compose, resulting in a 40% reduction in code complexity.\n- Optimized local SQL databases utilizing Room, cutting database query overheads and application launch latency by 25%.\n- Mentored 4 junior engineers on MVVM architectures, automated JVM screenshot testing (Roborazzi), and responsive Material 3 layout structures."
                ),
                Experience(
                    company = "ByteBuilders Studio",
                    title = "Android Developer",
                    startDate = "Jun 2020",
                    endDate = "Dec 2022",
                    isCurrent = false,
                    description = "- Built and shipped 5 highly responsive client applications with custom interactive graphics and offline-first Room-backed state managers.\n- Integrated Google Identity Services and OAuth2 protocols securely for standard social authentication flows.\n- Collaborated tightly with UX designers to establish unified token-based Material Design styling guidelines."
                )
            ),
            education = listOf(
                Education(
                    school = "University of Texas at Austin",
                    degree = "Bachelor of Science",
                    fieldOfStudy = "Computer Science",
                    startDate = "2016",
                    endDate = "2020",
                    gpa = "3.85"
                )
            ),
            skills = listOf(
                "Kotlin", "Jetpack Compose", "Java", "Coroutines", "Room Database",
                "Firebase Firestore", "Retrofit", "Git", "Figma", "REST APIs", "CI/CD"
            ),
            projects = listOf(
                Project(
                    title = "Sovereign Wallet",
                    url = "github.com/jamescarter/sovereign-wallet",
                    description = "An offline-first, encrypted personal finance tracker built entirely with Jetpack Compose, custom canvas charts, and biometric security layers.",
                    techStack = "Kotlin, Compose, Room, Biometrics"
                ),
                Project(
                    title = "Compose Canvas Charts",
                    url = "github.com/jamescarter/compose-charts",
                    description = "A lightweight, customizable charting library for Jetpack Compose utilizing custom DrawScope graphics for smooth interactive gestures.",
                    techStack = "Kotlin, Jetpack Compose, Canvas"
                )
            ),
            languages = listOf(
                Language(name = "English", proficiency = "Native"),
                Language(name = "Spanish", proficiency = "Conversational")
            ),
            accentColor = "#6366F1" // Indigo
        )
    }
}
