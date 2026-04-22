package com.equimaxlab.solver.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class TrainingSession(
    val id: String,
    val scenario: TrainingScenario,
    val userAction: PokerAction,
    val correctAction: PokerAction,
    val isCorrect: Boolean,
    val timestamp: Instant
) {
    companion object {
        fun create(
            id: String,
            scenario: TrainingScenario,
            userAction: PokerAction,
            correctAction: PokerAction,
            isCorrect: Boolean
        ) = TrainingSession(
            id            = id,
            scenario      = scenario,
            userAction    = userAction,
            correctAction = correctAction,
            isCorrect     = isCorrect,
            timestamp     = Clock.System.now()
        )
    }
}