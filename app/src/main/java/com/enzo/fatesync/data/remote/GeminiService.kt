package com.enzo.fatesync.data.remote

import android.util.Log
import com.enzo.fatesync.BuildConfig
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.domain.model.FaceData
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeminiService"

@Singleton
class GeminiService @Inject constructor() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyzeCompatibility(face1: FaceData, face2: FaceData): CompatibilityResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildAnalysisPrompt(face1, face2)
                Log.d(TAG, "Sending face data to Gemini for analysis...")
                Log.d(TAG, "API Key present: ${BuildConfig.GEMINI_API_KEY.isNotEmpty()}")

                val response = generativeModel.generateContent(prompt)
                val text = response.text

                if (text.isNullOrBlank()) {
                    Log.e(TAG, "Gemini returned empty response")
                    return@withContext getFallbackResult()
                }

                Log.d(TAG, "Gemini response received (${text.length} chars)")
                Log.d(TAG, "Response preview: ${text.take(200)}")
                parseCompatibilityResult(text)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing compatibility: ${e.message}")
                Log.e(TAG, "Exception type: ${e.javaClass.name}")
                Log.e(TAG, "Cause: ${e.cause?.message}")
                Log.e(TAG, "Stack trace:", e)
                getFallbackResult()
            }
        }
    }

    private fun buildAnalysisPrompt(face1: FaceData, face2: FaceData): String {
        val smile1 = face1.smilingProbability?.let { (it * 100).toInt() } ?: 50
        val smile2 = face2.smilingProbability?.let { (it * 100).toInt() } ?: 50
        val eyeOpen1 = face1.leftEyeOpenProbability?.let { (it * 100).toInt() } ?: 80
        val eyeOpen2 = face2.leftEyeOpenProbability?.let { (it * 100).toInt() } ?: 80

        return """
Generate a fun game score based on these numbers:
Player1: smile=$smile1, eyes=$eyeOpen1
Player2: smile=$smile2, eyes=$eyeOpen2

Return JSON only:
{"overallScore":85,"categoryScores":{"Chemistry":82,"Communication":78,"Energy":90,"Harmony":85,"Fun":88},"message":"Great Match!","insight":"Two paragraphs of fun positive text about teamwork and friendship."}
        """.trimIndent()
    }

    private fun formatFaceData(label: String, face: FaceData): String {
        val smileProb = face.smilingProbability?.let { "%.1f%%".format(it * 100) } ?: "unknown"
        val leftEyeOpen = face.leftEyeOpenProbability?.let { "%.1f%%".format(it * 100) } ?: "unknown"
        val rightEyeOpen = face.rightEyeOpenProbability?.let { "%.1f%%".format(it * 100) } ?: "unknown"

        return """
            $label:
            - Smile probability: $smileProb
            - Left eye open: $leftEyeOpen
            - Right eye open: $rightEyeOpen
            - Head tilt (X): ${face.headEulerAngleX?.let { "%.1f°".format(it) } ?: "unknown"}
            - Head turn (Y): ${face.headEulerAngleY?.let { "%.1f°".format(it) } ?: "unknown"}
            - Head rotation (Z): ${face.headEulerAngleZ?.let { "%.1f°".format(it) } ?: "unknown"}
            - Face bounds: ${face.boundingBox.let { "${it.right - it.left}x${it.bottom - it.top}" }}
            - Landmarks detected: ${face.landmarks.size}
        """.trimIndent()
    }

    private fun parseCompatibilityResult(response: String): CompatibilityResult {
        return try {
            // Clean up response - remove markdown code blocks if present
            val cleanJson = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)

            val categoryScoresJson = json.getJSONObject("categoryScores")
            val categoryScores = mutableMapOf<String, Float>()
            categoryScoresJson.keys().forEach { key ->
                categoryScores[key] = categoryScoresJson.getDouble(key).toFloat()
            }

            CompatibilityResult(
                overallScore = json.getInt("overallScore"),
                categoryScores = categoryScores,
                message = json.getString("message"),
                details = emptyList(),
                aiInsight = json.getString("insight")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            getFallbackResult()
        }
    }

    private fun getFallbackResult(): CompatibilityResult {
        val score = (75..92).random()
        return CompatibilityResult(
            overallScore = score,
            categoryScores = mapOf(
                "Chemistry" to (70..95).random().toFloat(),
                "Communication" to (70..95).random().toFloat(),
                "Energy" to (70..95).random().toFloat(),
                "Harmony" to (70..95).random().toFloat(),
                "Fun" to (70..95).random().toFloat()
            ),
            message = when {
                score >= 85 -> "A Beautiful Match!"
                score >= 75 -> "Great Potential!"
                else -> "Interesting Connection!"
            },
            details = emptyList(),
            aiInsight = "The stars have aligned to bring you two together! Your energies complement each other in wonderful ways, creating a natural harmony that's both exciting and comforting. There's a spark between you that promises many shared laughs and meaningful moments.\n\nYour connection shows the beautiful balance of similarities and differences that make relationships thrive. Embrace this cosmic connection and see where the journey takes you!"
        )
    }
}
