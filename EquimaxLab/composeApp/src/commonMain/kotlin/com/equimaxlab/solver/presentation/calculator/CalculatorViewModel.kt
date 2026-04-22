package com.equimaxlab.solver.presentation.calculator


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.EquityResult
import com.equimaxlab.solver.domain.usecase.CalculateEquityUseCase
import com.equimaxlab.solver.domain.usecase.EvaluateHandUseCase
import com.equimaxlab.solver.core.HandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CalculatorUiState(
    val holeCards: List<Card> = emptyList(),
    val board: List<Card> = emptyList(),
    val opponentRange: String = "",
    val equityResult: EquityResult? = null,
    val handResult: HandResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CalculatorViewModel(
    private val calculateEquity: CalculateEquityUseCase = CalculateEquityUseCase(),
    private val evaluateHand: EvaluateHandUseCase = EvaluateHandUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun addHoleCard(card: Card) {
        if (_uiState.value.holeCards.size >= 2) return
        if (card in _uiState.value.holeCards) return
        _uiState.update { it.copy(holeCards = it.holeCards + card) }
        recalculate()
    }

    fun removeHoleCard(card: Card) {
        _uiState.update { it.copy(holeCards = it.holeCards - card, equityResult = null) }
    }

    fun addBoardCard(card: Card) {
        if (_uiState.value.board.size >= 5) return
        if (card in _uiState.value.board) return
        if (card in _uiState.value.holeCards) return
        _uiState.update { it.copy(board = it.board + card) }
        recalculate()
    }

    fun removeBoardCard(card: Card) {
        _uiState.update { it.copy(board = it.board - card, equityResult = null) }
    }

    fun setOpponentRange(range: String) {
        _uiState.update { it.copy(opponentRange = range) }
    }

    fun calculate() {
        val state = _uiState.value
        if (state.holeCards.size < 2) {
            _uiState.update { it.copy(error = "Selecciona 2 cartas") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val equity = withContext(Dispatchers.Default) {
                    calculateEquity(
                        holeCards     = state.holeCards,
                        opponentRange = state.opponentRange,
                        board         = state.board
                    )
                }
                val hand = withContext(Dispatchers.Default) {
                    evaluateHand(state.holeCards, state.board)
                }
                _uiState.update {
                    it.copy(
                        equityResult = equity,
                        handResult   = hand,
                        isLoading    = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun reset() {
        _uiState.value = CalculatorUiState()
    }

    private fun recalculate() {
        val state = _uiState.value
        if (state.holeCards.size == 2) calculate()
    }
}