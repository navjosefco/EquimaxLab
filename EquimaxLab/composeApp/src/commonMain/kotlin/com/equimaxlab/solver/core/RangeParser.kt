package com.equimaxlab.solver.core

object RangeParser {

    fun parse(input: String): List<List<Card>> {
        return input
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { parseToken(it) }
            .distinct()
    }

    private fun parseToken(token: String): List<List<Card>> {
        return when {
            token.endsWith("+")                          -> parsePlus(token.dropLast(1))
            token.contains("-")                          -> parseRange(token)
            token.endsWith("s")                          -> parseSuited(token.dropLast(1))
            token.endsWith("o")                          -> parseOffsuit(token.dropLast(1))
            token.length == 2 && token[0] == token[1]   -> parsePair(token)
            token.length == 2                            -> parseSuited(token) + parseOffsuit(token)
            else                                         -> emptyList()
        }
    }

    // QQ+ → QQ, KK, AA
    private fun parsePlus(token: String): List<List<Card>> {
        if (token.length == 2 && token[0] == token[1]) {
            val base = rankFromChar(token[0]) ?: return emptyList()
            return Rank.entries
                .filter { it.value >= base.value }
                .flatMap { rank -> makePairs(rank) }
        }
        // AKs+ → AKs, AQs... (incrementa el segundo)
        if (token.length == 2) {
            val high = rankFromChar(token[0]) ?: return emptyList()
            val low  = rankFromChar(token[1]) ?: return emptyList()
            return Rank.entries
                .filter { it.value in low.value until high.value }
                .flatMap { rank -> makeSuited(high, rank) }
        }
        return emptyList()
    }

    // JJ-99 → JJ, TT, 99
    private fun parseRange(token: String): List<List<Card>> {
        val parts = token.split("-")
        if (parts.size != 2) return emptyList()
        val top = rankFromChar(parts[0].first()) ?: return emptyList()
        val bot = rankFromChar(parts[1].first()) ?: return emptyList()
        return Rank.entries
            .filter { it.value in bot.value..top.value }
            .flatMap { makePairs(it) }
    }

    // QQ → todas las combinaciones de par de reinas
    private fun parsePair(token: String): List<List<Card>> {
        val rank = rankFromChar(token[0]) ?: return emptyList()
        return makePairs(rank)
    }

    // AKs → todas las combinaciones suited
    private fun parseSuited(token: String): List<List<Card>> {
        if (token.length < 2) return emptyList()
        val high = rankFromChar(token[0]) ?: return emptyList()
        val low  = rankFromChar(token[1]) ?: return emptyList()
        return makeSuited(high, low)
    }

    // AKo → todas las combinaciones offsuit
    private fun parseOffsuit(token: String): List<List<Card>> {
        if (token.length < 2) return emptyList()
        val high = rankFromChar(token[0]) ?: return emptyList()
        val low  = rankFromChar(token[1]) ?: return emptyList()
        return makeOffsuit(high, low)
    }

    private fun makePairs(rank: Rank): List<List<Card>> {
        val suits = Suit.entries
        return suits.flatMapIndexed { i, s1 ->
            suits.drop(i + 1).map { s2 ->
                listOf(Card(rank, s1), Card(rank, s2))
            }
        }
    }

    private fun makeSuited(high: Rank, low: Rank): List<List<Card>> =
        Suit.entries.map { suit ->
            listOf(Card(high, suit), Card(low, suit))
        }

    private fun makeOffsuit(high: Rank, low: Rank): List<List<Card>> {
        return Suit.entries.flatMap { s1 ->
            Suit.entries
                .filter { it != s1 }
                .map { s2 -> listOf(Card(high, s1), Card(low, s2)) }
        }
    }

    private fun rankFromChar(c: Char): Rank? = when (c.uppercaseChar()) {
        '2'  -> Rank.TWO
        '3'  -> Rank.THREE
        '4'  -> Rank.FOUR
        '5'  -> Rank.FIVE
        '6'  -> Rank.SIX
        '7'  -> Rank.SEVEN
        '8'  -> Rank.EIGHT
        '9'  -> Rank.NINE
        'T'  -> Rank.TEN
        'J'  -> Rank.JACK
        'Q'  -> Rank.QUEEN
        'K'  -> Rank.KING
        'A'  -> Rank.ACE
        else -> null
    }
}