package com.equimaxlab.solver.ui.screen.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equimaxlab.solver.domain.model.PokerAction
import com.equimaxlab.solver.domain.model.Position
import com.equimaxlab.solver.domain.model.StackDepth
import com.equimaxlab.solver.presentation.trainer.ActionStep
import com.equimaxlab.solver.presentation.trainer.DecisionScore
import com.equimaxlab.solver.presentation.trainer.ScoreStats
import com.equimaxlab.solver.presentation.trainer.TrainerViewModel
import com.equimaxlab.solver.ui.components.CardSize
import com.equimaxlab.solver.ui.components.CardView
import com.equimaxlab.solver.ui.components.PokerTable
import com.equimaxlab.solver.ui.theme.*

@Composable
fun TrainerScreen(
    viewModel: TrainerViewModel = viewModel { TrainerViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Trainer",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 16.sp)
                    Text(
                        text       = "${state.streak}",
                        color      = GoldAccent,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("racha", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // Stats bar
            if (state.totalAnswered > 0) {
                StatsBar(stats = state.scoreStats)
            }

            if (state.isLoading) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent, strokeWidth = 2.dp)
                }
            } else {
                state.scenario?.let { scenario ->

                    // Tarjeta de escenario
                    Surface(shape = RoundedCornerShape(12.dp), color = GreenFeltLight) {
                        Column(modifier = Modifier.padding(14.dp)) {

                            // Badges posición + stack
                            // Mesa visual + badges
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                PokerTable(
                                    heroPosition    = scenario.position,
                                    activePositions = state.actionSequence
                                        .map { it.position }
                                        .toSet(),
                                    tableSize       = 220.dp,
                                    modifier        = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ScenarioBadge(scenario.position.displayName())
                                    ScenarioBadge(scenario.stackDepth.displayName())
                                }
                            }

                            // Tus cartas
                            Text(
                                "TUS CARTAS",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                scenario.holeCards.forEach { card ->
                                    CardView(card = card, size = CardSize.LARGE)
                                }
                            }

                            // Board
                            if (scenario.board.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = when (scenario.board.size) {
                                        3 -> "FLOP"; 4 -> "TURN"; else -> "RIVER"
                                    },
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = TextSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    scenario.board.forEach { card ->
                                        CardView(card = card, size = CardSize.MEDIUM)
                                    }
                                }
                            }

                            // Secuencia preflop
                            if (state.actionSequence.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                SequenceBlock("PREFLOP", state.actionSequence)
                            }

                            // Historial calles
                            state.streetHistory.forEach { street ->
                                Spacer(Modifier.height(8.dp))
                                SequenceBlock(street.streetName.uppercase(), street.actions)
                            }

                            // Pot odds + equidad mínima
                            Spacer(Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = GreenFelt) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Pot odds", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "${(scenario.potOdds * 100).toInt()}%",
                                            color      = TextPrimary,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Equity mínima para call", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "${((state.requiredEquity ?: 0.0) * 100).toInt()}%",
                                            color      = GoldAccent,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Bote", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            "${state.potSizeBb}bb",
                                            color      = TextPrimary,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pregunta
                    Text(
                        "¿CUÁL ES TU ACCIÓN?",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    // Botones con bb
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(PokerAction.FOLD, PokerAction.CALL, PokerAction.RAISE).forEach { action ->
                            val isSelected = state.selectedAction == action
                            val isCorrect  = state.correctAction == action
                            val answered   = state.isAnswered
                            val score      = state.decisionScore

                            val containerColor = when {
                                answered && isCorrect  -> scoreColor(score ?: DecisionScore.CORRECT).copy(alpha = 0.15f)
                                answered && isSelected && !isCorrect -> RedAccent.copy(alpha = 0.15f)
                                else -> GreenFeltLight
                            }
                            val borderColor = when {
                                answered && isCorrect  -> scoreColor(score ?: DecisionScore.CORRECT)
                                answered && isSelected && !isCorrect -> RedAccent
                                else -> DividerColor
                            }
                            val textColor = when {
                                answered && isCorrect  -> scoreColor(score ?: DecisionScore.CORRECT)
                                answered && isSelected && !isCorrect -> RedAccent
                                else -> TextPrimary
                            }

                            Surface(
                                onClick  = { if (!answered) viewModel.answer(action) },
                                shape    = RoundedCornerShape(10.dp),
                                color    = containerColor,
                                border   = BorderStroke(1.dp, borderColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text       = action.displayName(),
                                        color      = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 13.sp
                                    )
                                    Text(
                                        text  = when (action) {
                                            PokerAction.FOLD  -> "−"
                                            PokerAction.CALL  -> "${state.callSizeBb}bb"
                                            PokerAction.RAISE -> "${state.raiseSizeBb}bb"
                                            else              -> ""
                                        },
                                        color    = textColor.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Feedback de decisión
                    AnimatedVisibility(
                        visible = state.isAnswered,
                        enter   = fadeIn() + slideInVertically()
                    ) {
                        state.decisionScore?.let { score ->
                            DecisionFeedback(
                                score         = score,
                                correctAction = state.correctAction,
                                equity        = state.equity,
                                required      = state.requiredEquity
                            )
                        }
                    }

                    // Botón siguiente
                    AnimatedVisibility(visible = state.isAnswered) {
                        Button(
                            onClick  = { viewModel.nextScenario() },
                            colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape    = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(
                                "SIGUIENTE MANO",
                                color         = Color.Black,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize      = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ---- Componentes ----

@Composable
private fun StatsBar(stats: ScoreStats) {
    Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Precisión: ${stats.accuracyPercent}%",
                    color      = GreenAccent,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${stats.total} manos",
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))

            // Barra de desglose
            if (stats.total > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    val total = stats.total.toFloat()
                    if (stats.best > 0)    Box(Modifier.weight(stats.best / total).fillMaxHeight().background(GreenAccent))
                    if (stats.correct > 0) Box(Modifier.weight(stats.correct / total).fillMaxHeight().background(Color(0xFF27AE60)))
                    if (stats.inexact > 0) Box(Modifier.weight(stats.inexact / total).fillMaxHeight().background(GoldAccent))
                    if (stats.bad > 0)     Box(Modifier.weight(stats.bad / total).fillMaxHeight().background(Color(0xFFE67E22)))
                    if (stats.blunder > 0) Box(Modifier.weight(stats.blunder / total).fillMaxHeight().background(RedAccent))
                }
                Spacer(Modifier.height(8.dp))

                // Leyenda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatChip("MEJOR", stats.best, GreenAccent)
                    StatChip("OK", stats.correct, Color(0xFF27AE60))
                    StatChip("INEXACTO", stats.inexact, GoldAccent)
                    StatChip("MAL", stats.bad, Color(0xFFE67E22))
                    StatChip("TORPE", stats.blunder, RedAccent)
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = "$count",
            color      = color,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(label, color = TextSecondary, fontSize = 8.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun DecisionFeedback(
    score: DecisionScore,
    correctAction: PokerAction?,
    equity: Double?,
    required: Double?
) {
    val (bg, border, emoji, title) = when (score) {
        DecisionScore.BEST    -> Quadruple(GreenAccent.copy(0.12f), GreenAccent, "✦", "Mejor jugada")
        DecisionScore.CORRECT -> Quadruple(Color(0xFF27AE60).copy(0.12f), Color(0xFF27AE60), "✓", "Correcto")
        DecisionScore.INEXACT -> Quadruple(GoldAccent.copy(0.12f), GoldAccent, "~", "Inexacto")
        DecisionScore.BAD     -> Quadruple(Color(0xFFE67E22).copy(0.12f), Color(0xFFE67E22), "✗", "Mal")
        DecisionScore.BLUNDER -> Quadruple(RedAccent.copy(0.12f), RedAccent, "✗✗", "Error grave")
    }

    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 18.sp, color = border)
                Text(
                    title,
                    color      = border,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
                correctAction?.let {
                    Text(
                        "· Correcto: ${it.displayName()}",
                        color    = border.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            equity?.let { eq ->
                Text(
                    "Tu equity: ${(eq * 100).toInt()}%  ·  Mínimo para call: ${((required ?: 0.0) * 100).toInt()}%",
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SequenceBlock(title: String, steps: List<ActionStep>) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelSmall,
        color    = TextSecondary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Surface(shape = RoundedCornerShape(6.dp), color = GreenFelt) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = step.position.displayName(),
                        color      = if (step.isHero) GoldAccent else TextSecondary,
                        fontSize   = 11.sp,
                        fontWeight = if (step.isHero) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text       = step.action,
                        color      = if (step.isHero) GoldAccent else TextPrimary,
                        fontSize   = 11.sp,
                        fontWeight = if (step.isHero) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioBadge(text: String) {
    Surface(shape = RoundedCornerShape(4.dp), color = GreenFelt) {
        Text(
            text          = text,
            color         = GoldAccent,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun scoreColor(score: DecisionScore): Color = when (score) {
    DecisionScore.BEST    -> Color(0xFF2ECC71)
    DecisionScore.CORRECT -> Color(0xFF27AE60)
    DecisionScore.INEXACT -> Color(0xFFE8C84A)
    DecisionScore.BAD     -> Color(0xFFE67E22)
    DecisionScore.BLUNDER -> Color(0xFFE74C3C)
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

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

private fun StackDepth.displayName() = when (this) {
    StackDepth.SHORT     -> "< 20bb"
    StackDepth.MEDIUM    -> "20-40bb"
    StackDepth.DEEP      -> "40-100bb"
    StackDepth.VERY_DEEP -> "> 100bb"
}

private fun PokerAction.displayName() = when (this) {
    PokerAction.FOLD          -> "Fold"
    PokerAction.CALL          -> "Call"
    PokerAction.RAISE         -> "Raise"
    PokerAction.THREE_BET     -> "3-Bet"
    PokerAction.FOUR_BET_PLUS -> "4-Bet+"
    PokerAction.CHECK         -> "Check"
    PokerAction.BET           -> "Bet"
}