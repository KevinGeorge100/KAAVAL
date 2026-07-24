package com.kaaval.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kaaval.app.domain.model.EmergencyContact

@Entity(tableName = "emergency_contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false
) {
    fun toDomainModel(): EmergencyContact {
        return EmergencyContact(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship,
            isPrimary = isPrimary
        )
    }

    companion object {
        fun fromDomainModel(contact: EmergencyContact): ContactEntity {
            return ContactEntity(
                id = contact.id,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary
            )
        }
    }
}
