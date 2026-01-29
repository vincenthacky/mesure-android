package com.example.mesure_android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mesure_android.data.local.database.entity.PointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: PointEntity): Long

    @Query("SELECT * FROM measure_points WHERE id = :pointId")
    suspend fun getPointById(pointId: Long): PointEntity?

    @Query("SELECT * FROM measure_points WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getPointsBySessionId(sessionId: Long): Flow<List<PointEntity>>

    @Query("SELECT * FROM measure_points WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getPointsBySessionIdOnce(sessionId: Long): List<PointEntity>

    @Query("SELECT * FROM measure_points WHERE sessionId = :sessionId ORDER BY orderIndex DESC LIMIT 1")
    suspend fun getLastPointForSession(sessionId: Long): PointEntity?

    @Query("SELECT COUNT(*) FROM measure_points WHERE sessionId = :sessionId")
    suspend fun getPointCountForSession(sessionId: Long): Int

    @Query("DELETE FROM measure_points WHERE id = :pointId")
    suspend fun deletePoint(pointId: Long)

    @Query("DELETE FROM measure_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)
}
