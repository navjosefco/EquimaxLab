package com.equimaxlab.solver.presentation.replayer

import com.equimaxlab.solver.core.Card
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class PlayerAction {
    FOLD, CHECK, CALL, BET, RAISE, ALL_IN
}

data class PlayerActionEntry(
    val playerName: String,
    val action: PlayerAction,
    val amountBb: Double? = null  // null para fold/check/call sin cantidad
)

data class StreetEntry(
    val name: String,
    val cards: List<Card>,
    val actions: List<PlayerActionEntry> = emptyList()
)

data class SavedHandEntry(
    val id: String,
    val label: String,          // ← sustituye timestamp
    val heroCards: List<Card>,
    val villainCards: List<Card>,
    val players: List<String>,
    val streets: List<StreetEntry>,
    val notes: String = ""
)

data class Player(
    val name: String,
    val isHero: Boolean = false
)