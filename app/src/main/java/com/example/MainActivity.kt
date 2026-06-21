package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.QnnViewModel
import com.example.ui.screens.DocScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val qnnViewModel: QnnViewModel by viewModels()

    enum class Screen {
        SPLASH, MAIN, DOCS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 400),
                    modifier = Modifier.fillMaxSize(),
                    label = "screen_navigation"
                ) { screen ->
                    when (screen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                onTransition = {
                                    currentScreen = Screen.MAIN
                                }
                            )
                        }
                        Screen.MAIN -> {
                            MainScreen(
                                viewModel = qnnViewModel,
                                onNavigateToDocs = {
                                    currentScreen = Screen.DOCS
                                }
                            )
                        }
                        Screen.DOCS -> {
                            DocScreen(
                                onBack = {
                                    currentScreen = Screen.MAIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
