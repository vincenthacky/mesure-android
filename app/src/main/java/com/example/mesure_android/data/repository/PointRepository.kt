package com.example.mesure_android.data.repository

import com.example.mesure_android.data.local.database.dao.PointDao
import com.example.mesure_android.data.local.database.entity.PointEntity
import com.example.mesure_android.data.model.MeasurePoint
import com.example.mesure_android.data.model.Vector3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointRepository @Inject constructor(
    private val pointDao: PointDao
) {
    fun getPointsForSession(sessionId: Long): Flow<List<MeasurePoint>> {
        return pointDao.getPointsBySessionId(sessionId).map { entities ->
            entities.map { it.toMeasurePoint() }
        }
    }

    suspend fun getPointsForSessionOnce(sessionId: Long): List<MeasurePoint> {
        return pointDao.getPointsBySessionIdOnce(sessionId).map { it.toMeasurePoint() }
    }

    suspend fun getLastPointForSession(sessionId: Long): MeasurePoint? {
        return pointDao.getLastPointForSession(sessionId)?.toMeasurePoint()
    }

    suspend fun getPointCountForSession(sessionId: Long): Int {
        return pointDao.getPointCountForSession(sessionId)
    }

    suspend fun savePoint(point: MeasurePoint): Long {
        val entity = PointEntity(
            id = if (point.id == 0L) 0 else point.id,
            sessionId = point.sessionId,
            orderIndex = point.orderIndex,
            worldX = point.worldPosition.x,
            worldY = point.worldPosition.y,
            worldZ = point.worldPosition.z,
            relativeToOriginX = point.relativeToOrigin.x,
            relativeToOriginY = point.relativeToOrigin.y,
            relativeToOriginZ = point.relativeToOrigin.z,
            previousPointId = point.previousPointId,
            relativeToPreviousX = point.relativeToPrevious?.x,
            relativeToPreviousY = point.relativeToPrevious?.y,
            relativeToPreviousZ = point.relativeToPrevious?.z,
            distanceToPrevious = point.distanceToPrevious,
            label = point.label,
            createdAt = point.createdAt
        )
        return pointDao.insertPoint(entity)
    }

    suspend fun deletePoint(pointId: Long) {
        pointDao.deletePoint(pointId)
    }

    private fun PointEntity.toMeasurePoint(): MeasurePoint {
        return MeasurePoint(
            id = id,
            sessionId = sessionId,
            orderIndex = orderIndex,
            worldPosition = Vector3(worldX, worldY, worldZ),
            relativeToOrigin = Vector3(relativeToOriginX, relativeToOriginY, relativeToOriginZ),
            previousPointId = previousPointId,
            relativeToPrevious = if (relativeToPreviousX != null && relativeToPreviousY != null && relativeToPreviousZ != null) {
                Vector3(relativeToPreviousX, relativeToPreviousY, relativeToPreviousZ)
            } else null,
            distanceToPrevious = distanceToPrevious,
            label = label,
            createdAt = createdAt
        )
    }
}
