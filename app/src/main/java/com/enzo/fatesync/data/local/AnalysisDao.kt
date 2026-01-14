package com.enzo.fatesync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analysis_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analysis_results WHERE id = :id")
    suspend fun getResultById(id: String): AnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: AnalysisEntity)

    @Query("DELETE FROM analysis_results WHERE id = :id")
    suspend fun deleteResult(id: String)

    @Query("DELETE FROM analysis_results")
    suspend fun clearAll()
}
