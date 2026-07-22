package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.ResumeViewModel

// Predefined 10 skill categories with popular industry skills (50+)
private val SkillCategories = mapOf(
    "Tech" to listOf("Kotlin", "Jetpack Compose", "Java", "Python", "React", "SQL", "AWS", "Git", "Node.js", "Docker", "TypeScript", "C++"),
    "Business" to listOf("Project Management", "Scrum", "Agile", "Business Analysis", "Product Strategy", "CRM", "Leadership", "Budgeting", "Excel"),
    "Creative" to listOf("Figma", "UI/UX Design", "Photoshop", "Illustrator", "Prototyping", "Design Systems", "Motion Graphics", "Wireframing"),
    "Healthcare" to listOf("Patient Care", "ACLS", "ICU Care", "Triage", "CPR", "Clinical Nursing", "EHR Charting", "Diagnostics"),
    "Finance" to listOf("Financial Modeling", "Valuation", "SQL", "Tableau", "Accounting", "Portfolio Management", "Risk Assessment"),
    "Sales" to listOf("B2B Sales", "Salesforce", "Enterprise Sales", "Lead Generation", "Product Demo", "Contract Negotiation", "Prospecting"),
    "Marketing" to listOf("SEO", "Google Ads", "Meta Ads", "Google Analytics", "Content Strategy", "Email Marketing", "A/B Testing"),
    "Writing" to listOf("UX Writing", "Technical Writing", "Content Design", "Copywriting", "Editing", "SEO Writing", "Proofreading"),
    "Science" to listOf("Data Analysis", "R Programming", "MATLAB", "Research Methods", "Statistics", "Machine Learning", "Experimentation"),
    "Languages" to listOf("English", "Spanish", "French", "German", "Mandarin", "Japanese", "Portuguese", "Arabic", "Hindi")
)

private val PredefinedColors = listOf(
    "#6366F1" to "Indigo",
    "#3B82F6" to "Blue",
    "#0EA5E9" to "Sky",
    "#10B981" to "Emerald",
    "#F59E0B" to "Amber",
    "#EF4444" to "Rose",
    "#8B5CF6" to "Violet",
    "#EC4899" to "Pink",
    "#64748B" to "Slate",
    "#1E293B" to "Charcoal"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildTab(
    viewModel: ResumeViewModel,
    modifier: Modifier = Modifier
) {
    val resume by viewModel.resumeState.collectAsStateWithLifecycle()
    val progress by viewModel.completionScore.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()
    
    val context = LocalContext.current

    // Trigger toast messages from VM status
    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    val selectedSection by viewModel.activeBuildSection.collectAsStateWithLifecycle()
    val sections = listOf("Personal", "Experience", "Education", "Skills", "Projects", "More")

    var showLinkedInDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EASY CV",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row {
                            IconButton(
                                onClick = { showLinkedInDialog = true },
                                modifier = Modifier.size(24.dp).testTag("linkedin_import_button")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Import from LinkedIn", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.loadSample() },
                                modifier = Modifier.size(24.dp).testTag("load_sample_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Load Sample Resume", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Build",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$progress% Complete",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = TextGray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier
                                    .width(96.dp)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Section Select Tab Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sections) { section ->
                    val isSelected = selectedSection == section
                    val accentHex = resume.accentColor
                    val accentColor = remember(accentHex) { Color(android.graphics.Color.parseColor(accentHex)) }
                    
                    val bg = if (isSelected) accentColor else MaterialTheme.colorScheme.surface
                    val textCol = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(bg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else LightGrayBorder,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { viewModel.setActiveBuildSection(section) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = section,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textCol
                        )
                    }
                }
            }

            // Section Content Builder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedSection) {
                    "Personal" -> PersonalInfoSection(resume.personalInfo, viewModel, isLoading)
                    "Experience" -> ExperienceSection(resume.experience, viewModel, isLoading)
                    "Education" -> EducationSection(resume.education, viewModel)
                    "Skills" -> SkillsSection(resume.skills, viewModel)
                    "Projects" -> ProjectsSection(resume.projects, viewModel)
                    "More" -> ExtrasSection(resume, viewModel)
                }
            }
        }

        // Global full-screen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI is thinking...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // LinkedIn Import Dialog
        if (showLinkedInDialog) {
            LinkedInImportDialog(
                onDismiss = { showLinkedInDialog = false },
                onImport = { text ->
                    viewModel.importFromLinkedIn(text)
                    showLinkedInDialog = false
                }
            )
        }
    }
}

// --- Component sections ---

