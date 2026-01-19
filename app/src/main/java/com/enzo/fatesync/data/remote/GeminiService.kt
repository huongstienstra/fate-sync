package com.enzo.fatesync.data.remote

import android.util.Log
import com.enzo.fatesync.BuildConfig
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.domain.model.FaceData
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeminiService"

@Singleton
class GeminiService @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 32
            topP = 0.9f
            maxOutputTokens = 800
        }
    )

    suspend fun analyzeCompatibility(face1: FaceData, face2: FaceData): CompatibilityResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildAnalysisPrompt(face1, face2)
                Log.d(TAG, "Sending face data to Gemini for analysis...")
                Log.d(TAG, "API Key present: ${BuildConfig.GEMINI_API_KEY.isNotEmpty()}")

                val response = model.generateContent(prompt)
                val text = response.text

                if (text.isNullOrBlank()) {
                    Log.e(TAG, "Gemini returned empty response")
                    return@withContext getFallbackResult()
                }

                Log.d(TAG, "Gemini response received (${text.length} chars)")
                Log.d(TAG, "Response preview: ${text.take(200)}")
                parseCompatibilityResult(text)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing compatibility: ${e.message}", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                getFallbackResult()
            }
        }
    }

    private fun buildAnalysisPrompt(face1: FaceData, face2: FaceData): String {
        val face1Data = formatFaceData("Person 1", face1)
        val face2Data = formatFaceData("Person 2", face2)

        return """
You are a fun compatibility game assistant. Based on facial expression data, create an entertaining compatibility score.

$face1Data

$face2Data

Respond with ONLY valid JSON (no markdown):
{"overallScore":<65-98>,"categoryScores":{"Chemistry":<60-100>,"Communication":<60-100>,"Energy":<60-100>,"Harmony":<60-100>,"Fun":<60-100>},"message":"<3-5 word fun title>","insight":"<2 short fun paragraphs about their compatibility>"}
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
