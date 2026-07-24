package com.kaaval.app.data.dao

import androidx.room.*
import com.kaaval.app.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("UPDATE emergency_contacts SET isPrimary = 0")
    suspend fun clearPrimaryContact()

    @Query("UPDATE emergency_contacts SET isPrimary = 1 WHERE id = :contactId")
    suspend fun setPrimaryContact(contactId: String)
}
