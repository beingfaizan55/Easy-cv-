package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AccountViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.MatchViewModel
import com.example.ui.viewmodel.ResumeViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val resumeViewModel: ResumeViewModel = viewModel()
        val accountViewModel: AccountViewModel = viewModel()
        val jobViewModel: JobViewModel = viewModel()
        val matchViewModel: MatchViewModel = viewModel()

        MainScreen(
          resumeViewModel = resumeViewModel,
          accountViewModel = accountViewModel,
          jobViewModel = jobViewModel,
          matchViewModel = matchViewModel
        )
      }
    }
  }
}

