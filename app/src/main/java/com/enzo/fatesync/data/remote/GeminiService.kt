package com.enzo.fatesync.data.remote

import android.util.Log
import com.enzo.fatesync.BuildConfig
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeminiService"

@Singleton
class GeminiService @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 500
        }
    )

    suspend fun generateCompatibilityInsight(result: CompatibilityResult): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(result)
                Log.d(TAG, "Sending prompt to Gemini...")

                val response = model.generateContent(prompt)
                val text = response.text ?: getFallbackInsight(result)

                Log.d(TAG, "Gemini response received: ${text.take(100)}...")
                text
            } catch (e: Exception) {
                Log.e(TAG, "Error generating insight", e)
                getFallbackInsight(result)
            }
        }
    }

    private fun buildPrompt(result: CompatibilityResult): String {
        val categoryBreakdown = result.categoryScores.entries.joinToString("\n") { (category, score) ->
            "- $category: ${score.toInt()}%"
        }

        return """
            You are a fun, playful relationship compatibility advisor for an entertainment app called FateSync.
            Based on facial analysis, generate a short, engaging compatibility reading.

            Compatibility Score: ${result.overallScore}%

            Category Breakdown:
            $categoryBreakdown

            Guidelines:
            - Be positive, fun, and lighthearted
            - Keep it to 2-3 short paragraphs
            - Use romantic and mystical language
            - Include specific observations based on the scores
            - End with an encouraging message
            - Don't mention this is AI-generated or based on facial analysis
            - Write as if you're a fortune teller or relationship guru

            Generate the compatibility reading:
        """.trimIndent()
    }

    private fun getFallbackInsight(result: CompatibilityResult): String {
        return when {
            result.overallScore >= 90 -> "The stars have aligned perfectly for you two! Your connection radiates with an extraordinary harmony that's rare to find. This is a bond that transcends the ordinary - cherish it and nurture it, for you've found something truly special."
            result.overallScore >= 80 -> "What a beautiful match! Your energies complement each other wonderfully, creating a natural flow of understanding and affection. There's a magnetic quality to your connection that promises deep conversations and shared adventures."
            result.overallScore >= 70 -> "There's genuine warmth between you two! Your compatibility shows a solid foundation with plenty of room for growth. The subtle differences in your energies actually create an intriguing balance - opposites attracting in the best way."
            result.overallScore >= 60 -> "An interesting pairing with hidden potential! While you may need to work a bit harder to sync your rhythms, the effort will be rewarding. The universe often pairs people who have much to teach each other."
            else -> "Every connection has its own unique magic! While the cosmic alignment suggests some challenges, remember that the most meaningful relationships often require patience and understanding. Your journey together could be one of beautiful growth."
        }
    }
}
