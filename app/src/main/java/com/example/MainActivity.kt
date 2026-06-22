package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.GameRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameScreen
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val coroutineScope = rememberCoroutineScope()
        
        // Initialize Room Local Storage
        val database = AppDatabase.getDatabase(applicationContext, coroutineScope)
        val repository = GameRepository(database.userProgressDao())
        
        // Create Game View Model
        val viewModel = ViewModelProvider(
          this,
          GameViewModelFactory(repository)
        )[GameViewModel::class.java]

        val currentScreen by viewModel.currentScreen.collectAsState()

        // Handle physical hardware back clicks hygienically
        BackHandler(enabled = currentScreen != GameScreen.Home) {
          viewModel.handleBackPress()
        }

        // Animate smooth navigation transitions using a Crossfade
        Crossfade(
          targetState = currentScreen,
          modifier = Modifier.fillMaxSize(),
          label = "ScreenTransitions"
        ) { screen ->
          when (screen) {
            GameScreen.Home -> {
              HomeScreen(viewModel = viewModel)
            }
            GameScreen.LevelSelect -> {
              LevelSelectScreen(
                viewModel = viewModel,
                onBack = { viewModel.handleBackPress() }
              )
            }
            GameScreen.Gameplay -> {
              GameplayScreen(
                viewModel = viewModel,
                onBack = { viewModel.handleBackPress() }
              )
            }
            GameScreen.Leaderboard -> {
              LeaderboardScreen(
                viewModel = viewModel,
                onBack = { viewModel.handleBackPress() }
              )
            }
            GameScreen.DailyChallenge -> {
              DailyChallengeScreen(
                viewModel = viewModel,
                onBack = { viewModel.handleBackPress() }
              )
            }
          }
        }
      }
    }
  }
}
