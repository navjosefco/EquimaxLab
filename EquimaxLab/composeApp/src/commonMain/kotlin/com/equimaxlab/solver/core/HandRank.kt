package com.equimaxlab.solver.core

enum class HandRank {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    STRAIGHT,
    FLUSH,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    STRAIGHT_FLUSH,
    ROYAL_FLUSH;
}

data class HandResult(
    val rank: HandRank,
    val score: Int  // permite comparar dos manos del mismo HandRank
) : Comparable<HandResult> {
    override fun compareTo(other: HandResult): Int =
        compareValuesBy(this, other, { it.rank.ordinal }, { it.score })
}