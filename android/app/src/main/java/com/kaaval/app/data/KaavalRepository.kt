package com.kaaval.app.data

import com.kaaval.app.data.entity.ContactEntity
import com.kaaval.app.data.entity.EmergencyStateEntity
import com.kaaval.app.data.entity.IncidentEntity
import com.kaaval.app.data.entity.MedicalProfileEntity
import com.kaaval.app.domain.model.EmergencyContact
import com.kaaval.app.domain.model.EmergencyIncident
import com.kaaval.app.domain.model.EmergencyState
import com.kaaval.app.domain.model.MedicalProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KaavalRepository(private val db: KaavalDatabase) {

    val allContacts: Flow<List<EmergencyContact>> = db.contactDao().getAllContacts().map { list ->
        list.map { it.toDomainModel() }
    }

    val medicalProfile: Flow<MedicalProfile?> = db.medicalProfileDao().getMedicalProfile().map {
        it?.toDomainModel()
    }

    val currentEmergencyState: Flow<EmergencyState.Active?> = db.emergencyStateDao().getCurrentState().map {
        it?.toDomainModel()
    }

    suspend fun saveEmergencyState(state: EmergencyState.Active) {
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
}
