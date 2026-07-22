package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AtsAnalysisResponse
import com.example.data.api.GeminiService
import com.example.data.model.Resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CvTestScores(
    val contactInfoScore: Int = 0, // max 15
    val summaryScore: Int = 0,     // max 10
    val experienceScore: Int = 0,  // max 25
    val educationScore: Int = 0,   // max 10
    val skillsScore: Int = 0,      // max 15
    val extrasScore: Int = 0,      // max 15
    val qualityScore: Int = 0,     // max 10
    val totalScore: Int = 0,       // max 100
    val grade: String = "D",       // A+, A, B, C, D
    val tips: List<String> = emptyList()
)

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MatchViewModel"

    private val _atsResult = MutableStateFlow<AtsAnalysisResponse?>(null)
    val atsResult: StateFlow<AtsAnalysisResponse?> = _atsResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _jobDescriptionInput = MutableStateFlow("")
    val jobDescriptionInput: StateFlow<String> = _jobDescriptionInput.asStateFlow()

    private val _cvScores = MutableStateFlow(CvTestScores())
    val cvScores: StateFlow<CvTestScores> = _cvScores.asStateFlow()

    // Map of experience bullet IDs to their tailored bullet strings
    private val _tailoredBullets = MutableStateFlow<Map<String, String>>(emptyMap())
    val tailoredBullets: StateFlow<Map<String, String>> = _tailoredBullets.asStateFlow()

    private val _isTailoringBullet = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isTailoringBullet: StateFlow<Map<String, Boolean>> = _isTailoringBullet.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setJobDescription(desc: String) {
        _jobDescriptionInput.value = desc
    }

    /**
     * Executes the ATS analysis using Gemini AI.
     */
    fun analyzeAts(resume: Resume) {
        val desc = _jobDescriptionInput.value
        if (desc.isBlank()) {
            showStatus("Please paste a job description first.")
            return
        }

        // Construct a clean, comprehensive text representation of the resume
        val resumeText = buildString {
            appendLine("Name: ${resume.personalInfo.fullName}")
            appendLine("Contact: ${resume.personalInfo.email} | ${resume.personalInfo.phone} | ${resume.personalInfo.location}")
            appendLine("Summary: ${resume.personalInfo.summary}")
            appendLine("Skills: ${resume.skills.joinToString(", ")}")
            appendLine("EXPERIENCE:")
            resume.experience.forEach {
                appendLine("- ${it.title} at ${it.company} (${it.startDate} - ${it.endDate}): ${it.description}")
            }
            appendLine("EDUCATION:")
            resume.education.forEach {
                appendLine("- ${it.degree} in ${it.fieldOfStudy} at ${it.school} (GPA: ${it.gpa})")
            }
            appendLine("PROJECTS:")
            resume.projects.forEach {
                appendLine("- ${it.title}: ${it.description}")
            }
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _tailoredBullets.value = emptyMap() // Reset tailored bullets
            try {
                val result = GeminiService.analyzeAts(resumeText, desc)
                _atsResult.value = result
                showStatus("ATS Analysis Complete! Scored ${result.score}/100.")
            } catch (e: Exception) {
                Log.e(TAG, "ATS analysis failed", e)
                showStatus("Failed to analyze resume with ATS optimizer.")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Tailor a specific experience bullet to the active job description using Gemini AI.
     */
    fun tailorBulletForJob(expId: String, originalBullet: String) {
        val jobDesc = _jobDescriptionInput.value
        if (jobDesc.isBlank()) {
            showStatus("Please enter a job description to tailor this bullet point.")
            return
        }

        viewModelScope.launch {
            _isTailoringBullet.value = _isTailoringBullet.value + (expId to true)
            try {
                val tailored = GeminiService.rewriteBulletForJob(originalBullet, jobDesc)
                if (tailored.isNotBlank()) {
                    _tailoredBullets.value = _tailoredBullets.value + (expId to tailored)
                    showStatus("Bullet tailored successfully for the job!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error tailoring bullet for job", e)
                showStatus("Failed to tailor bullet.")
            } finally {
                _isTailoringBullet.value = _isTailoringBullet.value + (expId to false)
            }
        }
    }

    /**
     * Clear ATS results
     */
    fun clearAtsResult() {
        _atsResult.value = null
        _tailoredBullets.value = emptyMap()
    }

    /**
     * Runs the native CV Tester algorithm to evaluate the user's resume state.
     */
    fun evaluateCvTester(resume: Resume) {
        var contactScore = 0
        val contactTips = mutableListOf<String>()
        val info = resume.personalInfo
        if (info.fullName.isNotBlank()) contactScore += 4 else contactTips.add("Add your full name.")
        if (info.email.isNotBlank()) contactScore += 4 else contactTips.add("Provide a contact email address.")
        if (info.phone.isNotBlank()) contactScore += 4 else contactTips.add("Include a contact phone number.")
        if (info.location.isNotBlank()) contactScore += 3 else contactTips.add("State your current city/location.")

        var summaryScore = 0
        if (info.summary.isNotBlank()) {
            summaryScore = 10
        } else {
            contactTips.add("Write a professional summary or draft one using Gemini AI.")
        }

        var experienceScore = 0
        val expTips = mutableListOf<String>()
        if (resume.experience.isNotEmpty()) {
            val validRoles = resume.experience.count { it.company.isNotBlank() && it.title.isNotBlank() }
            if (validRoles >= 2) {
                experienceScore = 25
            } else if (validRoles == 1) {
                experienceScore = 15
                expTips.add("Add at least one more past job role to show career progression.")
            }
            
            val emptyDesc = resume.experience.any { it.description.isBlank() }
            if (emptyDesc) {
                expTips.add("Write description bullet points for all experience roles.")
            }
        } else {
            expTips.add("Add at least one professional work experience.")
        }

        var educationScore = 0
        val eduTips = mutableListOf<String>()
        if (resume.education.isNotEmpty()) {
            val validEdu = resume.education.any { it.school.isNotBlank() && it.degree.isNotBlank() }
            if (validEdu) {
                educationScore = 10
            } else {
                eduTips.add("Complete your school name and degree fields.")
            }
        } else {
            eduTips.add("Add your educational background (college or degree).")
        }

        var skillsScore = 0
        val skillTips = mutableListOf<String>()
        val sc = resume.skills.size
        if (sc >= 8) {
            skillsScore = 15
        } else if (sc >= 4) {
            skillsScore = 10
            skillTips.add("Add more technical or professional skills (aim for 8+).")
        } else if (sc >= 1) {
            skillsScore = 5
            skillTips.add("Include critical industry skills to pass ATS screeners.")
        } else {
            skillTips.add("Add specialized skills organized by groups in the Build tab.")
        }

        var extrasScore = 0
        val extraTips = mutableListOf<String>()
        if (resume.projects.isNotEmpty()) extrasScore += 5 else extraTips.add("Include personal or work projects to demonstrate hands-on experience.")
        if (resume.certifications.isNotEmpty()) extrasScore += 5 else extraTips.add("Add relevant certifications (e.g., AWS, PMP, Scrums) to boost credibility.")
        if (resume.languages.isNotEmpty()) extrasScore += 5 else extraTips.add("List spoken languages.")

        var qualityScore = 0
        val qualityTips = mutableListOf<String>()
        if (resume.experience.isNotEmpty()) {
            val lengths = resume.experience.map { it.description.length }
            val avg = lengths.average()
            if (avg >= 80) {
                qualityScore = 10
            } else {
                qualityScore = 5
                qualityTips.add("Elaborate on job descriptions. Use the Gemini 'Rewrite Bullets' helper to add impact.")
            }
        } else {
            qualityTips.add("Add professional detail to showcase your career outcomes.")
        }

        val total = contactScore + summaryScore + experienceScore + educationScore + skillsScore + extrasScore + qualityScore
        val grade = when {
            total >= 90 -> "A+"
            total >= 80 -> "A"
            total >= 70 -> "B"
            total >= 55 -> "C"
            else -> "D"
        }

        val allTips = contactTips + expTips + eduTips + skillTips + extraTips + qualityTips

        _cvScores.value = CvTestScores(
            contactInfoScore = contactScore,
            summaryScore = summaryScore,
            experienceScore = experienceScore,
            educationScore = educationScore,
            skillsScore = skillsScore,
            extrasScore = extrasScore,
            qualityScore = qualityScore,
            totalScore = total,
            grade = grade,
            tips = allTips
        )
    }

    private fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
