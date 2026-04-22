package com.equimaxlab.solver.presentation.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equimaxlab.solver.core.Deck
import com.equimaxlab.solver.domain.model.PokerAction
import com.equimaxlab.solver.domain.model.Position
import com.equimaxlab.solver.domain.model.StackDepth
import com.equimaxlab.solver.domain.model.TrainingScenario
import com.equimaxlab.solver.domain.model.TrainingSession
import com.equimaxlab.solver.domain.usecase.CalculateEquityUseCase
import com.equimaxlab.solver.domain.usecase.SaveSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class DecisionScore {
    BEST, CORRECT, INEXACT, BAD, BLUNDER
}

data class ActionStep(
    val position: Position,
    val action: String,
    val isHero: Boolean = false
)

data class StreetAction(
    val streetName: String,
    val actions: List<ActionStep>
)

data class ScoreStats(
    val best: Int    = 0,
    val correct: Int = 0,
    val inexact: Int = 0,
    val bad: Int     = 0,
    val blunder: Int = 0
) {
    val total get() = best + correct + inexact + bad + blunder
    val accuracyPercent get() = if (total == 0) 0
    else ((best + correct).toDouble() / total * 100).toInt()
}

data class TrainerUiState(
    val config: TrainerConfig? = null,
    val scenario: TrainingScenario? = null,
    val actionSequence: List<ActionStep> = emptyList(),
    val streetHistory: List<StreetAction> = emptyList(),
    val selectedAction: PokerAction? = null,
    val correctAction: PokerAction? = null,
    val decisionScore: DecisionScore? = null,
    val requiredEquity: Double? = null,
    val equity: Double? = null,
    val potSizeBb: Double = 0.0,
    val callSizeBb: Double = 0.0,
    val raiseSizeBb: Double = 0.0,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean? = null,
    val streak: Int = 0,
    val totalAnswered: Int = 0,
    val scoreStats: ScoreStats = ScoreStats(),
    val isLoading: Boolean = false
)

