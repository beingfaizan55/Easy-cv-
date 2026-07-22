package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.AccountViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.MatchViewModel
import com.example.ui.viewmodel.ResumeViewModel

@Composable
fun MainScreen(
    resumeViewModel: ResumeViewModel,
    accountViewModel: AccountViewModel,
    jobViewModel: JobViewModel,
    matchViewModel: MatchViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }

    val navItems = listOf(
        NavigationItem("Build", Icons.Default.Edit, "build_tab_button"),
        NavigationItem("Match", Icons.Default.Analytics, "match_tab_button"),
        NavigationItem("Jobs", Icons.Default.Work, "jobs_tab_button"),
        NavigationItem("Account", Icons.Default.AccountCircle, "account_tab_button"),
        NavigationItem("Export", Icons.Default.PictureAsPdf, "export_tab_button")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> BuildTab(
                    viewModel = resumeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> MatchTab(
                    resumeViewModel = resumeViewModel,
                    matchViewModel = matchViewModel,
                    onNavigateToBuildSection = { section ->
                        resumeViewModel.setActiveBuildSection(section)
                        selectedTab = 0
                    },
                    modifier = Modifier.fillMaxSize()
                )
                2 -> JobsTab(
                    jobViewModel = jobViewModel,
                    accountViewModel = accountViewModel,
                    resumeViewModel = resumeViewModel,
                    onNavigateToAccount = { selectedTab = 3 },
                    modifier = Modifier.fillMaxSize()
                )
                3 -> AccountTab(
                    viewModel = accountViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                4 -> ExportTab(
                    viewModel = resumeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
