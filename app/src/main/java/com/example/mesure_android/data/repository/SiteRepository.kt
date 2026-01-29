package com.example.mesure_android.data.repository

import com.example.mesure_android.data.local.database.dao.SessionDao
import com.example.mesure_android.data.local.database.dao.SiteDao
import com.example.mesure_android.data.local.database.entity.SessionEntity
import com.example.mesure_android.data.local.database.entity.SiteEntity
import com.example.mesure_android.data.model.QrCodeData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepository @Inject constructor(
    private val siteDao: SiteDao,
    private val sessionDao: SessionDao
) {
    fun getAllSites(): Flow<List<SiteEntity>> = siteDao.getAllSites()

    suspend fun getSiteById(siteId: String): SiteEntity? = siteDao.getSiteById(siteId)

    suspend fun createOrGetSite(qrData: QrCodeData, rawJson: String): SiteEntity {
        val existingSite = siteDao.getSiteById(qrData.id)
        if (existingSite != null) {
            return existingSite
        }

        val newSite = SiteEntity(
            id = qrData.id,
            name = qrData.nom,
            latitude = qrData.lat,
            longitude = qrData.lon,
            rawQrData = rawJson
        )
        siteDao.insertSite(newSite)
        return newSite
    }

    suspend fun createNewSession(siteId: String): SessionEntity {
        val session = SessionEntity(siteId = siteId)
        val sessionId = sessionDao.insertSession(session)
        return session.copy(id = sessionId)
    }

    suspend fun getLatestSessionForSite(siteId: String): SessionEntity? {
        return sessionDao.getLatestSessionForSite(siteId)
    }

    fun getSessionsForSite(siteId: String): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsBySiteId(siteId)
    }

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(sessionId: Long): SessionEntity? = sessionDao.getSessionById(sessionId)

    suspend fun calibrateSession(
        sessionId: Long,
        originX: Float,
        originY: Float,
        originZ: Float,
        qx: Float,
        qy: Float,
        qz: Float,
        qw: Float
    ) {
        sessionDao.calibrateSession(
            sessionId = sessionId,
            isCalibrated = true,
            originX = originX,
            originY = originY,
            originZ = originZ,
            qx = qx,
            qy = qy,
            qz = qz,
            qw = qw
        )
    }

    suspend fun endSession(sessionId: Long) {
        sessionDao.endSession(sessionId, System.currentTimeMillis())
    }
}
