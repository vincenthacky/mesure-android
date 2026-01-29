package com.example.mesure_android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mesure_android.data.local.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE siteId = :siteId ORDER BY startedAt DESC")
    fun getSessionsBySiteId(siteId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE siteId = :siteId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestSessionForSite(siteId: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("UPDATE sessions SET isCalibrated = :isCalibrated, originX = :originX, originY = :originY, originZ = :originZ, originQx = :qx, originQy = :qy, originQz = :qz, originQw = :qw WHERE id = :sessionId")
    suspend fun calibrateSession(
        sessionId: Long,
        isCalibrated: Boolean,
        originX: Float,
        originY: Float,
        originZ: Float,
        qx: Float,
        qy: Float,
        qz: Float,
        qw: Float
    )

    @Query("UPDATE sessions SET endedAt = :endedAt WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endedAt: Long)
}
