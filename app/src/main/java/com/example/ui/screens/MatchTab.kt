package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Resume
import com.example.ui.theme.*
import com.example.ui.viewmodel.CvTestScores
import com.example.ui.viewmodel.MatchViewModel
import com.example.ui.viewmodel.ResumeViewModel

@Composable
fun MatchTab(
    resumeViewModel: ResumeViewModel,
    matchViewModel: MatchViewModel,
    onNavigateToBuildSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val resume by resumeViewModel.resumeState.collectAsStateWithLifecycle()
    val atsResult by matchViewModel.atsResult.collectAsStateWithLifecycle()
    val isAnalyzing by matchViewModel.isAnalyzing.collectAsStateWithLifecycle()
    val jobDescInput by matchViewModel.jobDescriptionInput.collectAsStateWithLifecycle()
    val cvScores by matchViewModel.cvScores.collectAsStateWithLifecycle()
    val tailoredBullets by matchViewModel.tailoredBullets.collectAsStateWithLifecycle()
    val isTailoringMap by matchViewModel.isTailoringBullet.collectAsStateWithLifecycle()
    val statusMsg by matchViewModel.statusMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            matchViewModel.clearStatusMessage()
        }
    }

    // Reactively evaluate CV Tester whenever resume is updated
    LaunchedEffect(resume) {
        matchViewModel.evaluateCvTester(resume)
    }

    var selectedSubTab by remember { mutableStateOf("ATS Optimizer") }
    val subTabs = listOf("ATS Optimizer", "CV Tester")

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
                Text(
                    text = "Optimize & Test",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Tab Switcher (Sub Tab Switcher)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    subTabs.forEach { tab ->
                        val isSelected = selectedSubTab == tab
                        val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                        val textCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        val weight = 1f
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { selectedSubTab = tab }
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

        // Sub Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedSubTab) {
                "ATS Optimizer" -> AtsOptimizerScreen(
                    resume = resume,
                    atsResult = atsResult,
                    isAnalyzing = isAnalyzing,
                    jobDescInput = jobDescInput,
                    tailoredBullets = tailoredBullets,
                    isTailoringMap = isTailoringMap,
                    matchViewModel = matchViewModel,
                    resumeViewModel = resumeViewModel
                )
                "CV Tester" -> CvTesterScreen(
                    scores = cvScores,
                    onNavigateToBuildSection = onNavigateToBuildSection
                )
            }
        }
    }
}

// --- ATS OPTIMIZER VIEW ---

