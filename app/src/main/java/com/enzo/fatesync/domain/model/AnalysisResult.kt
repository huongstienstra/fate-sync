package com.enzo.fatesync.domain.model

data class AnalysisResult(
    val id: String,
    val compatibilityScore: Int,
    val facialFeatures: FacialFeatures,
    val fortune: String?,
    val timestamp: Long
)

data class FacialFeatures(
    val faceShape: String,
    val eyeDistance: Float,
    val noseRatio: Float,
    val lipFullness: Float,
    val symmetryScore: Float
)
