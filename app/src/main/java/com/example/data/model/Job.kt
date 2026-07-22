package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class Job(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val jobType: String = "Full-time", // Full-time / Part-time / Contract / Internship / Remote / Hybrid
    val salaryRange: String = "",
    val description: String = "",
    val skillsRequired: List<String> = emptyList(),
    val postedByName: String = "",
    val postedByEmail: String = "",
    val applicationsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class JobApplication(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val location: String = "",
    val userId: String = "",
    val applicantName: String = "",
    val applicantEmail: String = "",
    val applicantPhone: String = "",
    val coverNote: String = "",
    val resumeJson: String = "", // Saved Resume state as JSON
    val status: String = "pending", // pending / reviewed
    val appliedAt: Long = System.currentTimeMillis()
)

object JobSeedData {
    val sampleJobs = listOf(
        Job(
            id = "seed-job-1",
            title = "Senior Android Developer",
            company = "FinTech Labs",
            location = "San Francisco, CA",
            jobType = "Remote",
            salaryRange = "$140,000 - $180,000",
            description = "We are seeking a Senior Android Engineer with extensive Jetpack Compose and Kotlin Coroutines experience to lead the development of our flagship mobile wallets.",
            skillsRequired = listOf("Kotlin", "Jetpack Compose", "Coroutines", "Room", "Dagger Hilt", "Git"),
            postedByName = "Sarah Jenkins",
            postedByEmail = "sarah.jenkins@fintechlabs.com",
            applicationsCount = 14
        ),
        Job(
            id = "seed-job-2",
            title = "Lead Product Designer",
            company = "SaaSify Studio",
            location = "New York, NY",
            jobType = "Hybrid",
            salaryRange = "$120,000 - $150,000",
            description = "Looking for an empathetic and highly visual UX/UI designer with a robust background in design systems and mobile-first responsive interfaces.",
            skillsRequired = listOf("Figma", "User Research", "Prototyping", "Design Systems", "Mobile Design", "Adobe CC"),
            postedByName = "Marcus Vance",
            postedByEmail = "marcus@saasifystudio.com",
            applicationsCount = 8
        ),
        Job(
            id = "seed-job-3",
            title = "Registered Nurse (ICU)",
            company = "Metro Health Partners",
            location = "Chicago, IL",
            jobType = "Full-time",
            salaryRange = "$90,000 - $115,000",
            description = "Join our state-of-the-art ICU unit providing exceptional clinical care. Requires valid state licensing and active ACLS certification.",
            skillsRequired = listOf("Patient Care", "ACLS", "Critical Care", "EMR Charting", "Triage", "Communication"),
            postedByName = "David Miller",
            postedByEmail = "dmiller@metrohealth.org",
            applicationsCount = 3
        ),
        Job(
            id = "seed-job-4",
            title = "Financial Analyst",
            company = "Summit Capital Group",
            location = "Boston, MA",
            jobType = "Full-time",
            salaryRange = "$85,000 - $105,000",
            description = "Actively recruiting a detail-oriented analyst to build financial models, assess market risks, and advise executive leadership on portfolio allocation.",
            skillsRequired = listOf("Excel", "Financial Modeling", "SQL", "Tableau", "Risk Assessment", "Valuations"),
            postedByName = "Eleanor Rigby",
            postedByEmail = "e.rigby@summitcap.com",
            applicationsCount = 21
        ),
        Job(
            id = "seed-job-5",
            title = "Senior Sales Executive",
            company = "CloudScale CRM",
            location = "Austin, TX",
            jobType = "Full-time",
            salaryRange = "$100,000 + OTE Commission",
            description = "Drive outbound pipeline development and convert Enterprise prospects in our CRM software vertical. Must have 5+ years B2B sales experience.",
            skillsRequired = listOf("B2B Sales", "Salesforce", "Prospecting", "Contract Negotiation", "Demos", "Enterprise Sales"),
            postedByName = "Jeffery Gable",
            postedByEmail = "jgable@cloudscalecrm.com",
            applicationsCount = 11
        ),
        Job(
            id = "seed-job-6",
            title = "AI Research Engineer",
            company = "DeepSynthetics AI",
            location = "Seattle, WA",
            jobType = "Hybrid",
            salaryRange = "$160,000 - $210,000",
            description = "Contribute to building and fine-tuning next-generation Large Language Models. Focus on tokenization, quantization, and real-time generation latency.",
            skillsRequired = listOf("Python", "PyTorch", "LLMs", "Transformers", "Quantization", "C++"),
            postedByName = "Dr. Susan Chow",
            postedByEmail = "susan@deepsynthetics.ai",
            applicationsCount = 45
        ),
        Job(
            id = "seed-job-7",
            title = "Marketing Campaign Manager",
            company = "BrandWave Marketing",
            location = "Los Angeles, CA",
            jobType = "Contract",
            salaryRange = "$60 - $80 / hour",
            description = "Execute end-to-end digital marketing campaigns across Meta, Google Ads, and TikTok. Track conversion attribution and optimize budgets daily.",
            skillsRequired = listOf("Google Ads", "Meta Ads Manager", "SEO", "Google Analytics", "Content Strategy", "A/B Testing"),
            postedByName = "Tiffany Vance",
            postedByEmail = "tiffany@brandwave.io",
            applicationsCount = 9
        ),
        Job(
            id = "seed-job-8",
            title = "iOS Developer",
            company = "Nova Mobile Inc",
            location = "Denver, CO",
            jobType = "Remote",
            salaryRange = "$120,000 - $145,000",
            description = "Join a small, swift mobile agency building exceptional SwiftUI client apps. Emphasis on high performance, clean animations, and deep CoreData integration.",
            skillsRequired = listOf("Swift", "SwiftUI", "Combine", "CoreData", "Git", "App Store Connect"),
            postedByName = "Alex Mercer",
            postedByEmail = "alex.mercer@novamobile.co",
            applicationsCount = 5
        ),
        Job(
            id = "seed-job-9",
            title = "Data Platform Architect",
            company = "LogiCorp Global",
            location = "Atlanta, GA",
            jobType = "Hybrid",
            salaryRange = "$150,000 - $190,000",
            description = "Design, scale, and secure high-throughput cloud streaming pipelines. Experience migrating legacy databases to distributed cloud ecosystems is highly desired.",
            skillsRequired = listOf("Apache Kafka", "Kubernetes", "AWS", "Terraform", "PostgreSQL", "Scala"),
            postedByName = "Gregory House",
            postedByEmail = "ghouse@logicorp.com",
            applicationsCount = 12
        ),
        Job(
            id = "seed-job-10",
            title = "UX Content Strategist",
            company = "FlowState Labs",
            location = "Portland, OR",
            jobType = "Contract",
            salaryRange = "$50 - $70 / hour",
            description = "Lead content design and UI copy refinement for a complex fintech mobile app. Craft onboarding microcopy, tooltips, and informational sheets.",
            skillsRequired = listOf("UX Writing", "Content Strategy", "Information Architecture", "Figma", "User Empathy"),
            postedByName = "Clara Oswald",
            postedByEmail = "clara@flowstatelabs.io",
            applicationsCount = 4
        )
    )
}
