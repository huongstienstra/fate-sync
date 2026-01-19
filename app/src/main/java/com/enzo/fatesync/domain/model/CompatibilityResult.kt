package com.enzo.fatesync.domain.model

data class CompatibilityResult(
    val overallScore: Int,
    val categoryScores: Map<String, Float>,
    val message: String,
    val details: List<String>,
    val aiInsight: String? = null
)
