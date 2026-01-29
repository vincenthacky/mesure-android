package com.example.mesure_android.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mesure_android.data.local.database.entity.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity)

    @Query("SELECT * FROM sites WHERE id = :siteId")
    suspend fun getSiteById(siteId: String): SiteEntity?

    @Query("SELECT * FROM sites ORDER BY createdAt DESC")
    fun getAllSites(): Flow<List<SiteEntity>>

    @Query("DELETE FROM sites WHERE id = :siteId")
    suspend fun deleteSite(siteId: String)
}
