package com.equimaxlab.solver.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DeckTest {

    @Test
    fun deckHas52Cards() {
        assertEquals(52, Deck().remaining)
    }

    @Test
    fun dealReducesDeckSize() {
        val deck = Deck()
        deck.deal(5)
        assertEquals(47, deck.remaining)
    }

    @Test
    fun removedCardsNotInDeck() {
        val deck = Deck()
        val hole = listOf(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.HEARTS))
        deck.remove(hole)
        assertEquals(50, deck.remaining)
    }

    @Test
    fun allCardsAreUnique() {
        val deck = Deck()
        val dealt = deck.deal(52)
        assertEquals(52, dealt.toSet().size)
    }
}