package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Job
import com.example.data.model.JobApplication
import com.example.data.repository.JobRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class JobViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "JobViewModel"
    private val repository = JobRepository(application)

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _myApplications = MutableStateFlow<List<JobApplication>>(emptyList())
    val myApplications: StateFlow<List<JobApplication>> = _myApplications.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchLocation = MutableStateFlow("")
    val searchLocation: StateFlow<String> = _searchLocation.asStateFlow()

    private val _selectedJobType = MutableStateFlow<String?>(null) // All, Full-time, Remote, etc.
    val selectedJobType: StateFlow<String?> = _selectedJobType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Location provider
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    init {
        refreshJobs()
    }

    fun refreshJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getJobs()
                _jobs.value = list
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing jobs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadApplications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getMyApplications(userId)
                _myApplications.value = list
            } catch (e: Exception) {
                Log.e(TAG, "Error loading applications", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchLocation(loc: String) {
        _searchLocation.value = loc
    }

    fun setJobTypeFilter(type: String?) {
        _selectedJobType.value = type
    }

    /**
     * Generates a cover letter using Gemini AI.
     */
    fun generateCoverLetter(resumeJson: String, jobDescription: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Resume is passed as JSON string, we can just use the string directly
                val letter = com.example.data.api.GeminiService.generateCoverLetter(resumeJson, jobDescription)
                if (letter.isNotBlank()) {
                    onResult(letter)
                    showStatus("Cover letter generated via AI.")
                } else {
                    showStatus("Failed to generate cover letter.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating cover letter", e)
                showStatus("Error generating cover letter.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Posts a new job.
     */
    fun postJob(
        title: String,
        company: String,
        location: String,
        jobType: String,
        salaryRange: String,
        description: String,
        skillsRequired: List<String>,
        postedByName: String,
        postedByEmail: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newJob = Job(
                    title = title,
                    company = company,
                    location = location,
                    jobType = jobType,
                    salaryRange = salaryRange,
                    description = description,
                    skillsRequired = skillsRequired,
                    postedByName = postedByName,
                    postedByEmail = postedByEmail,
                    applicationsCount = 0
                )
                val success = repository.postJob(newJob)
                if (success) {
                    refreshJobs()
                    showStatus("Successfully posted job: $title")
                } else {
                    showStatus("Failed to post job.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting job", e)
                showStatus("Error posting job.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Applies to a job.
     */
    fun applyToJob(
        job: Job,
        userId: String,
        applicantName: String,
        applicantEmail: String,
        applicantPhone: String,
        coverNote: String,
        resumeJson: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val app = JobApplication(
                    jobId = job.id,
                    jobTitle = job.title,
                    companyName = job.company,
                    location = job.location,
                    userId = if (userId.isEmpty()) "guest" else userId,
                    applicantName = applicantName,
                    applicantEmail = applicantEmail,
                    applicantPhone = applicantPhone,
                    coverNote = coverNote,
                    resumeJson = resumeJson,
                    status = "pending"
                )
                val success = repository.applyToJob(app)
                if (success) {
                    // Refresh jobs to update application counts
                    refreshJobs()
                    // Reload user's applications
                    loadApplications(userId)
                    showStatus("Application submitted successfully to ${job.company}!")
                } else {
                    showStatus("Failed to submit application.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error applying to job", e)
                showStatus("Error submitting application.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Requests last known location and reverse-geocodes to get City, State.
     */
    @SuppressLint("MissingPermission")
    fun fetchLocationAndAutoFill(onPermissionRequired: () -> Unit) {
        viewModelScope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(getApplication(), Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val address = addresses?.firstOrNull()
                            if (address != null) {
                                val city = address.locality ?: ""
                                val state = address.adminArea ?: ""
                                val formatted = if (city.isNotEmpty() && state.isNotEmpty()) {
                                    "$city, $state"
                                } else if (city.isNotEmpty()) {
                                    city
                                } else {
                                    state
                                }
                                if (formatted.isNotEmpty()) {
                                    _searchLocation.value = formatted
                                    showStatus("📍 Location auto-filled: $formatted")
                                } else {
                                    showStatus("Could not resolve location address.")
                                }
                            } else {
                                showStatus("Could not resolve location address.")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Geocoder reverse-lookup failed", e)
                            showStatus("Auto-filled fallback from GPS coordinates.")
                            _searchLocation.value = "GPS Location (${String.format("%.2f", location.latitude)}, ${String.format("%.2f", location.longitude)})"
                        }
                    } else {
                        showStatus("GPS signal not found. Make sure location services are enabled.")
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Fused location fetch failed", it)
                    showStatus("Failed to fetch location from GPS.")
                    onPermissionRequired()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission not granted", e)
                onPermissionRequired()
            }
        }
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
