package com.equimaxlab.solver.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandEvaluatorTest {

    private fun card(rank: Rank, suit: Suit) = Card(rank, suit)

    @Test
    fun detectsRoyalFlush() {
        val cards = listOf(
            card(Rank.ACE, Suit.SPADES), card(Rank.KING, Suit.SPADES),
            card(Rank.QUEEN, Suit.SPADES), card(Rank.JACK, Suit.SPADES),
            card(Rank.TEN, Suit.SPADES)
        )
        assertEquals(HandRank.ROYAL_FLUSH, HandEvaluator.evaluate(cards).rank)
    }

    @Test
    fun detectsStraightFlush() {
        val cards = listOf(
            card(Rank.NINE, Suit.HEARTS), card(Rank.EIGHT, Suit.HEARTS),
            card(Rank.SEVEN, Suit.HEARTS), card(Rank.SIX, Suit.HEARTS),
            card(Rank.FIVE, Suit.HEARTS)
        )
        assertEquals(HandRank.STRAIGHT_FLUSH, HandEvaluator.evaluate(cards).rank)
    }

    @Test
    fun detectsFourOfAKind() {
        val cards = listOf(
            card(Rank.ACE, Suit.SPADES), card(Rank.ACE, Suit.HEARTS),
            card(Rank.ACE, Suit.DIAMONDS), card(Rank.ACE, Suit.CLUBS),
            card(Rank.KING, Suit.SPADES)
        )
        assertEquals(HandRank.FOUR_OF_A_KIND, HandEvaluator.evaluate(cards).rank)
    }

    @Test
    fun detectsFullHouse() {
        val cards = listOf(
            card(Rank.KING, Suit.SPADES), card(Rank.KING, Suit.HEARTS),
            card(Rank.KING, Suit.DIAMONDS), card(Rank.ACE, Suit.CLUBS),
            card(Rank.ACE, Suit.SPADES)
        )
        assertEquals(HandRank.FULL_HOUSE, HandEvaluator.evaluate(cards).rank)
    }

    @Test
    fun detectsWheel() {
        val cards = listOf(
            card(Rank.ACE, Suit.SPADES), card(Rank.TWO, Suit.HEARTS),
            card(Rank.THREE, Suit.DIAMONDS), card(Rank.FOUR, Suit.CLUBS),
            card(Rank.FIVE, Suit.SPADES)
        )
        assertEquals(HandRank.STRAIGHT, HandEvaluator.evaluate(cards).rank)
    }

    @Test
    fun higherHandBeatsLower() {
        val flush = listOf(
            card(Rank.ACE, Suit.SPADES), card(Rank.TEN, Suit.SPADES),
            card(Rank.EIGHT, Suit.SPADES), card(Rank.SIX, Suit.SPADES),
            card(Rank.TWO, Suit.SPADES)
        )
        val straight = listOf(
            card(Rank.NINE, Suit.HEARTS), card(Rank.EIGHT, Suit.DIAMONDS),
            card(Rank.SEVEN, Suit.CLUBS), card(Rank.SIX, Suit.SPADES),
            card(Rank.FIVE, Suit.HEARTS)
        )
        assertTrue(HandEvaluator.evaluate(flush) > HandEvaluator.evaluate(straight))
    }

    @Test
    fun evaluates7Cards() {
        val cards = listOf(
            card(Rank.ACE, Suit.SPADES), card(Rank.KING, Suit.SPADES),
            card(Rank.QUEEN, Suit.SPADES), card(Rank.JACK, Suit.SPADES),
            card(Rank.TEN, Suit.SPADES), card(Rank.TWO, Suit.HEARTS),
            card(Rank.THREE, Suit.DIAMONDS)
        )
        assertEquals(HandRank.ROYAL_FLUSH, HandEvaluator.evaluate(cards).rank)
    }
}