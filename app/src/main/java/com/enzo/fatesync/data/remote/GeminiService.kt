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

    suspend fun analyzeCompatibility(
        face1: FaceData,
        face2: FaceData,
        languageCode: String = "en"
    ): CompatibilityResult {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildAnalysisPrompt(face1, face2, languageCode)
                Log.d(TAG, "Sending face data to Gemini for analysis (language: $languageCode)...")
                Log.d(TAG, "API Key present: ${BuildConfig.GEMINI_API_KEY.isNotEmpty()}")

                val response = generativeModel.generateContent(prompt)
                val text = response.text

                if (text.isNullOrBlank()) {
                    Log.e(TAG, "Gemini returned empty response")
                    return@withContext getFallbackResult(languageCode)
                }

                Log.d(TAG, "Gemini response received (${text.length} chars)")
                Log.d(TAG, "Response preview: ${text.take(200)}")
                parseCompatibilityResult(text, languageCode)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing compatibility: ${e.message}")
                Log.e(TAG, "Exception type: ${e.javaClass.name}")
                Log.e(TAG, "Cause: ${e.cause?.message}")
                Log.e(TAG, "Stack trace:", e)
                getFallbackResult(languageCode)
            }
        }
    }

    private fun buildAnalysisPrompt(face1: FaceData, face2: FaceData, languageCode: String): String {
        val smile1 = face1.smilingProbability?.let { (it * 100).toInt() } ?: 50
        val smile2 = face2.smilingProbability?.let { (it * 100).toInt() } ?: 50
        val eyeOpen1 = face1.leftEyeOpenProbability?.let { (it * 100).toInt() } ?: 80
        val eyeOpen2 = face2.leftEyeOpenProbability?.let { (it * 100).toInt() } ?: 80

        return if (languageCode == "vi") {
            """
Tạo điểm số trò chơi vui dựa trên các con số này:
Người1: cười=$smile1, mắt=$eyeOpen1
Người2: cười=$smile2, mắt=$eyeOpen2

Chỉ trả về JSON (tất cả nội dung phải bằng tiếng Việt):
{"overallScore":85,"categoryScores":{"Hóa học":82,"Giao tiếp":78,"Năng lượng":90,"Hòa hợp":85,"Vui vẻ":88},"message":"Cặp đôi tuyệt vời!","insight":"Hai đoạn văn vui và tích cực bằng tiếng Việt về sự hợp tác và tình bạn."}
            """.trimIndent()
        } else {
            """
Generate a fun game score based on these numbers:
Player1: smile=$smile1, eyes=$eyeOpen1
Player2: smile=$smile2, eyes=$eyeOpen2

Return JSON only:
{"overallScore":85,"categoryScores":{"Chemistry":82,"Communication":78,"Energy":90,"Harmony":85,"Fun":88},"message":"Great Match!","insight":"Two paragraphs of fun positive text about teamwork and friendship."}
            """.trimIndent()
        }
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
            - Head tilt (X): ${"%.1f°".format(face.headEulerAngleX)}
            - Head turn (Y): ${"%.1f°".format(face.headEulerAngleY)}
            - Head rotation (Z): ${"%.1f°".format(face.headEulerAngleZ)}
            - Face bounds: ${face.boundingBox.let { "${it.right - it.left}x${it.bottom - it.top}" }}
            - Landmarks detected: ${face.landmarks.size}
        """.trimIndent()
    }

    private fun parseCompatibilityResult(response: String, languageCode: String): CompatibilityResult {
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
            getFallbackResult(languageCode)
        }
    }

    private fun getFallbackResult(languageCode: String = "en"): CompatibilityResult {
        val score = (75..92).random()

        return if (languageCode == "vi") {
            CompatibilityResult(
                overallScore = score,
                categoryScores = mapOf(
                    "Hóa học" to (70..95).random().toFloat(),
                    "Giao tiếp" to (70..95).random().toFloat(),
                    "Năng lượng" to (70..95).random().toFloat(),
                    "Hòa hợp" to (70..95).random().toFloat(),
                    "Vui vẻ" to (70..95).random().toFloat()
                ),
                message = when {
                    score >= 85 -> "Cặp đôi hoàn hảo!"
                    score >= 75 -> "Tiềm năng tuyệt vời!"
                    else -> "Kết nối thú vị!"
                },
                details = emptyList(),
                aiInsight = "Các vì sao đã sắp đặt để đưa hai bạn đến với nhau! Năng lượng của các bạn bổ sung cho nhau một cách tuyệt vời, tạo nên sự hòa hợp tự nhiên vừa thú vị vừa ấm áp. Có một tia sáng giữa hai bạn hứa hẹn nhiều tiếng cười và khoảnh khắc ý nghĩa.\n\nMối liên kết của các bạn cho thấy sự cân bằng tuyệt đẹp giữa những điểm tương đồng và khác biệt - điều làm cho mọi mối quan hệ thăng hoa. Hãy đón nhận kết nối định mệnh này và xem cuộc hành trình sẽ đưa các bạn đến đâu!"
            )
        } else {
            CompatibilityResult(
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
}
