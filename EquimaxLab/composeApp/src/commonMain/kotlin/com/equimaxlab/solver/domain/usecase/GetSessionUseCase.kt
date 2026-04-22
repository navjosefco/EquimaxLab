package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.domain.model.TrainingSession
import com.equimaxlab.solver.domain.repository.SessionRepository

class GetSessionsUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(): List<TrainingSession> =
        repository.getSessions()
}