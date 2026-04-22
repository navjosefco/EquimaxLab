package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.EquityResult
import com.equimaxlab.solver.core.MonteCarloSimulator
import com.equimaxlab.solver.core.RangeParser

class CalculateEquityUseCase {
    operator fun invoke(
        holeCards: List<Card>,
        opponentRange: String = "",
        board: List<Card> = emptyList(),
        iterations: Int = 10_000
    ): EquityResult {
        return if (opponentRange.isBlank()) {
            MonteCarloSimulator.calculate(
                holeCards  = holeCards,
                board      = board,
                iterations = iterations
            )
        } else {
            val rangeCombos = RangeParser.parse(opponentRange)
            val results = rangeCombos
                .filter { combo -> combo.none { it in holeCards || it in board } }
                .map { opponentHole ->
                    MonteCarloSimulator.calculate(
                        holeCards    = holeCards,
                        opponentHole = opponentHole,
                        board        = board,
                        iterations   = maxOf(100, iterations / rangeCombos.size)
                    )
                }
            if (results.isEmpty()) {
                MonteCarloSimulator.calculate(holeCards = holeCards, board = board)
            } else {
                EquityResult(
                    winProbability  = results.map { it.winProbability }.average(),
                    tieProbability  = results.map { it.tieProbability }.average(),
                    lossProbability = results.map { it.lossProbability }.average()
                )
            }
        }
    }
}