package com.equimaxlab.solver.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RangeParserTest {

    @Test
    fun parsesSimplePair() {
        val combos = RangeParser.parse("AA")
        assertEquals(6, combos.size) // C(4,2) = 6 combinaciones de ases
    }

    @Test
    fun parsesPairPlus() {
        val combos = RangeParser.parse("QQ+")
        // QQ + KK + AA = 3 * 6 = 18
        assertEquals(18, combos.size)
    }

    @Test
    fun parsesSuited() {
        val combos = RangeParser.parse("AKs")
        assertEquals(4, combos.size) // 4 palos
    }

    @Test
    fun parsesOffsuit() {
        val combos = RangeParser.parse("AKo")
        assertEquals(12, combos.size) // 4*3 = 12
    }

    @Test
    fun parsesUnsuited() {
        val combos = RangeParser.parse("AK")
        assertEquals(16, combos.size) // 4 suited + 12 offsuit
    }

    @Test
    fun parsesMultipleTokens() {
        val combos = RangeParser.parse("AA, KK, QQ")
        assertEquals(18, combos.size)
    }

    @Test
    fun noDuplicates() {
        val combos = RangeParser.parse("AA, QQ+")
        assertEquals(18, combos.size) // AA no se duplica
    }
}