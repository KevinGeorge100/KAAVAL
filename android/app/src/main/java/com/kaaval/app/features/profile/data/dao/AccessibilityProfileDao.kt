package com.kaaval.app.features.profile.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaaval.app.features.profile.data.entity.AccessibilityProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessibilityProfileDao {
    @Query("SELECT * FROM accessibility_profile WHERE id = 1")
    fun getProfile(): Flow<AccessibilityProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: AccessibilityProfileEntity)
}