@Composable
fun PersonalInfoSection(info: PersonalInfo, viewModel: ResumeViewModel, isLoading: Boolean) {
    var name by remember(info.fullName) { mutableStateOf(info.fullName) }
    var email by remember(info.email) { mutableStateOf(info.email) }
    var phone by remember(info.phone) { mutableStateOf(info.phone) }
    var location by remember(info.location) { mutableStateOf(info.location) }
    var linkedin by remember(info.linkedin) { mutableStateOf(info.linkedin) }
    var website by remember(info.website) { mutableStateOf(info.website) }
    var summary by remember(info.summary) { mutableStateOf(info.summary) }

    fun triggerSave() {
        viewModel.updatePersonalInfo(
            PersonalInfo(
                fullName = name,
                email = email,
                phone = phone,
                location = location,
                linkedin = linkedin,
                website = website,
                summary = summary
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; triggerSave() },
                        label = { Text("Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; triggerSave() },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; triggerSave() },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it; triggerSave() },
                            label = { Text("Location (e.g. Austin, TX)") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = linkedin,
                        onValueChange = { linkedin = it; triggerSave() },
                        label = { Text("LinkedIn URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it; triggerSave() },
                        label = { Text("Personal Website / Portfolio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Professional Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { viewModel.draftSummary() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("draft_summary_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draft Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it; triggerSave() },
                        label = { Text("Professional Profile Summary") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 6
                    )
                }
            }
        }
    }
}

@Composable
fun ExperienceSection(experience: List<Experience>, viewModel: ResumeViewModel, isLoading: Boolean) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeExperience by remember { mutableStateOf<Experience?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Work History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { activeExperience = Experience(); showAddDialog = true },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.testTag("add_experience_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Experience")
            }
        }

        if (experience.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.WorkOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No work experience added yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(experience) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.company} • ${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate}",
                                        fontSize = 13.sp,
                                        color = TextGray
                                    )
                                }
                                Row {
                                    IconButton(onClick = { activeExperience = item; showAddDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteExperience(item.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog && activeExperience != null) {
            ExperienceDialog(
                experience = activeExperience!!,
                onDismiss = { showAddDialog = false },
                onSave = { exp ->
                    if (experience.any { it.id == exp.id }) {
                        viewModel.updateExperience(exp)
                    } else {
                        viewModel.addExperience(exp)
                    }
                    showAddDialog = false
                },
                viewModel = viewModel,
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun ExperienceDialog(
    experience: Experience,
    onDismiss: () -> Unit,
    onSave: (Experience) -> Unit,
    viewModel: ResumeViewModel,
    isLoading: Boolean
) {
    var company by remember { mutableStateOf(experience.company) }
    var title by remember { mutableStateOf(experience.title) }
    var startDate by remember { mutableStateOf(experience.startDate) }
    var endDate by remember { mutableStateOf(experience.endDate) }
    var isCurrent by remember { mutableStateOf(experience.isCurrent) }
    var description by remember { mutableStateOf(experience.description) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (experience.company.isBlank()) "Add Experience" else "Edit Experience",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Title (e.g. Android Developer)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("MM/YYYY") }
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f),
                            enabled = !isCurrent,
                            singleLine = true,
                            placeholder = { Text("MM/YYYY") }
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCurrent, onCheckedChange = { isCurrent = it })
                        Text("I am currently working in this role", fontSize = 14.sp)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Responsibilities / Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = {
                                viewModel.rewriteExperienceBullets(experience.id, description) { rewritten ->
                                    description = rewritten
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("ai_rewrite_bullets_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rewrite Bullets", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Role Description (Use bullet points)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 6
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && company.isNotBlank()) {
                                    onSave(
                                        experience.copy(
                                            company = company,
                                            title = title,
                                            startDate = startDate,
                                            endDate = if (isCurrent) "Present" else endDate,
                                            isCurrent = isCurrent,
                                            description = description
                                        )
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && company.isNotBlank()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EducationSection(education: List<Education>, viewModel: ResumeViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeEducation by remember { mutableStateOf<Education?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Education History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { activeEducation = Education(); showAddDialog = true },
                shape = RoundedCornerShape(999.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Education")
            }
        }

        if (education.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No education history added yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(education) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.degree} in ${item.fieldOfStudy}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.school} • ${item.startDate} - ${item.endDate}",
                                        fontSize = 13.sp,
                                        color = TextGray
                                    )
                                    if (item.gpa.isNotBlank()) {
                                        Text(text = "GPA: ${item.gpa}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { activeEducation = item; showAddDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteEducation(item.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog && activeEducation != null) {
            EducationDialog(
                education = activeEducation!!,
                onDismiss = { showAddDialog = false },
                onSave = { edu ->
                    if (education.any { it.id == edu.id }) {
                        viewModel.updateEducation(edu)
                    } else {
                        viewModel.addEducation(edu)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EducationDialog(
    education: Education,
    onDismiss: () -> Unit,
    onSave: (Education) -> Unit
) {
    var school by remember { mutableStateOf(education.school) }
    var degree by remember { mutableStateOf(education.degree) }
    var fieldOfStudy by remember { mutableStateOf(education.fieldOfStudy) }
    var startDate by remember { mutableStateOf(education.startDate) }
    var endDate by remember { mutableStateOf(education.endDate) }
    var gpa by remember { mutableStateOf(education.gpa) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (education.school.isBlank()) "Add Education" else "Edit Education",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School / University Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = degree,
                        onValueChange = { degree = it },
                        label = { Text("Degree (e.g. BS)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fieldOfStudy,
                        onValueChange = { fieldOfStudy = it },
                        label = { Text("Field of Study") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("YYYY") }
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Year (or Expected)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("YYYY") }
                    )
                }

                OutlinedTextField(
                    value = gpa,
                    onValueChange = { gpa = it },
                    label = { Text("GPA / Grade (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (school.isNotBlank() && degree.isNotBlank()) {
                                onSave(
                                    education.copy(
                                        school = school,
                                        degree = degree,
                                        fieldOfStudy = fieldOfStudy,
                                        startDate = startDate,
                                        endDate = endDate,
                                        gpa = gpa
                                    )
                                )
                            }
                        },
                        enabled = school.isNotBlank() && degree.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SkillsSection(skills: List<String>, viewModel: ResumeViewModel) {
    var searchSkillQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("Tech") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Skills Library",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Custom chip list of user added skills
        if (skills.isNotEmpty()) {
            Text(
                text = "Your Skills (${skills.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                skills.forEach { skill ->
                    Surface(
                        onClick = { viewModel.removeSkill(skill) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("skill_chip_${skill}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(skill, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Add Custom Skill Input field
        var customSkillText by remember { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customSkillText,
                onValueChange = { customSkillText = it },
                label = { Text("Add custom skill...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_skill_input")
            )
            Button(
                onClick = {
                    if (customSkillText.isNotBlank()) {
                        viewModel.addSkill(customSkillText)
                        customSkillText = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_skill_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Predefined 10 Groups Category Selector
        Text(
            text = "Browse Predefined Skills Library",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(SkillCategories.keys.toList()) { cat ->
                val isSel = activeCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSel) Color.Transparent else LightGrayBorder,
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable { activeCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else TextGray
                    )
                }
            }
        }

        // Search in selected categories
        OutlinedTextField(
            value = searchSkillQuery,
            onValueChange = { searchSkillQuery = it },
            placeholder = { Text("Search skill in library...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )

        // Render skill options from category
        val displayedSkills = remember(activeCategory, searchSkillQuery) {
            val list = SkillCategories[activeCategory] ?: emptyList()
            if (searchSkillQuery.isBlank()) {
                list
            } else {
                list.filter { it.contains(searchSkillQuery, ignoreCase = true) }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LightGrayBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayedSkills.forEach { skillOption ->
                            val alreadyHas = skills.contains(skillOption)
                            InputChip(
                                selected = alreadyHas,
                                onClick = {
                                    if (alreadyHas) {
                                        viewModel.removeSkill(skillOption)
                                    } else {
                                        viewModel.addSkill(skillOption)
                                    }
                                },
                                label = { Text(skillOption, fontSize = 12.sp) },
                                leadingIcon = if (alreadyHas) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectsSection(projects: List<Project>, viewModel: ResumeViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeProject by remember { mutableStateOf<Project?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Projects",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { activeProject = Project(); showAddDialog = true },
                shape = RoundedCornerShape(999.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Project")
            }
        }

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No projects added yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(projects) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.url.isNotBlank()) {
                                        Text(text = item.url, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { activeProject = item; showAddDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteProject(item.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (item.techStack.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item.techStack.split(",").forEach { tech ->
                                        if (tech.trim().isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(tech.trim(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog && activeProject != null) {
            ProjectDialog(
                project = activeProject!!,
                onDismiss = { showAddDialog = false },
                onSave = { proj ->
                    if (projects.any { it.id == proj.id }) {
                        viewModel.updateProject(proj)
                    } else {
                        viewModel.addProject(proj)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit
) {
    var title by remember { mutableStateOf(project.title) }
    var url by remember { mutableStateOf(project.url) }
    var description by remember { mutableStateOf(project.description) }
    var techStack by remember { mutableStateOf(project.techStack) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (project.title.isBlank()) "Add Project" else "Edit Project",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Project URL (e.g. Github link)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Project Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 4
                )

                OutlinedTextField(
                    value = techStack,
                    onValueChange = { techStack = it },
                    label = { Text("Tech Stack Tags (Comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Kotlin, Compose, Room, Retrofit") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    project.copy(
                                        title = title,
                                        url = url,
                                        description = description,
                                        techStack = techStack
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ExtrasSection(resume: Resume, viewModel: ResumeViewModel) {
    var showAddCert by remember { mutableStateOf(false) }
    var activeCert by remember { mutableStateOf<Certification?>(null) }

    var showAddLang by remember { mutableStateOf(false) }
    var activeLang by remember { mutableStateOf<Language?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Accent Color Picker Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CV Accent Color Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Customize the primary visual accent color utilized in your rendered CV templates.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(PredefinedColors) { (hex, label) ->
                            val isSelected = resume.accentColor.equals(hex, ignoreCase = true)
                            val col = remember(hex) { Color(android.graphics.Color.parseColor(hex)) }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable { viewModel.updateAccentColor(hex) }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else LightGrayBorder,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Certifications Sub-Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Certifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { activeCert = Certification(); showAddCert = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Certification", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (resume.certifications.isEmpty()) {
                        Text(
                            text = "No certifications added yet. (Add AWS, GCP, PMP, Scrums etc.)",
                            fontSize = 13.sp,
                            color = TextGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        resume.certifications.forEach { cert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cert.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${cert.issuer} • ${cert.date}", fontSize = 12.sp, color = TextGray)
                                    if (cert.credentialId.isNotBlank()) {
                                        Text("Credential ID: ${cert.credentialId}", fontSize = 11.sp, color = TextGray)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { activeCert = cert; showAddCert = true }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteCertification(cert.id) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Languages Sub-Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spoken Languages",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { activeLang = Language(); showAddLang = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Language", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (resume.languages.isEmpty()) {
                        Text(
                            text = "No languages specified.",
                            fontSize = 13.sp,
                            color = TextGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            resume.languages.forEach { lang ->
                                Surface(
                                    onClick = { activeLang = lang; showAddLang = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("lang_chip_${lang.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${lang.name} (${lang.proficiency})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Delete",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.deleteLanguage(lang.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Clear Resume Trigger Button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.clearResume() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Resume Fields")
            }
        }
    }

    // Modal dialogs

    if (showAddCert && activeCert != null) {
        var name by remember { mutableStateOf(activeCert!!.name) }
        var issuer by remember { mutableStateOf(activeCert!!.issuer) }
        var date by remember { mutableStateOf(activeCert!!.date) }
        var credId by remember { mutableStateOf(activeCert!!.credentialId) }

        Dialog(onDismissRequest = { showAddCert = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Certification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Certification Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuing Organization") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Issue Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = credId, onValueChange = { credId = it }, label = { Text("Credential ID (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddCert = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val cert = activeCert!!.copy(name = name, issuer = issuer, date = date, credentialId = credId)
                                    if (resume.certifications.any { it.id == cert.id }) viewModel.updateCertification(cert)
                                    else viewModel.addCertification(cert)
                                    showAddCert = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showAddLang && activeLang != null) {
        var name by remember { mutableStateOf(activeLang!!.name) }
        var prof by remember { mutableStateOf(activeLang!!.proficiency) }
        val profLevels = listOf("Conversational", "Basic", "Fluent", "Native")
        var showDrop by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddLang = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Language", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Language Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = prof,
                            onValueChange = {},
                            label = { Text("Proficiency Level") },
                            modifier = Modifier.fillMaxWidth().clickable { showDrop = true },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showDrop = true }) }
                        )
                        DropdownMenu(expanded = showDrop, onDismissRequest = { showDrop = false }) {
                            profLevels.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level) },
                                    onClick = { prof = level; showDrop = false }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddLang = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val lang = activeLang!!.copy(name = name, proficiency = prof)
                                    if (resume.languages.any { it.id == lang.id }) viewModel.updateLanguage(lang)
                                    else viewModel.addLanguage(lang)
                                    showAddLang = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}

@Composable
fun LinkedInImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var linkedinText by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Import from LinkedIn",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Paste your raw LinkedIn profile text (e.g. copied from 'Save to PDF' or your Profile page), and Gemini AI will parse and auto-fill your fields instantly!",
                    fontSize = 12.sp,
                    color = TextGray
                )

                OutlinedTextField(
                    value = linkedinText,
                    onValueChange = { linkedinText = it },
                    label = { Text("Paste LinkedIn Profile text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onImport(linkedinText) },
                        enabled = linkedinText.isNotBlank(),
                        modifier = Modifier.testTag("linkedin_submit_button")
                    ) {
                        Text("Parse Profile")
                    }
                }
            }
        }
    }
}

// Custom flow row layout helper since standard FlowRow is part of experimental layout
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
