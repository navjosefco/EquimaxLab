package com.equimaxlab.solver.domain.model

import com.equimaxlab.solver.core.Card

data class TrainingScenario(
    val position: Position,
    val holeCards: List<Card>,
    val board: List<Card>,
    val potOdds: Double,
    val stackDepth: StackDepth
)