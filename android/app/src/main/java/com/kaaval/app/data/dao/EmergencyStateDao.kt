package com.kaaval.app.data.dao

import androidx.room.*
import com.kaaval.app.data.entity.EmergencyStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyStateDao {
    @Query("SELECT * FROM emergency_current_state LIMIT 1")
    fun getCurrentState(): Flow<EmergencyStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: EmergencyStateEntity)

    @Query("DELETE FROM emergency_current_state")
    suspend fun clearState()
}
