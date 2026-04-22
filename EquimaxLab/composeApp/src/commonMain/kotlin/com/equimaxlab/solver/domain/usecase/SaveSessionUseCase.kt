package com.equimaxlab.solver.domain.usecase

import com.equimaxlab.solver.domain.model.TrainingSession
import com.equimaxlab.solver.domain.repository.SessionRepository

class SaveSessionUseCase(private val repository: SessionRepository) {
    suspend operator fun invoke(session: TrainingSession) =
        repository.saveSession(session)
}