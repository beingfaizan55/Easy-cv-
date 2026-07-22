package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Resume(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val experience: List<Experience> = emptyList(),
    val education: List<Education> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<Project> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val languages: List<Language> = emptyList(),
    val accentColor: String = "#6366F1" // Hex Accent Color (default: Indigo)
)

@JsonClass(generateAdapter = true)
data class PersonalInfo(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val linkedin: String = "",
    val website: String = "",
    val summary: String = "",
    val profilePhotoUrl: String = "" // Profile photo URI or URL
)

@JsonClass(generateAdapter = true)
data class Experience(
    val id: String = java.util.UUID.randomUUID().toString(),
    val company: String = "",
    val title: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class Education(
    val id: String = java.util.UUID.randomUUID().toString(),
    val school: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val gpa: String = ""
)

@JsonClass(generateAdapter = true)
data class Project(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val url: String = "",
    val description: String = "",
    val techStack: String = "" // Comma-separated tech stack
)

@JsonClass(generateAdapter = true)
data class Certification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val issuer: String = "",
    val date: String = "",
    val credentialId: String = ""
)

@JsonClass(generateAdapter = true)
data class Language(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val proficiency: String = "Conversational" // Basic, Conversational, Fluent, Native
)
