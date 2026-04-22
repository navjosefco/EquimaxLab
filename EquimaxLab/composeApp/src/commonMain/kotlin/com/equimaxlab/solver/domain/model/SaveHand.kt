package com.equimaxlab.solver.domain.model

import com.equimaxlab.solver.core.Card
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class SavedHand(
    val id: String,
    val holeCards: List<Card>,
    val board: List<Card>,
    val result: HandResult,
    val notes: String,
    val timestamp: Instant
) {
    companion object {
        fun create(
            id: String,
            holeCards: List<Card>,
            board: List<Card>,
            result: HandResult,
            notes: String = ""
        ) = SavedHand(
            id        = id,
            holeCards = holeCards,
            board     = board,
            result    = result,
            notes     = notes,
            timestamp = Clock.System.now()
        )
    }
}