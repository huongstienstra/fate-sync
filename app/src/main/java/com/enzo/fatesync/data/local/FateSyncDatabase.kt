package com.enzo.fatesync.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AnalysisEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FateSyncDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}
