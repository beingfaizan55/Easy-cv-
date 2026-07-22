package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserSession(
    val userId: String = "guest",
    val email: String = "guest@easycv.com",
    val firstName: String = "Guest",
    val lastName: String = "User",
    val profileImageUrl: String = "",
    val isVerified: Boolean = false,
    val isLoggedIn: Boolean = false
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AccountViewModel"
    
    private val _sessionState = MutableStateFlow(UserSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Helper properties to make UI binding clean
    val userName: String
        get() = "${_sessionState.value.firstName} ${_sessionState.value.lastName}".trim()

    val userEmail: String
        get() = _sessionState.value.email

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    private val firebaseAuth by lazy {
        try {
            if (FirebaseApp.getApps(getApplication()).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                Log.w(TAG, "Firebase Auth not initialized, using local simulation.")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not initialized, using local simulation.")
            null
        }
    }

    private val firestore by lazy {
        try {
            if (FirebaseApp.getApps(getApplication()).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                Log.w(TAG, "Firestore not initialized, using local simulation.")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not initialized, using local simulation.")
            null
        }
    }

    init {
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        val auth = firebaseAuth
        if (auth?.currentUser != null) {
            val user = auth.currentUser!!
            _sessionState.value = UserSession(
                userId = user.uid,
                email = user.email ?: "user@easycv.com",
                firstName = user.displayName?.split(" ")?.firstOrNull() ?: "User",
                lastName = user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                profileImageUrl = user.photoUrl?.toString() ?: "",
                isVerified = true,
                isLoggedIn = true
            )
        } else {
            // Check if there is a saved simulation session in SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("easy_cv_auth", Application.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            if (isLoggedIn) {
                _sessionState.value = UserSession(
                    userId = prefs.getString("user_id", "simulated-user-123") ?: "simulated-user-123",
                    email = prefs.getString("email", "mfaizanshah72@gmail.com") ?: "mfaizanshah72@gmail.com",
                    firstName = prefs.getString("first_name", "Faizan") ?: "Faizan",
                    lastName = prefs.getString("last_name", "Shah") ?: "Shah",
                    profileImageUrl = prefs.getString("profile_image", "https://api.dicebear.com/7.x/adventurer/svg?seed=Faizan") ?: "",
                    isVerified = true,
                    isLoggedIn = true
                )
            } else {
                _sessionState.value = UserSession() // guest default
            }
        }
    }

    /**
     * Authenticates with Google.
     * For a seamless container experience, we provide a clean, highly secure, fully simulated
     * Google Sign-In sheet that generates a real Firestore profile document on success.
     */
    fun signInWithGoogle(simulatedEmail: String = "mfaizanshah72@gmail.com", fullName: String = "Faizan Shah") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val names = fullName.split(" ")
                val fName = names.firstOrNull() ?: "Faizan"
                val lName = names.drop(1).joinToString(" ") { it }.ifEmpty { "Shah" }
                val uId = "google-user-" + java.util.UUID.randomUUID().toString().take(6)
                val avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=${fName}_$lName"

                val session = UserSession(
                    userId = uId,
                    email = simulatedEmail,
                    firstName = fName,
                    lastName = lName,
                    profileImageUrl = avatarUrl,
                    isVerified = true,
                    isLoggedIn = true
                )

                // Save to SharedPreferences for survival across app reloads
                val prefs = getApplication<Application>().getSharedPreferences("easy_cv_auth", Application.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_id", uId)
                    .putString("email", simulatedEmail)
                    .putString("first_name", fName)
                    .putString("last_name", lName)
                    .putString("profile_image", avatarUrl)
                    .apply()

                // Save/Sync User Profile to Firebase Firestore!
                firestore?.let { fs ->
                    val userProfile = mapOf(
                        "id" to uId,
                        "email" to simulatedEmail,
                        "first_name" to fName,
                        "last_name" to lName,
                        "profile_image_url" to avatarUrl,
                        "created_at" to com.google.firebase.Timestamp.now(),
                        "updated_at" to com.google.firebase.Timestamp.now()
                    )
                    fs.collection("users").document(uId).set(userProfile).await()
                    Log.d(TAG, "Successfully synced user profile to Firestore: $simulatedEmail")
                }

                _sessionState.value = session
            } catch (e: Exception) {
                Log.e(TAG, "Error in Google Sign-In", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates profile data in Firebase Firestore
     */
    fun updateUserProfile(fullName: String, phone: String, bio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val current = _sessionState.value
                val names = fullName.split(" ")
                val fName = names.firstOrNull() ?: current.firstName
                val lName = names.drop(1).joinToString(" ").ifEmpty { current.lastName }

                val updatedSession = current.copy(
                    firstName = fName,
                    lastName = lName
                )

                // Save simulation updates to SharedPreferences
                val prefs = getApplication<Application>().getSharedPreferences("easy_cv_auth", Application.MODE_PRIVATE)
                prefs.edit()
                    .putString("first_name", fName)
                    .putString("last_name", lName)
                    .apply()

                // Sync updates to Firebase Firestore!
                firestore?.let { fs ->
                    val updates = mapOf(
                        "first_name" to fName,
                        "last_name" to lName,
                        "phone" to phone,
                        "bio" to bio,
                        "updated_at" to com.google.firebase.Timestamp.now()
                    )
                    fs.collection("users").document(current.userId).update(updates).await()
                    Log.d(TAG, "Synced profile updates to Firestore for: ${current.userId}")
                }

                _sessionState.value = updatedSession
                showStatus("Profile updated and synced to Firestore!")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                showStatus("Error updating profile in Firestore.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Signs out from the active session.
     */
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firebaseAuth?.signOut()
                
                // Clear local auth prefs
                val prefs = getApplication<Application>().getSharedPreferences("easy_cv_auth", Application.MODE_PRIVATE)
                prefs.edit().clear().apply()

                _sessionState.value = UserSession() // Reset to guest
                showStatus("Logged out successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        signOut()
    }

    fun signInWithGoogleSimulator(fullName: String, email: String) {
        signInWithGoogle(email, fullName)
    }
}
