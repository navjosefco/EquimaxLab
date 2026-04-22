package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.HandEvaluator
import com.equimaxlab.solver.core.HandResult

class EvaluateHandUseCase {
    operator fun invoke(
        holeCards: List<Card>,
        board: List<Card>
    ): HandResult? {
        if (board.size < 3) return null
        return HandEvaluator.evaluate(holeCards + board)
    }
}