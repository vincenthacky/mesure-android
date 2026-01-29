package com.example.mesure_android.di

import android.content.Context
import androidx.room.Room
import com.example.mesure_android.data.local.database.MesureDatabase
import com.example.mesure_android.data.local.database.dao.PointDao
import com.example.mesure_android.data.local.database.dao.SessionDao
import com.example.mesure_android.data.local.database.dao.SiteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMesureDatabase(
        @ApplicationContext context: Context
    ): MesureDatabase {
        return Room.databaseBuilder(
            context,
            MesureDatabase::class.java,
            "mesure_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSiteDao(database: MesureDatabase): SiteDao {
        return database.siteDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: MesureDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun providePointDao(database: MesureDatabase): PointDao {
        return database.pointDao()
    }
}
