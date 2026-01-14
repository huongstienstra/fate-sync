package com.enzo.fatesync.domain.repository

import android.graphics.Bitmap
import com.enzo.fatesync.domain.model.AnalysisResult
import com.enzo.fatesync.domain.model.FaceData
import kotlinx.coroutines.flow.Flow

interface FaceAnalysisRepository {
    suspend fun detectFaces(bitmap: Bitmap): List<FaceData>
    suspend fun analyzeCompatibility(face1: FaceData, face2: FaceData): AnalysisResult
    suspend fun generateFortune(analysisResult: AnalysisResult): String
    fun getCachedResults(): Flow<List<AnalysisResult>>
    suspend fun saveResult(result: AnalysisResult)
}
