package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.domain.model.SavedHand
import com.equimaxlab.solver.domain.repository.HandHistoryRepository

class GetHandsUseCase(private val repository: HandHistoryRepository) {
    suspend operator fun invoke(): List<SavedHand> =
        repository.getHands()
}