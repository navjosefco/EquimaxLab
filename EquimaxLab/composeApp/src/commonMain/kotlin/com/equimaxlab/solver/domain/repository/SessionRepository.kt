package com.equimaxlab.solver.domain.repository

import com.equimaxlab.solver.domain.model.TrainingSession

interface SessionRepository {
    suspend fun saveSession(session: TrainingSession)
    suspend fun getSessions(): List<TrainingSession>
    suspend fun deleteSession(id: String)
}