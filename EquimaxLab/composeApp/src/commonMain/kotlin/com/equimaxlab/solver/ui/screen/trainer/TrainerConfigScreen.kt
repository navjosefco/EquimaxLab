package com.equimaxlab.solver.ui.screen.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equimaxlab.solver.domain.model.Position
import com.equimaxlab.solver.domain.model.StackDepth
import com.equimaxlab.solver.presentation.trainer.PreflopAction
import com.equimaxlab.solver.presentation.trainer.StreetFilter
import com.equimaxlab.solver.presentation.trainer.TrainerConfig
import com.equimaxlab.solver.ui.theme.*

@Composable
fun TrainerConfigScreen(
    onStart: (TrainerConfig) -> Unit
) {
    var selectedPositions by remember { mutableStateOf(Position.entries.toSet()) }
    var selectedStreet    by remember { mutableStateOf(StreetFilter.ANY) }
    var selectedPreflop   by remember { mutableStateOf(PreflopAction.ANY) }
    var selectedStack     by remember { mutableStateOf<StackDepth?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenFelt)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Header
            Text(
                "Configurar sesión",
                style      = MaterialTheme.typography.headlineMedium,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // Posiciones
            ConfigSection(title = "POSICIÓN") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Position.entries.forEach { pos ->
                        val selected = pos in selectedPositions
                        FilterChip(
                            selected = selected,
                            label    = pos.displayName(),
                            onClick  = {
                                selectedPositions = if (selected && selectedPositions.size > 1)
                                    selectedPositions - pos
                                else
                                    selectedPositions + pos
                            }
                        )
                    }
                }
            }

            // Calle
            ConfigSection(title = "CALLE") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StreetFilter.entries.forEach { street ->
                        FilterChip(
                            selected = selectedStreet == street,
                            label    = street.displayName(),
                            onClick  = { selectedStreet = street }
                        )
                    }
                }
            }

            // Acción preflop
            ConfigSection(title = "ACCIÓN PREFLOP") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreflopAction.entries.forEach { action ->
                        FilterChip(
                            selected = selectedPreflop == action,
                            label    = action.displayName(),
                            onClick  = { selectedPreflop = action }
                        )
                    }
                }
            }

            // Stack depth
            ConfigSection(title = "STACK") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedStack == null,
                        label    = "Cualquiera",
                        onClick  = { selectedStack = null }
                    )
                    StackDepth.entries.forEach { depth ->
                        FilterChip(
                            selected = selectedStack == depth,
                            label    = depth.displayName(),
                            onClick  = { selectedStack = depth }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Botón empezar
            Button(
                onClick = {
                    onStart(
                        TrainerConfig(
                            positions     = selectedPositions,
                            street        = selectedStreet,
                            preflopAction = selectedPreflop,
                            stackDepth    = selectedStack
                        )
                    )
                },
                colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "EMPEZAR A ENTRENAR",
                    color         = Color.Black,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = GreenFeltLight
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(20.dp),
        color   = if (selected) GreenAccent else GreenFelt,
        border  = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) GreenAccent else DividerColor
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text       = label,
            color      = if (selected) Color.Black else TextSecondary,
            fontSize   = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun Position.displayName() = when (this) {
    Position.UTG         -> "UTG"
    Position.UTG1        -> "UTG+1"
    Position.UTG2        -> "UTG+2"
    Position.LOJACK      -> "LJ"
    Position.HIJACK      -> "HJ"
    Position.CUTOFF      -> "CO"
    Position.BUTTON      -> "BTN"
    Position.SMALL_BLIND -> "SB"
    Position.BIG_BLIND   -> "BB"
}

private fun StreetFilter.displayName() = when (this) {
    StreetFilter.ANY     -> "Cualquiera"
    StreetFilter.PREFLOP -> "Preflop"
    StreetFilter.FLOP    -> "Flop"
    StreetFilter.TURN    -> "Turn"
    StreetFilter.RIVER   -> "River"
}

private fun PreflopAction.displayName() = when (this) {
    PreflopAction.ANY      -> "Cualquiera"
    PreflopAction.SRP      -> "SRP"
    PreflopAction.THREE_BET -> "3-Bet"
    PreflopAction.FOUR_BET -> "4-Bet"
    PreflopAction.LIMP     -> "Limp"
}

private fun StackDepth.displayName() = when (this) {
    StackDepth.SHORT     -> "< 20bb"
    StackDepth.MEDIUM    -> "20-40bb"
    StackDepth.DEEP      -> "40-100bb"
    StackDepth.VERY_DEEP -> "> 100bb"
}