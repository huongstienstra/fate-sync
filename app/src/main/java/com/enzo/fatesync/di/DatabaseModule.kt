package com.enzo.fatesync.di

import android.content.Context
import androidx.room.Room
import com.enzo.fatesync.data.local.AnalysisDao
import com.enzo.fatesync.data.local.FateSyncDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): FateSyncDatabase {
        return Room.databaseBuilder(
            context,
            FateSyncDatabase::class.java,
            "fate_sync_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAnalysisDao(database: FateSyncDatabase): AnalysisDao {
        return database.analysisDao()
    }
}
