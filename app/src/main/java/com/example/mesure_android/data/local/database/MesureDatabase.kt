package com.example.mesure_android.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mesure_android.data.local.database.dao.PointDao
import com.example.mesure_android.data.local.database.dao.SessionDao
import com.example.mesure_android.data.local.database.dao.SiteDao
import com.example.mesure_android.data.local.database.entity.PointEntity
import com.example.mesure_android.data.local.database.entity.SessionEntity
import com.example.mesure_android.data.local.database.entity.SiteEntity

@Database(
    entities = [
        SiteEntity::class,
        SessionEntity::class,
        PointEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MesureDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun sessionDao(): SessionDao
    abstract fun pointDao(): PointDao
}
