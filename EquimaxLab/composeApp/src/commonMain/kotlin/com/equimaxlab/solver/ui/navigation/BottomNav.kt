package com.equimaxlab.solver.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equimaxlab.solver.ui.theme.*

enum class Screen { CALCULATOR, TRAINER, REPLAYER }

@Composable
fun PokerBottomNav(
    current: Screen,
    onSelect: (Screen) -> Unit
) {
    NavigationBar(
        containerColor    = GreenFelt,
        tonalElevation    = 0.dp
    ) {
        listOf(
            Triple(Screen.CALCULATOR, "Calc",    "⊞"),
            Triple(Screen.TRAINER,    "Trainer", "◈"),
            Triple(Screen.REPLAYER,   "Replayer","▷")
        ).forEach { (screen, label, icon) ->
            val selected = current == screen
            NavigationBarItem(
                selected = selected,
                onClick  = { onSelect(screen) },
                icon     = {
                    Text(icon, fontSize = 18.sp, color = if (selected) GreenAccent else TextSecondary)
                },
                label    = {
                    Text(label, fontSize = 10.sp, color = if (selected) GreenAccent else TextSecondary)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}