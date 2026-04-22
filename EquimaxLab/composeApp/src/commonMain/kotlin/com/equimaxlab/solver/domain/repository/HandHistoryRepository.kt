package com.equimaxlab.solver.domain.repository

import com.equimaxlab.solver.domain.model.SavedHand

interface HandHistoryRepository {
    suspend fun saveHand(hand: SavedHand)
    suspend fun getHands(): List<SavedHand>
    suspend fun deleteHand(id: String)
}