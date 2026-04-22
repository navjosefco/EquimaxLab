package com.equimaxlab.solver.core

import kotlin.test.Test
import kotlin.test.assertTrue

class MonteCarloSimulatorTest {

    @Test
    fun acesVsTwosPreflop() {
        val result = MonteCarloSimulator.calculate(
            holeCards    = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.HEARTS)),
            opponentHole = listOf(Card(Rank.TWO, Suit.CLUBS), Card(Rank.TWO, Suit.DIAMONDS)),
            iterations   = 5_000
        )
        // AA vs 22 preflop: AA gana ~80% de las veces
        assertTrue(result.winProbability > 0.75, "AA debería ganar más del 75% vs 22, fue ${result.winPercent}")
    }

    @Test
    fun equityIsAlways100Percent() {
        val result = MonteCarloSimulator.calculate(
            holeCards  = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
            iterations = 1_000
        )
        val total = result.winProbability + result.tieProbability + result.lossProbability
        assertTrue(total > 0.999 && total < 1.001, "Total debe ser ~1.0, fue $total")
    }

    @Test
    fun madeHandOnRiverIsAccurate() {
        // Tenemos escalera de color servida en el river
        val result = MonteCarloSimulator.calculate(
            holeCards    = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)),
            opponentHole = listOf(Card(Rank.TWO, Suit.HEARTS), Card(Rank.THREE, Suit.DIAMONDS)),
            board        = listOf(
                Card(Rank.QUEEN, Suit.SPADES), Card(Rank.JACK, Suit.SPADES),
                Card(Rank.TEN, Suit.SPADES), Card(Rank.FOUR, Suit.CLUBS),
                Card(Rank.FIVE, Suit.HEARTS)
            ),
            iterations   = 100
        )
        // Royal flush vs basura: ganamos siempre
        assertTrue(result.winProbability > 0.99, "Royal flush debe ganar >99%, fue ${result.winPercent}")
    }

    @Test
    fun unknownOpponentEquityIsReasonable() {
        val result = MonteCarloSimulator.calculate(
            holeCards  = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.ACE, Suit.HEARTS)),
            iterations = 5_000
        )
        // AA sin conocer rival: equity razonable entre 70-90%
        assertTrue(result.winProbability > 0.70, "AA preflop debe ganar >70%, fue ${result.winPercent}")
        assertTrue(result.winProbability < 0.95, "AA preflop debe ganar <95%, fue ${result.winPercent}")
    }
}