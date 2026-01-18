package com.enzo.fatesync.domain.usecase

import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.domain.model.FaceData
import com.enzo.fatesync.domain.model.LandmarkType
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

class CompatibilityCalculator @Inject constructor() {

    fun calculate(face1: FaceData, face2: FaceData): CompatibilityResult {
        val scores = mutableMapOf<String, Float>()

        // 1. Face shape similarity (based on width/height ratio)
        val faceRatioScore = calculateFaceRatioSimilarity(face1, face2)
        scores["Face Shape"] = faceRatioScore

        // 2. Smile compatibility
        val smileScore = calculateSmileSimilarity(face1, face2)
        scores["Smile"] = smileScore

        // 3. Eye openness compatibility
        val eyeScore = calculateEyeSimilarity(face1, face2)
        scores["Eyes"] = eyeScore

        // 4. Facial symmetry comparison
        val symmetryScore = calculateSymmetryMatch(face1, face2)
        scores["Symmetry"] = symmetryScore

        // 5. Golden ratio alignment
        val goldenRatioScore = calculateGoldenRatioScore(face1, face2)
        scores["Harmony"] = goldenRatioScore

        // Calculate overall score (weighted average)
        val overallScore = calculateOverallScore(scores)

        // Generate compatibility message
        val message = generateMessage(overallScore)
        val details = generateDetails(scores)

        return CompatibilityResult(
            overallScore = overallScore,
            categoryScores = scores,
            message = message,
            details = details
        )
    }

    private fun calculateFaceRatioSimilarity(face1: FaceData, face2: FaceData): Float {
        val ratio1 = face1.faceWidth.toFloat() / face1.faceHeight.toFloat()
        val ratio2 = face2.faceWidth.toFloat() / face2.faceHeight.toFloat()

        val difference = abs(ratio1 - ratio2)
        // Convert difference to score (smaller difference = higher score)
        return ((1f - min(difference, 1f)) * 100).coerceIn(65f, 100f)
    }

    private fun calculateSmileSimilarity(face1: FaceData, face2: FaceData): Float {
        val smile1 = face1.smilingProbability ?: 0.5f
        val smile2 = face2.smilingProbability ?: 0.5f

        // Both smiling or both not smiling = higher compatibility
        val difference = abs(smile1 - smile2)
        val baseScore = (1f - difference) * 100

        // Bonus if both are smiling
        val bonus = if (smile1 > 0.5f && smile2 > 0.5f) 10f else 0f

        return (baseScore + bonus).coerceIn(65f, 100f)
    }

    private fun calculateEyeSimilarity(face1: FaceData, face2: FaceData): Float {
        val leftEye1 = face1.leftEyeOpenProbability ?: 0.5f
        val rightEye1 = face1.rightEyeOpenProbability ?: 0.5f
        val leftEye2 = face2.leftEyeOpenProbability ?: 0.5f
        val rightEye2 = face2.rightEyeOpenProbability ?: 0.5f

        val avg1 = (leftEye1 + rightEye1) / 2
        val avg2 = (leftEye2 + rightEye2) / 2

        val difference = abs(avg1 - avg2)
        return ((1f - difference) * 100).coerceIn(65f, 100f)
    }

    private fun calculateSymmetryMatch(face1: FaceData, face2: FaceData): Float {
        // Calculate symmetry based on head angles
        val angleX1 = abs(face1.headEulerAngleX)
        val angleY1 = abs(face1.headEulerAngleY)
        val angleX2 = abs(face2.headEulerAngleX)
        val angleY2 = abs(face2.headEulerAngleY)

        // Lower angles = more symmetric pose
        val symmetry1 = 1f - min((angleX1 + angleY1) / 60f, 1f)
        val symmetry2 = 1f - min((angleX2 + angleY2) / 60f, 1f)

        val avgSymmetry = (symmetry1 + symmetry2) / 2
        return (avgSymmetry * 100).coerceIn(65f, 100f)
    }

    private fun calculateGoldenRatioScore(face1: FaceData, face2: FaceData): Float {
        val goldenRatio = 1.618f

        // Calculate how close each face is to golden ratio proportions
        val ratio1 = face1.faceHeight.toFloat() / face1.faceWidth.toFloat()
        val ratio2 = face2.faceHeight.toFloat() / face2.faceWidth.toFloat()

        val diff1 = abs(ratio1 - goldenRatio) / goldenRatio
        val diff2 = abs(ratio2 - goldenRatio) / goldenRatio

        val score1 = 1f - min(diff1, 1f)
        val score2 = 1f - min(diff2, 1f)

        return ((score1 + score2) / 2 * 100).coerceIn(65f, 100f)
    }

    private fun calculateOverallScore(scores: Map<String, Float>): Int {
        val weights = mapOf(
            "Face Shape" to 0.25f,
            "Smile" to 0.25f,
            "Eyes" to 0.15f,
            "Symmetry" to 0.15f,
            "Harmony" to 0.20f
        )

        var weightedSum = 0f
        var totalWeight = 0f

        scores.forEach { (category, score) ->
            val weight = weights[category] ?: 0.2f
            weightedSum += score * weight
            totalWeight += weight
        }

        return (weightedSum / totalWeight).toInt().coerceIn(65, 98)
    }

    private fun generateMessage(score: Int): String {
        return when {
            score >= 90 -> "Destined Soulmates!"
            score >= 80 -> "Amazing Connection!"
            score >= 75 -> "Great Compatibility!"
            score >= 70 -> "Good Match!"
            else -> "Interesting Pair!"
        }
    }

    private fun generateDetails(scores: Map<String, Float>): List<String> {
        val details = mutableListOf<String>()

        scores.forEach { (category, score) ->
            val description = when {
                score >= 90 -> "Exceptional $category compatibility"
                score >= 80 -> "Strong $category alignment"
                score >= 75 -> "Good $category match"
                else -> "Unique $category qualities"
            }
            details.add(description)
        }

        return details
    }
}