@Composable
fun AtsOptimizerScreen(
    resume: Resume,
    atsResult: com.example.data.api.AtsAnalysisResponse?,
    isAnalyzing: Boolean,
    jobDescInput: String,
    tailoredBullets: Map<String, String>,
    isTailoringMap: Map<String, Boolean>,
    matchViewModel: MatchViewModel,
    resumeViewModel: ResumeViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (atsResult == null) {
            // Input Mode: Paste JD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Job Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Paste the job description you are targeting. Gemini AI will cross-analyze your resume, output your compatibility score, and help you tailor roles.",
                            fontSize = 12.sp,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = jobDescInput,
                            onValueChange = { matchViewModel.setJobDescription(it) },
                            label = { Text("Paste Job Description details...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .testTag("job_description_input"),
                            maxLines = 15
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { matchViewModel.analyzeAts(resume) },
                            enabled = jobDescInput.isNotBlank() && !isAnalyzing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ats_analyze_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Resume...")
                            } else {
                                Icon(Icons.Default.Analytics, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze & Optimize Compatibility")
                            }
                        }
                    }
                }
            }
        } else {
            // Results Mode
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATS Analysis Results",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { matchViewModel.clearAtsResult() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Check", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Score Rings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ATS Match Score Ring
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AnimatedScoreRing(score = atsResult.score, maxScore = 100)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("ATS Match Score", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Formatting Score Ring
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AnimatedScoreRing(score = atsResult.formattingScore, maxScore = 30)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Formatting Subscore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Keywords matching
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Keywords Analysis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Matched Keywords
                        Text("Matched Keywords (${atsResult.matchedKeywords.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                        if (atsResult.matchedKeywords.isEmpty()) {
                            Text("No keywords matched.", fontSize = 13.sp, color = TextGray, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                atsResult.matchedKeywords.forEach { word ->
                                    Box(
                                        modifier = Modifier
                                            .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(word, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Missing Keywords
                        Text("Missing Keywords (${atsResult.missingKeywords.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ErrorRed)
                        if (atsResult.missingKeywords.isEmpty()) {
                            Text("Excellent! No critical missing keywords.", fontSize = 13.sp, color = TextGray, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                atsResult.missingKeywords.forEach { word ->
                                    Box(
                                        modifier = Modifier
                                            .background(ErrorRed.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(word, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Suggestions List
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Optimization Suggestions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        atsResult.suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tailoring Section
            if (resume.experience.isNotEmpty()) {
                item {
                    Text(
                        text = "Tailor Your Experience Roles",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(resume.experience) { exp ->
                    var showDescriptionText by remember { mutableStateOf(exp.description) }
                    val isTailoring = isTailoringMap[exp.id] ?: false
                    val tailoredText = tailoredBullets[exp.id]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${exp.title} at ${exp.company}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exp.description,
                                fontSize = 13.sp,
                                color = TextGray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (tailoredText != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "TAILORED BULLETS BY GEMINI:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tailoredText,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            Button(
                                                onClick = {
                                                    resumeViewModel.updateExperience(exp.copy(description = tailoredText))
                                                },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Keep Tailored Bullets", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { matchViewModel.tailorBulletForJob(exp.id, exp.description) },
                                    enabled = !isTailoring,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("tailor_for_job_button_${exp.id}")
                                ) {
                                    if (isTailoring) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tailoring...")
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tailor for this job")
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

@Composable
fun AnimatedScoreRing(score: Int, maxScore: Int) {
    val percent = score.toFloat() / maxScore
    val stroke = 8.dp
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Track
            drawCircle(
                color = trackColor,
                radius = size.minDimension / 2 - stroke.toPx(),
                style = Stroke(width = stroke.toPx())
            )
            // Progress
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * percent,
                useCenter = false,
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/$maxScore",
                fontSize = 11.sp,
                color = TextGray
            )
        }
    }
}

// --- CV TESTER SCREEN VIEW ---

@Composable
fun CvTesterScreen(
    scores: CvTestScores,
    onNavigateToBuildSection: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Overall Grade Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Large Grade Badge
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scores.grade,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Resume Grade",
                            fontSize = 13.sp,
                            color = TextGray
                        )
                        Text(
                            text = "Your CV Score: ${scores.totalScore}/100",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                scores.totalScore >= 85 -> "Excellent work! Your resume is ready for submissions."
                                scores.totalScore >= 70 -> "Good foundation. Polish tips below to score an A+."
                                else -> "Needs attention. Resolve warnings to beat modern ATS systems."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 7 Categories Breakdown Cards
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scoring Dimensions Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ScoreCategoryRow("Contact Info", scores.contactInfoScore, 15, "Personal", onNavigateToBuildSection)
                    ScoreCategoryRow("Summary", scores.summaryScore, 10, "Personal", onNavigateToBuildSection)
                    ScoreCategoryRow("Experience", scores.experienceScore, 25, "Experience", onNavigateToBuildSection)
                    ScoreCategoryRow("Education", scores.educationScore, 10, "Education", onNavigateToBuildSection)
                    ScoreCategoryRow("Skills Library", scores.skillsScore, 15, "Skills", onNavigateToBuildSection)
                    ScoreCategoryRow("Projects & Extras", scores.extrasScore, 15, "Projects", onNavigateToBuildSection)
                    ScoreCategoryRow("Content Quality", scores.qualityScore, 10, "Experience", onNavigateToBuildSection)
                }
            }
        }

        // Actionable Tips with deep link Fixes
        if (scores.tips.isNotEmpty()) {
            item {
                Text(
                    text = "Actionable Improvement Tips (${scores.tips.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(scores.tips) { tip ->
                val targetSection = remember(tip) {
                    when {
                        tip.contains("name", ignoreCase = true) || tip.contains("email", ignoreCase = true) || tip.contains("phone", ignoreCase = true) || tip.contains("location", ignoreCase = true) || tip.contains("summary", ignoreCase = true) -> "Personal"
                        tip.contains("experience", ignoreCase = true) || tip.contains("job", ignoreCase = true) || tip.contains("history", ignoreCase = true) || tip.contains("bullet", ignoreCase = true) || tip.contains("describe", ignoreCase = true) || tip.contains("elaborate", ignoreCase = true) -> "Experience"
                        tip.contains("school", ignoreCase = true) || tip.contains("education", ignoreCase = true) || tip.contains("college", ignoreCase = true) || tip.contains("degree", ignoreCase = true) -> "Education"
                        tip.contains("skill", ignoreCase = true) -> "Skills"
                        tip.contains("project", ignoreCase = true) -> "Projects"
                        else -> "More"
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        TextButton(
                            onClick = { onNavigateToBuildSection(targetSection) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Fix", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCategoryRow(
    label: String,
    score: Int,
    maxScore: Int,
    sectionTag: String,
    onNavigate: (String) -> Unit
) {
    val progress = score.toFloat() / maxScore
    val barColor = when {
        progress >= 0.8f -> SuccessGreen
        progress >= 0.5f -> WarningOrange
        else -> ErrorRed
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$score / $maxScore pts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate to fix",
                    tint = TextGray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(12.dp)
                        .clickable { onNavigate(sectionTag) }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.12f)
        )
    }
}
