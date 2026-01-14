package com.enzo.fatesync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enzo.fatesync.domain.model.AnalysisResult
import com.enzo.fatesync.domain.model.FacialFeatures

@Entity(tableName = "analysis_results")
data class AnalysisEntity(
    @PrimaryKey
    val id: String,
    val compatibilityScore: Int,
    val faceShape: String,
    val eyeDistance: Float,
    val noseRatio: Float,
    val lipFullness: Float,
    val symmetryScore: Float,
    val fortune: String?,
    val timestamp: Long
) {
    fun toDomainModel(): AnalysisResult {
        return AnalysisResult(
            id = id,
            compatibilityScore = compatibilityScore,
            facialFeatures = FacialFeatures(
                faceShape = faceShape,
                eyeDistance = eyeDistance,
                noseRatio = noseRatio,
                lipFullness = lipFullness,
                symmetryScore = symmetryScore
            ),
            fortune = fortune,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomainModel(result: AnalysisResult): AnalysisEntity {
            return AnalysisEntity(
                id = result.id,
                compatibilityScore = result.compatibilityScore,
                faceShape = result.facialFeatures.faceShape,
                eyeDistance = result.facialFeatures.eyeDistance,
                noseRatio = result.facialFeatures.noseRatio,
                lipFullness = result.facialFeatures.lipFullness,
                symmetryScore = result.facialFeatures.symmetryScore,
                fortune = result.fortune,
                timestamp = result.timestamp
            )
        }
    }
}
