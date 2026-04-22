package com.equimaxlab.solver.core

object HandEvaluator {

    fun evaluate(cards: List<Card>): HandResult {
        require(cards.size >= 5) { "Need at least 5 cards" }

        val best = cards.combinations(5).maxOf { evaluateFive(it) }
        return best
    }

    private fun evaluateFive(cards: List<Card>): HandResult {
        require(cards.size == 5)

        val ranks  = cards.map { it.rank.value }.sortedDescending()
        val suits  = cards.map { it.suit }
        val isFlush    = suits.toSet().size == 1
        val isStraight = isStraight(ranks)

        if (isFlush && isStraight) {
            return if (ranks.first() == 14) // As alto
                HandResult(HandRank.ROYAL_FLUSH, ranks.first())
            else
                HandResult(HandRank.STRAIGHT_FLUSH, ranks.first())
        }

        val groups = ranks.groupBy { it }
            .values
            .sortedWith(compareByDescending<List<Int>> { it.size }.thenByDescending { it.first() })

        val groupSizes = groups.map { it.size }

        return when {
            groupSizes[0] == 4 ->
                HandResult(HandRank.FOUR_OF_A_KIND, score(groups))

            groupSizes[0] == 3 && groupSizes[1] == 2 ->
                HandResult(HandRank.FULL_HOUSE, score(groups))

            isFlush ->
                HandResult(HandRank.FLUSH, ranks.fold(0) { acc, r -> acc * 15 + r })

            isStraight ->
                HandResult(HandRank.STRAIGHT, ranks.first())

            groupSizes[0] == 3 ->
                HandResult(HandRank.THREE_OF_A_KIND, score(groups))

            groupSizes[0] == 2 && groupSizes[1] == 2 ->
                HandResult(HandRank.TWO_PAIR, score(groups))

            groupSizes[0] == 2 ->
                HandResult(HandRank.ONE_PAIR, score(groups))

            else ->
                HandResult(HandRank.HIGH_CARD, ranks.fold(0) { acc, r -> acc * 15 + r })
        }
    }

    private fun isStraight(sortedRanks: List<Int>): Boolean {
        // Escalera normal
        if (sortedRanks.zipWithNext().all { (a, b) -> a - b == 1 }) return true
        // Escalera de rueda: A-2-3-4-5
        val wheel = listOf(14, 5, 4, 3, 2)
        return sortedRanks == wheel
    }

    private fun score(groups: List<List<Int>>): Int =
        groups.fold(0) { acc, g -> acc * 15 + g.first() * 10 + g.size }
}

fun <T> List<T>.combinations(k: Int): Sequence<List<T>> = sequence {
    if (k == 0) { yield(emptyList()); return@sequence }
    if (k > size) return@sequence
    val indices = IntArray(k) { it }
    while (true) {
        yield(indices.map { this@combinations[it] })
        var i = k - 1
        while (i >= 0 && indices[i] == size - k + i) i--
        if (i < 0) break
        indices[i]++
        for (j in i + 1 until k) indices[j] = indices[j - 1] + 1
    }
}