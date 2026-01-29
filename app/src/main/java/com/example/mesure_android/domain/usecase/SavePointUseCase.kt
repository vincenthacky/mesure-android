package com.example.mesure_android.domain.usecase

import com.example.mesure_android.data.model.MeasurePoint
import com.example.mesure_android.data.model.Vector3
import com.example.mesure_android.data.repository.PointRepository
import javax.inject.Inject

class SavePointUseCase @Inject constructor(
    private val pointRepository: PointRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        worldPosition: Vector3,
        origin: Vector3,
        label: String
    ): MeasurePoint {
        val lastPoint = pointRepository.getLastPointForSession(sessionId)
        val pointCount = pointRepository.getPointCountForSession(sessionId)

        val relativeToOrigin = worldPosition - origin

        val relativeToPrevious = lastPoint?.let { worldPosition - it.worldPosition }
        val distanceToPrevious = lastPoint?.let { worldPosition.distanceTo(it.worldPosition) }

        val point = MeasurePoint(
            sessionId = sessionId,
            orderIndex = pointCount,
            worldPosition = worldPosition,
            relativeToOrigin = relativeToOrigin,
            previousPointId = lastPoint?.id,
            relativeToPrevious = relativeToPrevious,
            distanceToPrevious = distanceToPrevious,
            label = label
        )

        val pointId = pointRepository.savePoint(point)
        return point.copy(id = pointId)
    }
}
