package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProvider(private val apiKeyProvider: () -> String) : AIProvider {

    override val name: String = "Gemini Cloud (2.5 Flash)"
    override val isOnline: Boolean = true
    override val isAvailable: Boolean
        get() = apiKeyProvider().isNotBlank()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    override suspend fun askTutor(question: String, contextInfo: String): Result<String> {
        val prompt = "You are the expert Learning Lab tutor. The student is studying: $contextInfo.\nQuestion: $question\nProvide a friendly, pedagogical, concise explanation with an example."
        return callGemini(prompt)
    }

    override suspend fun explainConcept(concept: String, level: String): Result<String> {
        val prompt = "Explain the computer science/English concept '$concept' clearly for a $level learner. Keep it concise, structured, with bullet points and a brief code/text example."
        return callGemini(prompt)
    }

    override suspend fun reviewCode(code: String): Result<String> {
        val prompt = "Perform a friendly, concise code review for the following code:\n```\n$code\n```\nHighlight bugs, PEP 8/clean code best practices, and runtime complexity."
        return callGemini(prompt)
    }

    override suspend fun providePronunciationTip(phrase: String): Result<String> {
        val prompt = "Provide practical phonetics, syllable breakdown, mouth shape, and common pitfalls when pronouncing this phrase: '$phrase'."
        return callGemini(prompt)
    }

    private suspend fun callGemini(promptText: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Please add it in Settings."))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error (${response.code}): $respBody"))
            }

            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                Result.failure(Exception("Empty response received from Gemini"))
            } else {
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
