package com.equimaxlab.solver.core

class Deck {
    private val cards: MutableList<Card> = buildList {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                add(Card(rank, suit))
            }
        }
    }.toMutableList()

    fun shuffle() = cards.shuffle()

    fun deal(n: Int): List<Card> {
        require(cards.size >= n) { "Not enough cards in deck" }
        return (0 until n).map { cards.removeLast() }
    }

    fun remove(cards: List<Card>) = this.cards.removeAll(cards.toSet())

    val remaining: Int get() = cards.size
}