class TrainerViewModel(
    private val calculateEquity: CalculateEquityUseCase = CalculateEquityUseCase(),
    private val saveSession: SaveSessionUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerUiState())
    val uiState: StateFlow<TrainerUiState> = _uiState.asStateFlow()

    fun startWithConfig(config: TrainerConfig) {
        _uiState.update { it.copy(config = config) }
        nextScenario()
    }

    fun nextScenario() {
        val config = _uiState.value.config ?: TrainerConfig()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val scenario  = withContext(Dispatchers.Default) { generateScenario(config) }
            val equity    = withContext(Dispatchers.Default) {
                calculateEquity(
                    holeCards  = scenario.holeCards,
                    board      = scenario.board,
                    iterations = 2000
                )
            }

            // Fix 1: pot odds realistas máximo 50%
            val required  = scenario.potOdds / (1 + scenario.potOdds)
            val correct   = determineCorrectAction(equity.winProbability, required)
            val preflop   = withContext(Dispatchers.Default) {
                generatePreflopSequence(scenario.position)
            }
            val streets   = withContext(Dispatchers.Default) {
                generateStreetHistory(scenario)
            }

            val potSize   = listOf(3.0, 4.5, 6.0, 8.0, 10.0).random()
            val callSize  = ((potSize * scenario.potOdds) * 2).toInt() / 2.0
            val raiseSize = (potSize * 2.5 * 2).toInt() / 2.0

            _uiState.update {
                it.copy(
                    scenario       = scenario,
                    actionSequence = preflop,
                    streetHistory  = streets,
                    correctAction  = correct,
                    requiredEquity = required,
                    equity         = equity.winProbability,
                    potSizeBb      = potSize,
                    callSizeBb     = callSize,
                    raiseSizeBb    = raiseSize,
                    selectedAction = null,
                    decisionScore  = null,
                    isAnswered     = false,
                    isCorrect      = null,
                    isLoading      = false
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun answer(action: PokerAction) {
        val state = _uiState.value
        if (state.isAnswered || state.scenario == null) return

        val equity    = state.equity ?: 0.0
        val required  = state.requiredEquity ?: 0.5
        val score     = evaluateDecision(action, equity, required)
        val isCorrect = action == state.correctAction

        val updatedStats = when (score) {
            DecisionScore.BEST    -> state.scoreStats.copy(best    = state.scoreStats.best + 1)
            DecisionScore.CORRECT -> state.scoreStats.copy(correct = state.scoreStats.correct + 1)
            DecisionScore.INEXACT -> state.scoreStats.copy(inexact = state.scoreStats.inexact + 1)
            DecisionScore.BAD     -> state.scoreStats.copy(bad     = state.scoreStats.bad + 1)
            DecisionScore.BLUNDER -> state.scoreStats.copy(blunder = state.scoreStats.blunder + 1)
        }

        _uiState.update {
            it.copy(
                selectedAction = action,
                decisionScore  = score,
                isAnswered     = true,
                isCorrect      = isCorrect,
                streak         = if (score == DecisionScore.BEST || score == DecisionScore.CORRECT)
                    it.streak + 1 else 0,
                totalAnswered  = it.totalAnswered + 1,
                scoreStats     = updatedStats
            )
        }

        viewModelScope.launch {
            saveSession?.invoke(
                TrainingSession.create(
                    id            = Uuid.random().toString(),
                    scenario      = state.scenario,
                    userAction    = action,
                    correctAction = state.correctAction!!,
                    isCorrect     = isCorrect
                )
            )
        }
    }

    private fun evaluateDecision(
        action: PokerAction,
        equity: Double,
        required: Double
    ): DecisionScore {
        val advantage = equity - required
        val correct   = determineCorrectAction(equity, required)
        return when {
            // Acción correcta
            action == correct && advantage > 0.25  -> DecisionScore.BEST
            action == correct                       -> DecisionScore.CORRECT

            // Fold cuando era call/raise pero muy close
            action == PokerAction.FOLD
                    && advantage in -0.05..0.05    -> DecisionScore.INEXACT

            // Call cuando debería raise pero tiene equity suficiente
            action == PokerAction.CALL
                    && correct == PokerAction.RAISE
                    && advantage > 0.10            -> DecisionScore.INEXACT

            // Raise cuando debería call — agresivo pero defendible
            action == PokerAction.RAISE
                    && correct == PokerAction.CALL
                    && advantage > 0.05            -> DecisionScore.INEXACT

            // Error grave: fold con equity alta
            action == PokerAction.FOLD
                    && equity > 0.60               -> DecisionScore.BLUNDER

            // Error grave: call con equity muy baja
            action == PokerAction.CALL
                    && equity < 0.20               -> DecisionScore.BLUNDER

            else -> DecisionScore.BAD
        }
    }

    // Fix 2: umbral correcto — equity >= required es CALL, no FOLD
    private fun determineCorrectAction(equity: Double, requiredEquity: Double): PokerAction {
        return when {
            equity >= requiredEquity + 0.15 -> PokerAction.RAISE
            equity >= requiredEquity        -> PokerAction.CALL
            else                            -> PokerAction.FOLD
        }
    }

    private fun generateScenario(config: TrainerConfig): TrainingScenario {
        val deck       = Deck()
        deck.shuffle()
        val position   = config.positions.random()
        val stackDepth = config.stackDepth ?: StackDepth.entries.random()
        val boardSize  = when (config.street) {
            StreetFilter.PREFLOP -> 0
            StreetFilter.FLOP    -> 3
            StreetFilter.TURN    -> 4
            StreetFilter.RIVER   -> 5
            StreetFilter.ANY     -> listOf(0, 3, 4, 5).random()
        }
        val holeCards = deck.deal(2)
        val board     = if (boardSize > 0) deck.deal(boardSize) else emptyList()

        // Fix 2: pot odds realistas — máximo 50%
        val potOdds = listOf(0.20, 0.25, 0.33, 0.40, 0.50).random()

        return TrainingScenario(
            position   = position,
            holeCards  = holeCards,
            board      = board,
            potOdds    = potOdds,
            stackDepth = stackDepth
        )
    }

    private fun generatePreflopSequence(heroPosition: Position): List<ActionStep> {
        val allPositions = Position.entries
        val heroIndex    = allPositions.indexOf(heroPosition)
        val actingBefore = allPositions.take(heroIndex)

        if (actingBefore.isEmpty()) {
            return listOf(ActionStep(heroPosition, "primera en actuar", isHero = true))
        }

        val sequence   = mutableListOf<ActionStep>()
        var hasRaise   = false
        var hasCallers = 0

        actingBefore.forEach { pos ->
            val action = when {
                !hasRaise && pos.ordinal >= Position.CUTOFF.ordinal -> {
                    hasRaise = true
                    "raise ${if ((1..2).random() == 1) "2.5bb" else "3bb"}"
                }
                !hasRaise && (1..4).random() == 1 -> {
                    hasRaise = true
                    "raise 3bb"
                }
                hasRaise && (1..3).random() == 1 -> {
                    hasCallers++
                    "call"
                }
                else -> null
            }
            if (action != null) sequence.add(ActionStep(pos, action))
        }

        if (!hasRaise) {
            sequence.add(ActionStep(heroPosition, "primera en abrir", isHero = true))
        } else {
            val callerText = if (hasCallers > 0)
                " (${hasCallers} caller${if (hasCallers > 1) "s" else ""})"
            else ""
            sequence.add(ActionStep(heroPosition, "tu acción$callerText", isHero = true))
        }

        return sequence
    }

    private fun generateStreetHistory(scenario: TrainingScenario): List<StreetAction> {
        if (scenario.board.isEmpty()) return emptyList()
        val streetNames = listOf("Flop", "Turn", "River")
        val streetCount = when (scenario.board.size) {
            3 -> 1; 4 -> 2; 5 -> 3; else -> 0
        }
        // Fix 3: calles anteriores con acción del héroe,
        // última calle es la actual donde el héroe decide
        return (0 until streetCount).mapIndexed { i, _ ->
            val isCurrentStreet = i == streetCount - 1
            StreetAction(
                streetName = streetNames[i],
                actions    = if (isCurrentStreet)
                    generateCurrentStreetActions(scenario.position)
                else
                    generatePastStreetActions(scenario.position)
            )
        }
    }

    // Calles ya jugadas — héroe con acción coherente
    private fun generatePastStreetActions(heroPosition: Position): List<ActionStep> {
        val actions   = mutableListOf<ActionStep>()
        val opponents = listOf(Position.BIG_BLIND, Position.SMALL_BLIND, Position.UTG)
            .filter { it != heroPosition }
            .take((1..2).random())
        var hasBet = false

        opponents.forEach { pos ->
            val action = when {
                !hasBet && (1..2).random() == 1 -> {
                    hasBet = true
                    "bet ${listOf("33%", "50%", "75%").random()}"
                }
                hasBet && (1..2).random() == 1 -> "call"
                else -> null
            }
            if (action != null) actions.add(ActionStep(pos, action))
        }

        // Acción del héroe coherente con lo que pasó
        val heroAction = if (hasBet)
            listOf("call", "raise").random()
        else
            listOf("check", "bet 50%").random()

        actions.add(ActionStep(heroPosition, heroAction, isHero = true))
        return actions
    }

    // Calle actual — solo acciones de rivales, héroe decide
    private fun generateCurrentStreetActions(heroPosition: Position): List<ActionStep> {
        val actions   = mutableListOf<ActionStep>()
        val opponents = listOf(Position.BIG_BLIND, Position.SMALL_BLIND, Position.UTG)
            .filter { it != heroPosition }
            .take((1..2).random())
        var hasBet = false

        opponents.forEach { pos ->
            val action = when {
                !hasBet && (1..2).random() == 1 -> {
                    hasBet = true
                    "bet ${listOf("33%", "50%", "75%").random()}"
                }
                hasBet && (1..2).random() == 1 -> "call"
                else -> null
            }
            if (action != null) actions.add(ActionStep(pos, action))
        }

        actions.add(ActionStep(heroPosition, "tu acción", isHero = true))
        return actions
    }
}