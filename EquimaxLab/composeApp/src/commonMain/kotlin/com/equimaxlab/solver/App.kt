package com.equimaxlab.solver

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equimaxlab.solver.presentation.trainer.TrainerViewModel
import com.equimaxlab.solver.ui.navigation.PokerBottomNav
import com.equimaxlab.solver.ui.navigation.Screen
import com.equimaxlab.solver.ui.screen.calculator.CalculatorScreen
import com.equimaxlab.solver.ui.screen.replayer.ReplayerScreen
import com.equimaxlab.solver.ui.screen.trainer.TrainerConfigScreen
import com.equimaxlab.solver.ui.screen.trainer.TrainerScreen
import com.equimaxlab.solver.ui.theme.PokerLabTheme

@Composable
fun App() {
    PokerLabTheme {
        var current        by remember { mutableStateOf(Screen.CALCULATOR) }
        var trainerStarted by remember { mutableStateOf(false) }
        val trainerVm      = viewModel { TrainerViewModel() }

        Scaffold(
            bottomBar = {
                PokerBottomNav(
                    current   = current,
                    onSelect  = {
                        current = it
                        if (it == Screen.TRAINER) trainerStarted = false
                    }
                )
            }
        ) { _ ->
            when (current) {
                Screen.CALCULATOR -> CalculatorScreen()
                Screen.REPLAYER   -> ReplayerScreen()
                Screen.TRAINER    -> {
                    if (!trainerStarted) {
                        TrainerConfigScreen(
                            onStart = { config ->
                                trainerVm.startWithConfig(config)
                                trainerStarted = true
                            }
                        )
                    } else {
                        TrainerScreen(viewModel = trainerVm)
                    }
                }
            }
        }
    }
}