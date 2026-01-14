package com.enzo.fatesync.domain.usecase

import android.graphics.Bitmap
import com.enzo.fatesync.domain.model.AnalysisResult
import com.enzo.fatesync.domain.repository.FaceAnalysisRepository
import javax.inject.Inject

class AnalyzeFacesUseCase @Inject constructor(
    private val repository: FaceAnalysisRepository
) {
    suspend operator fun invoke(image1: Bitmap, image2: Bitmap): Result<AnalysisResult> {
        return try {
            val faces1 = repository.detectFaces(image1)
            val faces2 = repository.detectFaces(image2)

            if (faces1.isEmpty() || faces2.isEmpty()) {
                return Result.failure(Exception("Could not detect faces in one or both images"))
            }

            val result = repository.analyzeCompatibility(faces1.first(), faces2.first())
            val fortune = repository.generateFortune(result)
            val finalResult = result.copy(fortune = fortune)

            repository.saveResult(finalResult)
            Result.success(finalResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
