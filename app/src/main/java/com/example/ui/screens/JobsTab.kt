package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.data.model.Job
import com.example.data.model.JobApplication
import com.example.data.model.Resume
import com.example.ui.theme.*
import com.example.ui.viewmodel.AccountViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.ResumeViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsTab(
    jobViewModel: JobViewModel,
    accountViewModel: AccountViewModel,
    resumeViewModel: ResumeViewModel,
    onNavigateToAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jobs by jobViewModel.jobs.collectAsStateWithLifecycle()
    val myApplications by jobViewModel.myApplications.collectAsStateWithLifecycle()
    val searchQuery by jobViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchLocation by jobViewModel.searchLocation.collectAsStateWithLifecycle()
    val selectedJobType by jobViewModel.selectedJobType.collectAsStateWithLifecycle()
    val isLoading by jobViewModel.isLoading.collectAsStateWithLifecycle()
    val statusMsg by jobViewModel.statusMessage.collectAsStateWithLifecycle()
    
    val session by accountViewModel.sessionState.collectAsStateWithLifecycle()
    val resume by resumeViewModel.resumeState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            jobViewModel.clearStatusMessage()
        }
    }

    // Refresh applications whenever login status changes
    LaunchedEffect(session.isLoggedIn) {
        if (session.isLoggedIn) {
            jobViewModel.loadApplications(session.userId)
        }
    }

    var activeSubTab by remember { mutableStateOf("Browse Jobs") }
    val subTabs = listOf("Browse Jobs", "My Applications")

    var showPostJobDialog by remember { mutableStateOf(false) }
    var selectedJobForDetail by remember { mutableStateOf<Job?>(null) }

    // GPS location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            jobViewModel.fetchLocationAndAutoFill {}
        } else {
            Toast.makeText(context, "Location permissions denied.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        ) {
            Column {
                Text(
                    text = "EASY CV",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Job Board",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (activeSubTab == "Browse Jobs") {
                        Button(
                            onClick = { showPostJobDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("post_job_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post a Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher between Browse and Applications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    subTabs.forEach { tab ->
                        val isSelected = activeSubTab == tab
                        val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                        val textCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { activeSubTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                    }
                }
            }
        }

        // Body Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (activeSubTab) {
                "Browse Jobs" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Queries Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Search bar
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { jobViewModel.setSearchQuery(it) },
                                    placeholder = { Text("Job title, skills, or company...") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("job_search_input"),
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = if (searchQuery.isNotEmpty()) {
                                        { IconButton(onClick = { jobViewModel.setSearchQuery("") }) { Icon(Icons.Default.Close, contentDescription = null) } }
                                    } else null
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Location input with GPS button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = searchLocation,
                                        onValueChange = { jobViewModel.setSearchLocation(it) },
                                        placeholder = { Text("City, State or 'Remote'") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("job_location_input"),
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        trailingIcon = if (searchLocation.isNotEmpty()) {
                                            { IconButton(onClick = { jobViewModel.setSearchLocation("") }) { Icon(Icons.Default.Close, contentDescription = null) } }
                                        } else null
                                    )

                                    // GPS Location Button
                                    Button(
                                        onClick = {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .height(52.dp)
                                            .testTag("gps_location_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Get GPS Location")
                                    }
                                }
                            }
                        }

                        // Pull to Refresh / Refresh Control Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Job Openings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { jobViewModel.refreshJobs() }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh Jobs List",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Filtered list
                        val filteredJobs = remember(jobs, searchQuery, searchLocation) {
                            jobs.filter { job ->
                                val matchesQuery = searchQuery.isBlank() ||
                                               job.title.contains(searchQuery, ignoreCase = true) ||
                                               job.company.contains(searchQuery, ignoreCase = true) ||
                                               job.skillsRequired.any { it.contains(searchQuery, ignoreCase = true) }
                                val matchesLoc = searchLocation.isBlank() ||
                                             job.location.contains(searchLocation, ignoreCase = true) ||
                                             (searchLocation.trim().lowercase() == "remote" && job.jobType.lowercase() == "remote")
                                matchesQuery && matchesLoc
                            }
                        }

                        if (isLoading) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (filteredJobs.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No matching job vacancies found.", style = MaterialTheme.typography.bodyMedium.copy(color = TextGray))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(filteredJobs) { job ->
                                    JobCard(job = job, onClick = { selectedJobForDetail = job })
                                }
                            }
                        }
                    }
                }

                "My Applications" -> {
                    if (!session.isLoggedIn) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sign in to Track Applications",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your applications and submitted resume versions are synchronized to your secure account cloud storage.",
                                    fontSize = 13.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(onClick = onNavigateToAccount) {
                                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign In / Google Login")
                                }
                            }
                        }
                    } else {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (myApplications.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.AutoMirrored.Filled.SendAndArchive, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("You haven't applied to any jobs yet.", style = MaterialTheme.typography.bodyMedium.copy(color = TextGray))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(myApplications) { app ->
                                    ApplicationCard(app = app)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal forms

    if (showPostJobDialog) {
        PostJobDialog(
            onDismiss = { showPostJobDialog = false },
            onPost = { title, comp, loc, type, sal, desc, skills, name, email ->
                jobViewModel.postJob(title, comp, loc, type, sal, desc, skills, name, email)
                showPostJobDialog = false
            }
        )
    }

    if (selectedJobForDetail != null) {
        val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
        val json = moshi.adapter(com.example.data.model.Resume::class.java).toJson(resume)

        JobDetailDialog(
            job = selectedJobForDetail!!,
            isLoggedIn = session.isLoggedIn,
            userResume = resume,
            jobViewModel = jobViewModel,
            resumeJson = json,
            onDismiss = { selectedJobForDetail = null },
            onApply = { name, email, phone, cover ->
                jobViewModel.applyToJob(
                    job = selectedJobForDetail!!,
                    userId = session.userId,
                    applicantName = name,
                    applicantEmail = email,
                    applicantPhone = phone,
                    coverNote = cover,
                    resumeJson = json
                )
                selectedJobForDetail = null
            }
        )
    }
}

@Composable
fun JobCard(job: Job, onClick: () -> Unit) {
    val charAvatar = job.company.firstOrNull()?.toString()?.uppercase() ?: "J"
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character initial avatar placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = charAvatar,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${job.company} • ${job.location}",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
                
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = TextGray.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pills: Job Type & Salary
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = job.jobType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (job.salaryRange.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = job.salaryRange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tech stack required tags (first 3)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                job.skillsRequired.take(4).forEach { skill ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, LightGrayBorder, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${job.applicationsCount} applicants",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("View & Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(app: JobApplication) {
    val formatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(app.appliedAt))

    val statusColor = if (app.status == "pending") WarningOrange else SuccessGreen

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.jobTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${app.companyName} • ${app.location}",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = LightGrayBorder)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Applied on: $formattedDate",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = "CV Attached",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PostJobDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String, String, String, String, List<String>, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("Full-time") }
    var salary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var skillsInput by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val jobTypes = listOf("Full-time", "Part-time", "Contract", "Internship", "Remote", "Hybrid")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Post a New Job Opening",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location (e.g. Austin, TX)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                // Job Type selection row
                Text("Job Type", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(jobTypes) { type ->
                        val isSel = jobType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { jobType = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary / Rate Range") }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("$120,000 - $140,000") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Full Description") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 5)
                OutlinedTextField(value = skillsInput, onValueChange = { skillsInput = it }, label = { Text("Required Skills (Comma separated)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Kotlin, Compose, Room, APIs") }, singleLine = true)
                
                HorizontalDivider(color = LightGrayBorder)
                Text("Poster Contact Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Your Contact Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val skillsList = skillsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onPost(title, company, location, jobType, salary, description, skillsList, name, email)
                        },
                        enabled = title.isNotBlank() && company.isNotBlank() && description.isNotBlank() && name.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Post Opening")
                    }
                }
            }
        }
    }
}

