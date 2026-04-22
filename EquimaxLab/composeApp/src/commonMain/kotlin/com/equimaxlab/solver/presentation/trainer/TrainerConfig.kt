package com.equimaxlab.solver.presentation.trainer


import com.equimaxlab.solver.domain.model.Position
import com.equimaxlab.solver.domain.model.StackDepth

enum class StreetFilter {
    ANY, PREFLOP, FLOP, TURN, RIVER
}

enum class PreflopAction {
    ANY, SRP, THREE_BET, FOUR_BET, LIMP
}

data class TrainerConfig(
    val positions: Set<Position>     = Position.entries.toSet(),
    val street: StreetFilter         = StreetFilter.ANY,
    val preflopAction: PreflopAction = PreflopAction.ANY,
    val stackDepth: StackDepth?      = null
)