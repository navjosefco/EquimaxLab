package com.equimaxlab.solver.core

import kotlin.math.roundToInt

data class EquityResult(
    val winProbability: Double,
    val tieProbability: Double,
    val lossProbability: Double
) {
    val winPercent: String get() = "${(winProbability * 100).roundToInt()}%"
    val tiePercent: String get() = "${(tieProbability * 100).roundToInt()}%"
    val lossPercent: String get() = "${(lossProbability * 100).roundToInt()}%"
}

object MonteCarloSimulator {

    fun calculate(
        holeCards: List<Card>,
        opponentHole: List<Card> = emptyList(),
        board: List<Card> = emptyList(),
        iterations: Int = 10_000
    ): EquityResult {
        require(holeCards.size == 2) { "Need exactly 2 hole cards" }
        require(board.size in listOf(0, 3, 4, 5)) { "Board must have 0, 3, 4 or 5 cards" }

        val knownCards = holeCards + opponentHole + board
        require(knownCards.size == knownCards.toSet().size) { "Duplicate cards detected" }

        var wins = 0
        var ties = 0

        repeat(iterations) {
            val deck = Deck()
            deck.remove(knownCards)
            deck.shuffle()

            val opCards = if (opponentHole.size == 2) opponentHole
            else deck.deal(2)

            val boardCards = board + deck.deal(5 - board.size)

            val myHand = HandEvaluator.evaluate(holeCards + boardCards)
            val opHand = HandEvaluator.evaluate(opCards + boardCards)

            when {
                myHand > opHand -> wins++
                myHand == opHand -> ties++
            }
        }

        return EquityResult(
            winProbability  = wins.toDouble() / iterations,
            tieProbability  = ties.toDouble() / iterations,
            lossProbability = (iterations - wins - ties).toDouble() / iterations
        )
    }
}