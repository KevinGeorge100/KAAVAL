package com.kaaval.app.data

import com.kaaval.app.data.entity.ContactEntity
import com.kaaval.app.data.entity.EmergencySessionEntity
import com.kaaval.app.data.entity.EmergencyStateEntity
import com.kaaval.app.data.entity.IncidentEntity
import com.kaaval.app.data.entity.MedicalProfileEntity
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.domain.model.EmergencyIncident
import com.kaaval.app.domain.model.EmergencySession
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.MedicalProfile
import com.kaaval.app.domain.repository.EmergencySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class KaavalRepository(private val db: KaavalDatabase) : EmergencySessionRepository {

    val allContacts: Flow<List<EmergencyContact>> = db.contactDao().getAllContacts().map { list ->
        list.map { it.toDomainModel() }
    }

    val medicalProfile: Flow<MedicalProfile?> = db.medicalProfileDao().getMedicalProfile().map {
        it?.toDomainModel()
    }

    val currentEmergencyState: Flow<EmergencyState.LiveTracking?> = db.emergencyStateDao().getCurrentState().map {
        it?.toDomainModel()
    }

    suspend fun saveEmergencyState(state: EmergencyState.LiveTracking) {
        db.emergencyStateDao().saveState(EmergencyStateEntity.fromDomainModel(state))
    }

    suspend fun clearEmergencyState() {
        db.emergencyStateDao().clearState()
    }

    suspend fun insertContact(contact: EmergencyContact) {
        db.contactDao().insertContact(ContactEntity.fromDomainModel(contact))
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        db.contactDao().deleteContact(ContactEntity.fromDomainModel(contact))
    }

    suspend fun setPrimaryContact(contactId: String) {
        db.contactDao().clearPrimaryContact()
        db.contactDao().setPrimaryContact(contactId)
    }

    suspend fun logIncident(incident: EmergencyIncident) {
        db.incidentDao().insertIncident(IncidentEntity.fromDomainModel(incident))
    }

    suspend fun saveMedicalProfile(profile: MedicalProfile) {
        db.medicalProfileDao().saveMedicalProfile(MedicalProfileEntity.fromDomainModel(profile))
    }

    // Emergency Session Repository Implementation
    override suspend fun createSession(session: EmergencySession): Boolean {
        val active = db.emergencySessionDao().getActiveSession().first()
        if (active != null) {
            android.util.Log.w("KaavalRepository", "DUPLICATE_SESSION_REJECTED incidentId=${session.incidentId}")
            return false
        }
        db.emergencySessionDao().insertSession(EmergencySessionEntity.fromDomainModel(session))
        android.util.Log.i("KaavalRepository", "SESSION_CREATED incidentId=${session.incidentId}")
        return true
    }

    override fun getActiveSession(): Flow<EmergencySession?> {
        return db.emergencySessionDao().getActiveSession().map { it?.toDomainModel() }
    }

    override suspend fun updateSession(session: EmergencySession) {
        db.emergencySessionDao().updateSession(EmergencySessionEntity.fromDomainModel(session))
    }

    override suspend fun completeSession(incidentId: String) {
        db.emergencySessionDao().markCompleted(incidentId, System.currentTimeMillis())
        android.util.Log.i("KaavalRepository", "SESSION_COMPLETED incidentId=$incidentId")
    }

    override suspend fun clearActiveSession() {
        db.emergencySessionDao().clearAll()
    }
}
