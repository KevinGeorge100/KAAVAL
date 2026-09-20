package com.kaaval.app.data.repository

import com.kaaval.app.data.dao.EmergencySessionDao
import com.kaaval.app.data.entity.EmergencySessionEntity
import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.repository.EmergencySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class EmergencySessionRepositoryImpl(private val dao: EmergencySessionDao) : EmergencySessionRepository {

    override suspend fun createSession(session: EmergencySession): Boolean {
        // Check if an active session already exists
        val active = dao.getActiveSession().first()
        if (active != null) return false

        dao.insertSession(EmergencySessionEntity.fromDomainModel(session))
        return true
    }

    override fun getActiveSession(): Flow<EmergencySession?> {
        return dao.getActiveSession().map { it?.toDomainModel() }
    }

    override suspend fun updateSession(session: EmergencySession) {
        dao.updateSession(EmergencySessionEntity.fromDomainModel(session))
    }

    override suspend fun completeSession(incidentId: String) {
        dao.markCompleted(incidentId, System.currentTimeMillis())
    }

    override suspend fun clearActiveSession() {
        dao.clearAll()
    }
}
