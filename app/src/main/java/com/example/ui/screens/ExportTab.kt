package com.example.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Resume
import com.example.ui.theme.*
import com.example.ui.viewmodel.ResumeViewModel

private val TemplateList = listOf(
    "Minimalist" to "Clean layout with subtle spacing and high readability.",
    "Professional" to "Traditional corporate layout with structured headers.",
    "Executive" to "High-impact layout with custom summary block.",
    "Creative" to "Left-aligned sidebar layout highlighting major skills.",
    "Tech" to "Clean developer-focused aesthetic using monospace elements.",
    "Modernist" to "Elegant modern grid system featuring bold name titles.",
    "Academic" to "Comprehensive citation-ready detailed research structure.",
    "Slate" to "Dark accent block styling highlighting career achievements.",
    "Warm Retro" to "Stylized warm-tint background with serif headlines.",
    "Compact" to "High-density single-page optimizer for junior roles."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportTab(
    viewModel: ResumeViewModel,
    modifier: Modifier = Modifier
) {
    val resume by viewModel.resumeState.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    var selectedTemplate by remember { mutableStateOf("Professional") }

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
                    text = "Export & Print CV",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Template Selector Row
            item {
                Text(
                    text = "Select Resume Design Template",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TemplateList) { (template, desc) ->
                        val isSelected = selectedTemplate == template
                        val accentHex = resume.accentColor
                        val accentColor = remember(accentHex) { Color(android.graphics.Color.parseColor(accentHex)) }
                        
                        val borderCol = if (isSelected) accentColor else LightGrayBorder
                        val bg = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderCol),
                            modifier = Modifier
                                .width(130.dp)
                                .height(74.dp)
                                .clickable { selectedTemplate = template }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = template,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (template) {
                                        "Minimalist" -> "Modern Minimal"
                                        "Professional" -> "Classic Corp"
                                        "Executive" -> "Lead Executive"
                                        "Tech" -> "Code Monospace"
                                        else -> "Popular Style"
                                    },
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // High Fidelity Live Visual Preview
            item {
                Text(
                    text = "Live Visual Preview ($selectedTemplate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        LiveTemplatePreview(
                            templateName = selectedTemplate,
                            resume = resume
                        )
                    }
                }
            }

            // Export trigger buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val html = generateTemplateHtml(selectedTemplate, resume)
                            printHtmlResume(context, html, resume.personalInfo.fullName)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pdf_export_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF / Print", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val textRepresentation = buildString {
                                appendLine("Easy CV - ${resume.personalInfo.fullName}")
                                appendLine("Title: ${resume.personalInfo.fullName}")
                                appendLine("Summary: ${resume.personalInfo.summary}")
                                appendLine("Experience Count: ${resume.experience.size}")
                                appendLine("Skills: ${resume.skills.joinToString(", ")}")
                            }
                            // Trigger native text share intent
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, textRepresentation)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share CV details via"))
                        },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("share_cv_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTemplatePreview(
    templateName: String,
    resume: Resume
) {
    val info = resume.personalInfo
    val accentHex = resume.accentColor
    val accentColor = remember(accentHex) { Color(android.graphics.Color.parseColor(accentHex)) }

    val nameText = if (info.fullName.isBlank()) "YOUR FULL NAME" else info.fullName
    val summaryText = if (info.summary.isBlank()) "Write a summary detailing your outcomes and experience." else info.summary

    Column(modifier = Modifier.fillMaxWidth()) {
        when (templateName) {
            "Minimalist" -> {
                // Minimalist visual
                Column(horizontalAlignment = Alignment.Start) {
                    Text(nameText.uppercase(), fontWeight = FontWeight.Light, letterSpacing = 2.sp, fontSize = 20.sp, color = accentColor)
                    Text("${info.email} | ${info.phone} | ${info.location}", fontSize = 11.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = accentColor.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("PROFILE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = accentColor, letterSpacing = 1.sp)
                    Text(summaryText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            "Professional" -> {
                // Professional Dual colored Header
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(nameText, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${info.email} • ${info.phone}", fontSize = 11.sp, color = TextGray)
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = accentColor, thickness = 2.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("SUMMARY", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = accentColor)
                    Text(summaryText, fontSize = 12.sp)
                }
            }

            "Tech" -> {
                // Tech coding brackets visual
                Column {
                    Text(text = "class Candidate {", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = accentColor)
                    Row {
                        Text(text = "  val name = ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextGray)
                        Text(text = "\"$nameText\"", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    Row {
                        Text(text = "  val summary = ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextGray)
                        Text(text = "\"${summaryText.take(60)}...\"", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = SuccessGreen)
                    }
                    Text(text = "}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = accentColor)
                }
            }

            "Warm Retro" -> {
                // Warm Retro style serif headings
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFBF7), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(nameText, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Color(0xFF452D12), fontStyle = FontStyle.Italic)
                    Text("${info.email} | ${info.location}", fontSize = 11.sp, color = Color(0xFF6B5843))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SUMMARY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF452D12))
                    Text(summaryText, fontSize = 12.sp, color = Color(0xFF3B3025))
                }
            }

            else -> {
                // Default Standard visual block
                Column {
                    Text(nameText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = accentColor)
                    Text("${info.email} | ${info.phone}", fontSize = 11.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = 0.06f))
                            .padding(8.dp)
                    ) {
                        Text(summaryText, fontSize = 12.sp)
                    }
                }
            }
        }

        // Render Experience in preview
        if (resume.experience.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "EXPERIENCE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            resume.experience.take(2).forEach { exp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(exp.company, fontSize = 11.sp, color = TextGray)
                        Text(exp.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Text("${exp.startDate} - ${if (exp.isCurrent) "Pres" else exp.endDate}", fontSize = 11.sp, color = TextGray)
                }
            }
        }

        // Render Education in preview
        if (resume.education.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "EDUCATION",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            resume.education.take(1).forEach { edu ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${edu.degree} in ${edu.fieldOfStudy}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(edu.school, fontSize = 11.sp, color = TextGray)
                    }
                    Text("${edu.startDate} - ${edu.endDate}", fontSize = 11.sp, color = TextGray)
                }
            }
        }

        // Render Skills in preview
        if (resume.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SKILLS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = resume.skills.take(8).joinToString(", "),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Native Android WebView Print layout adapter helper ---

private fun printHtmlResume(context: Context, html: String, candidateName: String) {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Easy_CV_${candidateName.replace(" ", "_")}.pdf")
                val jobName = "Easy CV Print - $candidateName"
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to print CV.", Toast.LENGTH_SHORT).show()
    }
}

// --- HTML Template Generator ---

fun generateTemplateHtml(template: String, resume: Resume): String {
    val info = resume.personalInfo
    val hex = resume.accentColor

    val name = if (info.fullName.isBlank()) "Your Full Name" else info.fullName
    val summary = if (info.summary.isBlank()) "Career summary details." else info.summary

    // Dynamic style selections
    val fontStyle = when (template) {
        "Tech" -> "font-family: 'Courier New', Courier, monospace;"
        "Warm Retro" -> "font-family: Georgia, serif; background-color: #FCFBF7;"
        else -> "font-family: Arial, sans-serif;"
    }

    val contentHtml = buildString {
        append("<h2>Experience</h2>")
        resume.experience.forEach { exp ->
            append("""
                <div class="item">
                    <div style="display: flex; justify-content: space-between; font-weight: bold;">
                        <span>${exp.title}</span>
                        <span>${exp.startDate} - ${if (exp.isCurrent) "Present" else exp.endDate}</span>
                    </div>
                    <div style="font-style: italic; color: #555;">${exp.company}</div>
                    <div style="margin-top: 5px;">${exp.description.replace("\n", "<br>")}</div>
                </div>
            """.trimIndent())
        }

        if (resume.education.isNotEmpty()) {
            append("<h2>Education</h2>")
            resume.education.forEach { edu ->
                append("""
                    <div class="item">
                        <div style="display: flex; justify-content: space-between; font-weight: bold;">
                            <span>${edu.degree} in ${edu.fieldOfStudy}</span>
                            <span>${edu.startDate} - ${edu.endDate}</span>
                        </div>
                        <div style="font-style: italic; color: #555;">${edu.school}</div>
                        ${if (edu.gpa.isNotBlank()) "<div>GPA: ${edu.gpa}</div>" else ""}
                    </div>
                """.trimIndent())
            }
        }

        if (resume.skills.isNotEmpty()) {
            append("<h2>Skills</h2>")
            append("<div style='margin-bottom: 15px;'>${resume.skills.joinToString(", ")}</div>")
        }

        if (resume.projects.isNotEmpty()) {
            append("<h2>Projects</h2>")
            resume.projects.forEach { proj ->
                append("""
                    <div class="item">
                        <div style="font-weight: bold;">${proj.title} ${if (proj.url.isNotBlank()) "(${proj.url})" else ""}</div>
                        <div style="font-style: italic; font-size: 0.9em; color: #444;">${proj.techStack}</div>
                        <div>${proj.description}</div>
                    </div>
                """.trimIndent())
            }
        }

        if (resume.certifications.isNotEmpty() || resume.languages.isNotEmpty()) {
            append("<h2>Certifications & Languages</h2>")
            resume.certifications.forEach { cert ->
                append("<div><strong>${cert.name}</strong> - ${cert.issuer} (${cert.date})</div>")
            }
            if (resume.languages.isNotEmpty()) {
                append("<div style='margin-top: 8px;'><strong>Languages:</strong> ")
                append(resume.languages.joinToString(", ") { "${it.name} (${it.proficiency})" })
                append("</div>")
            }
        }
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body {
                    padding: 30px;
                    color: #333;
                    line-height: 1.4;
                    font-size: 14px;
                    $fontStyle
                }
                h1 {
                    color: $hex;
                    margin-bottom: 5px;
                    font-size: 26px;
                }
                h2 {
                    color: $hex;
                    border-bottom: 2px solid $hex;
                    padding-bottom: 4px;
                    font-size: 18px;
                    margin-top: 25px;
                    margin-bottom: 12px;
                    text-transform: uppercase;
                }
                .contact {
                    color: #666;
                    margin-bottom: 20px;
                    font-size: 12px;
                }
                .item {
                    margin-bottom: 15px;
                }
            </style>
        </head>
        <body>
            <h1>$name</h1>
            <div class="contact">
                ${info.email} | ${info.phone} | ${info.location}
                ${if (info.linkedin.isNotBlank()) " | " + info.linkedin else ""}
                ${if (info.website.isNotBlank()) " | " + info.website else ""}
            </div>
            
            <p>${summary.replace("\n", "<br>")}</p>
            
            $contentHtml
        </body>
        </html>
    """.trimIndent()
}
