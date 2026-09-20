package com.kaaval.app.data.dao

import androidx.room.*
import com.kaaval.app.data.entity.EmergencySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: EmergencySessionEntity)

    @Update
    suspend fun updateSession(session: EmergencySessionEntity)

    @Query("SELECT * FROM emergency_sessions WHERE status = 'ACTIVE' OR status = 'COMPLETING' LIMIT 1")
    fun getActiveSession(): Flow<EmergencySessionEntity?>

    @Query("SELECT * FROM emergency_sessions WHERE incidentId = :incidentId")
    suspend fun getSessionById(incidentId: String): EmergencySessionEntity?

    @Query("DELETE FROM emergency_sessions")
    suspend fun clearAll()

    @Query("UPDATE emergency_sessions SET status = 'COMPLETED', endedAt = :endedAt WHERE incidentId = :incidentId")
    suspend fun markCompleted(incidentId: String, endedAt: Long)
}
