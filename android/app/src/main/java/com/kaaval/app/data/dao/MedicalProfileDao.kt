package com.kaaval.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaaval.app.data.entity.MedicalProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalProfileDao {
    @Query("SELECT * FROM medical_profile WHERE id = 1 LIMIT 1")
    fun getMedicalProfile(): Flow<MedicalProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMedicalProfile(profile: MedicalProfileEntity)
}
