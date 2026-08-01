package com.kaaval.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaaval.app.data.entity.EmergencyStateEntity

@Dao
interface EmergencyStateDao {
    @Query("SELECT * FROM emergency_state WHERE id = 1 LIMIT 1")
    suspend fun getEmergencyState(): EmergencyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmergencyState(state: EmergencyStateEntity)

    @Query("DELETE FROM emergency_state")
    suspend fun clearEmergencyState()
}
