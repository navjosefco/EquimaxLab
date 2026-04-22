package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.domain.model.SavedHand
import com.equimaxlab.solver.domain.repository.HandHistoryRepository

class SaveHandUseCase(private val repository: HandHistoryRepository) {
    suspend operator fun invoke(hand: SavedHand) =
        repository.saveHand(hand)
}