package com.equimaxlab.solver.domain.model

import com.equimaxlab.solver.core.HandRank

data class HandResult(
    val rank: HandRank,
    val wonPot: Boolean,
    val potSize: Double
)