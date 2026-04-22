package com.equimaxlab.solver.ui.screen.calculator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.presentation.calculator.CalculatorViewModel
import com.equimaxlab.solver.ui.components.*
import com.equimaxlab.solver.ui.theme.*

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel { CalculatorViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    var showPickerFor by remember { mutableStateOf<PickerTarget?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Calculator",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GreenFeltLight
                ) {
                    Text(
                        text     = "10 000 sims",
                        color    = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Tus cartas
            SectionLabel("TUS CARTAS")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { i ->
                    val card = state.holeCards.getOrNull(i)
                    if (card != null) {
                        CardView(
                            card    = card,
                            size    = CardSize.LARGE,
                            onClick = { viewModel.removeHoleCard(card) }
                        )
                    } else {
                        EmptyCardSlot(
                            size    = CardSize.LARGE,
                            onClick = { showPickerFor = PickerTarget.HOLE }
                        )
                    }
                }
            }

            // Board
            SectionLabel("BOARD")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { i ->
                    val card = state.board.getOrNull(i)
                    if (card != null) {
                        CardView(
                            card    = card,
                            size    = CardSize.MEDIUM,
                            onClick = { viewModel.removeBoardCard(card) }
                        )
                    } else {
                        EmptyCardSlot(
                            size    = CardSize.MEDIUM,
                            onClick = {
                                if (state.board.size == i) showPickerFor = PickerTarget.BOARD
                            }
                        )
                    }
                }
            }

            // Rango rival
            SectionLabel("RANGO RIVAL (OPCIONAL)")
            OutlinedTextField(
                value         = state.opponentRange,
                onValueChange = { viewModel.setOpponentRange(it) },
                placeholder   = { Text("QQ+, AKs, AQo", color = TextSecondary) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    viewModel.calculate()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GreenAccent,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = GreenAccent,
                    focusedContainerColor   = GreenFeltLight,
                    unfocusedContainerColor = GreenFeltLight
                ),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón calcular
            Button(
                onClick  = { keyboard?.hide(); viewModel.calculate() },
                enabled  = state.holeCards.size == 2 && !state.isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "CALCULAR EQUIDAD",
                        color      = Color.Black,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Resultado de mano
            AnimatedVisibility(visible = state.handResult != null) {
                state.handResult?.let { hand ->
                    SectionLabel("MANO ACTUAL")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenFeltLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text       = hand.rank.displayName(),
                                color      = GoldAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
            }

            // Resultados de equidad
            AnimatedVisibility(visible = state.equityResult != null) {
                state.equityResult?.let { equity ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionLabel("EQUIDAD")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            EquityColumn(
                                value = equity.winPercent,
                                label = "VICTORIA",
                                color = GreenAccent
                            )
                            EquityColumn(
                                value = equity.tiePercent,
                                label = "EMPATE",
                                color = GoldAccent
                            )
                            EquityColumn(
                                value = equity.lossPercent,
                                label = "DERROTA",
                                color = RedAccent
                            )
                        }

                        // Barra de equidad
                        EquityBar(
                            win  = equity.winProbability.toFloat(),
                            tie  = equity.tieProbability.toFloat(),
                            loss = equity.lossProbability.toFloat()
                        )
                    }
                }
            }

            // Botón reset
            if (state.equityResult != null || state.holeCards.isNotEmpty()) {
                TextButton(
                    onClick  = { viewModel.reset() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Nueva mano", color = TextSecondary)
                }
            }

            Spacer(Modifier.height(80.dp)) // espacio para la bottom nav
        }
    }

    // Card picker dialog
    showPickerFor?.let { target ->
        val blocked = state.holeCards + state.board
        CardPickerDialog(
            blockedCards    = blocked,
            onCardSelected  = { card ->
                when (target) {
                    PickerTarget.HOLE  -> viewModel.addHoleCard(card)
                    PickerTarget.BOARD -> viewModel.addBoardCard(card)
                }
                showPickerFor = null
            },
            onDismiss = { showPickerFor = null }
        )
    }
}

// —— Helpers ——

private enum class PickerTarget { HOLE, BOARD }

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = TextSecondary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun EquityColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            color      = color,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun EquityBar(win: Float, tie: Float, loss: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
    ) {
        if (win > 0f) Box(
            modifier = Modifier
                .weight(win)
                .fillMaxHeight()
                .background(GreenAccent)
        )
        if (tie > 0f) Box(
            modifier = Modifier
                .weight(tie)
                .fillMaxHeight()
                .background(GoldAccent)
        )
        if (loss > 0f) Box(
            modifier = Modifier
                .weight(loss)
                .fillMaxHeight()
                .background(RedAccent)
        )
    }
}

private fun com.equimaxlab.solver.core.HandRank.displayName(): String = when (this) {
    com.equimaxlab.solver.core.HandRank.HIGH_CARD       -> "Carta alta"
    com.equimaxlab.solver.core.HandRank.ONE_PAIR        -> "Pareja"
    com.equimaxlab.solver.core.HandRank.TWO_PAIR        -> "Doble pareja"
    com.equimaxlab.solver.core.HandRank.THREE_OF_A_KIND -> "Trío"
    com.equimaxlab.solver.core.HandRank.STRAIGHT        -> "Escalera"
    com.equimaxlab.solver.core.HandRank.FLUSH           -> "Color"
    com.equimaxlab.solver.core.HandRank.FULL_HOUSE      -> "Full house"
    com.equimaxlab.solver.core.HandRank.FOUR_OF_A_KIND  -> "Póker"
    com.equimaxlab.solver.core.HandRank.STRAIGHT_FLUSH  -> "Escalera de color"
    com.equimaxlab.solver.core.HandRank.ROYAL_FLUSH     -> "Escalera real"
}