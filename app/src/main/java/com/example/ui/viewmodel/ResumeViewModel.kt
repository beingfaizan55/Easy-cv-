package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.model.Resume
import com.example.data.model.PersonalInfo
import com.example.data.model.Experience
import com.example.data.model.Education
import com.example.data.model.Project
import com.example.data.model.Certification
import com.example.data.model.Language
import com.example.data.repository.ResumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResumeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ResumeViewModel"
    private val repository = ResumeRepository(application)

    private val _resumeState = MutableStateFlow(Resume())
    val resumeState: StateFlow<Resume> = _resumeState.asStateFlow()

    private val _activeBuildSection = MutableStateFlow("Personal")
    val activeBuildSection: StateFlow<String> = _activeBuildSection.asStateFlow()

    private val _completionScore = MutableStateFlow(0)
    val completionScore: StateFlow<Int> = _completionScore.asStateFlow()

    fun setActiveBuildSection(section: String) {
        _activeBuildSection.value = section
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadResume()
    }

    private fun loadResume() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resume = repository.loadLocalResume()
                _resumeState.value = resume
                updateCompletionScore(resume)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading resume", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveResume(resume: Resume) {
        viewModelScope.launch {
            try {
                _resumeState.value = resume
                repository.saveLocalResume(resume)
                updateCompletionScore(resume)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving resume", e)
            }
        }
    }

    /**
     * Backup to Firebase Firestore if logged in
     */
    fun backupToCloud(userId: String) {
        viewModelScope.launch {
            if (userId != "guest" && userId.isNotEmpty()) {
                repository.backupToFirestore(userId, _resumeState.value)
                showStatus("Resume backed up to Firebase Firestore!")
            }
        }
    }

    /**
     * Restore from Firebase Firestore
     */
    fun restoreFromCloud(userId: String) {
        viewModelScope.launch {
            if (userId != "guest" && userId.isNotEmpty()) {
                _isLoading.value = true
                val restored = repository.restoreFromFirestore(userId)
                if (restored != null) {
                    _resumeState.value = restored
                    updateCompletionScore(restored)
                    showStatus("Resume restored from Firebase Firestore!")
                } else {
                    showStatus("No backup found on Firestore.")
                }
                _isLoading.value = false
            }
        }
    }

    // --- State Mutation Actions ---

    fun updatePersonalInfo(personalInfo: PersonalInfo) {
        val current = _resumeState.value
        saveResume(current.copy(personalInfo = personalInfo))
    }

    fun addExperience(exp: Experience) {
        val current = _resumeState.value
        saveResume(current.copy(experience = current.experience + exp))
    }

    fun updateExperience(updatedExp: Experience) {
        val current = _resumeState.value
        val updatedList = current.experience.map { if (it.id == updatedExp.id) updatedExp else it }
        saveResume(current.copy(experience = updatedList))
    }

    fun deleteExperience(id: String) {
        val current = _resumeState.value
        saveResume(current.copy(experience = current.experience.filterNot { it.id == id }))
    }

    fun addEducation(edu: Education) {
        val current = _resumeState.value
        saveResume(current.copy(education = current.education + edu))
    }

    fun updateEducation(updatedEdu: Education) {
        val current = _resumeState.value
        val updatedList = current.education.map { if (it.id == updatedEdu.id) updatedEdu else it }
        saveResume(current.copy(education = updatedList))
    }

    fun deleteEducation(id: String) {
        val current = _resumeState.value
        saveResume(current.copy(education = current.education.filterNot { it.id == id }))
    }

    fun addSkill(skill: String) {
        val current = _resumeState.value
        if (skill.isNotBlank() && !current.skills.contains(skill.trim())) {
            saveResume(current.copy(skills = current.skills + skill.trim()))
        }
    }

    fun removeSkill(skill: String) {
        val current = _resumeState.value
        saveResume(current.copy(skills = current.skills.filterNot { it == skill }))
    }

    fun addProject(project: Project) {
        val current = _resumeState.value
        saveResume(current.copy(projects = current.projects + project))
    }

    fun updateProject(updatedProj: Project) {
        val current = _resumeState.value
        val updatedList = current.projects.map { if (it.id == updatedProj.id) updatedProj else it }
        saveResume(current.copy(projects = updatedList))
    }

    fun deleteProject(id: String) {
        val current = _resumeState.value
        saveResume(current.copy(projects = current.projects.filterNot { it.id == id }))
    }

    fun addCertification(cert: Certification) {
        val current = _resumeState.value
        saveResume(current.copy(certifications = current.certifications + cert))
    }

    fun updateCertification(updatedCert: Certification) {
        val current = _resumeState.value
        val updatedList = current.certifications.map { if (it.id == updatedCert.id) updatedCert else it }
        saveResume(current.copy(certifications = updatedList))
    }

    fun deleteCertification(id: String) {
        val current = _resumeState.value
        saveResume(current.copy(certifications = current.certifications.filterNot { it.id == id }))
    }

    fun addLanguage(lang: Language) {
        val current = _resumeState.value
        saveResume(current.copy(languages = current.languages + lang))
    }

    fun updateLanguage(updatedLang: Language) {
        val current = _resumeState.value
        val updatedList = current.languages.map { if (it.id == updatedLang.id) updatedLang else it }
        saveResume(current.copy(languages = updatedList))
    }

    fun deleteLanguage(id: String) {
        val current = _resumeState.value
        saveResume(current.copy(languages = current.languages.filterNot { it.id == id }))
    }

    fun updateAccentColor(hexColor: String) {
        val current = _resumeState.value
        saveResume(current.copy(accentColor = hexColor))
    }

    fun loadSample() {
        val sample = repository.getSampleResume()
        saveResume(sample)
        showStatus("Loaded Sample Resume instantly!")
    }

    fun clearResume() {
        saveResume(Resume())
        showStatus("Cleared resume template.")
    }

    // --- Dynamic Completion Score Calculation ---

    private fun updateCompletionScore(resume: Resume) {
        var score = 0
        
        // 1. Personal Info: 20% total
        val info = resume.personalInfo
        if (info.fullName.isNotBlank()) score += 4
        if (info.email.isNotBlank()) score += 4
        if (info.phone.isNotBlank()) score += 4
        if (info.location.isNotBlank()) score += 4
        if (info.summary.isNotBlank()) score += 4

        // 2. Experience: 20% total
        if (resume.experience.isNotEmpty()) {
            val validExp = resume.experience.any { it.company.isNotBlank() && it.title.isNotBlank() }
            if (validExp) score += 20
        }

        // 3. Education: 15% total
        if (resume.education.isNotEmpty()) {
            val validEdu = resume.education.any { it.school.isNotBlank() && it.degree.isNotBlank() }
            if (validEdu) score += 15
        }

        // 4. Skills: 15% total
        if (resume.skills.isNotEmpty()) {
            val skillCount = resume.skills.size
            score += when {
                skillCount >= 8 -> 15
                skillCount >= 4 -> 10
                else -> 5
            }
        }

        // 5. Projects: 10% total
        if (resume.projects.isNotEmpty()) {
            val validProj = resume.projects.any { it.title.isNotBlank() }
            if (validProj) score += 10
        }

        // 6. Certifications: 10% total
        if (resume.certifications.isNotEmpty()) {
            score += 10
        }

        // 7. Languages: 10% total
        if (resume.languages.isNotEmpty()) {
            score += 10
        }

        _completionScore.value = score.coerceAtMost(100)
    }

    // --- Gemini AI Trigger Methods ---

    /**
     * Calls Gemini to generate a professional summary.
     */
    fun draftSummary() {
        val current = _resumeState.value
        if (current.personalInfo.fullName.isBlank() && current.skills.isEmpty() && current.experience.isEmpty()) {
            showStatus("Please fill in some skills or experiences first so Gemini can draft a profile summary.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val drafted = GeminiService.generateDraftSummary(
                    fullName = current.personalInfo.fullName,
                    skills = current.skills,
                    experiences = current.experience
                )
                if (drafted.isNotBlank()) {
                    updatePersonalInfo(current.personalInfo.copy(summary = drafted))
                    showStatus("Professional profile summary drafted by Gemini AI!")
                } else {
                    showStatus("Failed to draft summary with AI.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error drafting summary", e)
                showStatus("Error communicating with Gemini AI.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Rewrite experience bullets with Gemini.
     */
    fun rewriteExperienceBullets(expId: String, currentDescription: String, onComplete: (String) -> Unit) {
        if (currentDescription.isBlank()) {
            showStatus("Write description bullets first to let Gemini rewrite them.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rewritten = GeminiService.rewriteBullets(currentDescription)
                if (rewritten.isNotBlank()) {
                    onComplete(rewritten)
                    showStatus("Experience bullets polished and rewritten by Gemini AI!")
                } else {
                    showStatus("No change returned from AI.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rewriting bullets", e)
                showStatus("Error communicating with Gemini AI.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Imports from LinkedIn text and parses into structured CV.
     */
    fun importFromLinkedIn(text: String) {
        if (text.isBlank()) {
            showStatus("Paste LinkedIn profile text to import.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            showStatus("Gemini AI is parsing your LinkedIn profile...")
            try {
                val parsed = GeminiService.parseLinkedInProfile(text)
                if (parsed.personalInfo.fullName.isNotBlank()) {
                    // Retain accent color
                    val finalResume = parsed.copy(accentColor = _resumeState.value.accentColor)
                    saveResume(finalResume)
                    showStatus("Successfully imported and structured LinkedIn profile!")
                } else {
                    showStatus("Failed to extract structured resume from LinkedIn. Make sure the pasted text is clean.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing LinkedIn profile", e)
                showStatus("Error analyzing profile. Please try again.")
            } finally {
                _isLoading.value = false
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