@Composable
fun JobDetailDialog(
    job: Job,
    isLoggedIn: Boolean,
    userResume: Resume,
    jobViewModel: JobViewModel,
    resumeJson: String,
    onDismiss: () -> Unit,
    onApply: (String, String, String, String) -> Unit
) {
    var showApplyForm by remember { mutableStateOf(false) }

    var applicantName by remember { mutableStateOf(userResume.personalInfo.fullName) }
    var applicantEmail by remember { mutableStateOf(userResume.personalInfo.email) }
    var applicantPhone by remember { mutableStateOf(userResume.personalInfo.phone) }
    var coverNote by remember { mutableStateOf("") }

    val charAvatar = job.company.firstOrNull()?.toString()?.uppercase() ?: "J"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = charAvatar,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = job.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${job.company} • ${job.location}",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(job.jobType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    if (job.salaryRange.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(job.salaryRange, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }

                HorizontalDivider(color = LightGrayBorder)

                // Description
                Text("Role Description", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = job.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Required Skills
                Text("Skills Required", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    job.skillsRequired.forEach { skill ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, LightGrayBorder, RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(skill, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!showApplyForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Close") }
                        Button(
                            onClick = { showApplyForm = true },
                            modifier = Modifier.testTag("apply_now_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Now")
                        }
                    }
                } else {
                    // Expanded Apply Form
                    HorizontalDivider(color = LightGrayBorder)
                    Text("Submit Application", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    
                    Text(
                        text = "Your current 'Easy CV' structured resume JSON will be automatically attached to this application.",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    OutlinedTextField(value = applicantName, onValueChange = { applicantName = it }, label = { Text("Your Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = applicantEmail, onValueChange = { applicantEmail = it }, label = { Text("Contact Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = applicantPhone, onValueChange = { applicantPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cover Letter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = {
                                jobViewModel.generateCoverLetter(resumeJson, job.description) { generated ->
                                    coverNote = generated
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("ai_generate_cover_letter")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(value = coverNote, onValueChange = { coverNote = it }, label = { Text("Add optional cover note...") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 8)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showApplyForm = false }) { Text("Back") }
                        Button(
                            onClick = {
                                onApply(applicantName, applicantEmail, applicantPhone, coverNote)
                            },
                            enabled = applicantName.isNotBlank() && applicantEmail.isNotBlank(),
                            modifier = Modifier.testTag("submit_application_button")
                        ) {
                            Text("Submit Application")
                        }
                    }
                }
            }
        }
    }
}
