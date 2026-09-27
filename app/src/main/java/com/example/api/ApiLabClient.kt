package com.example.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ApiTestResult(
    val statusCode: Int,
    val statusMessage: String,
    val responseBody: String,
    val responseHeaders: Map<String, String>,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

object ApiLabClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun executeRequest(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: String?
    ): ApiTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return@withContext ApiTestResult(
                    statusCode = 0,
                    statusMessage = "Invalid Protocol",
                    responseBody = "",
                    responseHeaders = emptyMap(),
                    latencyMs = 0,
                    isSuccess = false,
                    errorMessage = "URL must start with http:// or https://"
                )
            }

            val reqBuilder = Request.Builder().url(url)

            // Add headers
            headers.forEach { (k, v) ->
                if (k.isNotBlank()) reqBuilder.addHeader(k.trim(), v.trim())
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val reqBody = if (body != null && (method in listOf("POST", "PUT", "PATCH"))) {
                body.toRequestBody(mediaType)
            } else null

            when (method.uppercase()) {
                "GET" -> reqBuilder.get()
                "POST" -> reqBuilder.post(reqBody ?: "".toRequestBody(mediaType))
                "PUT" -> reqBuilder.put(reqBody ?: "".toRequestBody(mediaType))
                "PATCH" -> reqBuilder.patch(reqBody ?: "".toRequestBody(mediaType))
                "DELETE" -> reqBuilder.delete(reqBody)
                else -> reqBuilder.get()
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val elapsed = System.currentTimeMillis() - startTime
            val bodyString = response.body?.string() ?: ""

            val headerMap = mutableMapOf<String, String>()
            for (i in 0 until response.headers.size) {
                headerMap[response.headers.name(i)] = response.headers.value(i)
            }

            ApiTestResult(
                statusCode = response.code,
                statusMessage = response.message,
                responseBody = bodyString,
                responseHeaders = headerMap,
                latencyMs = elapsed,
                isSuccess = response.isSuccessful,
                errorMessage = if (response.isSuccessful) null else "HTTP ${response.code}: ${response.message}"
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiTestResult(
                statusCode = 0,
                statusMessage = "Client Exception",
                responseBody = "",
                responseHeaders = emptyMap(),
                latencyMs = elapsed,
                isSuccess = false,
                errorMessage = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Network connection failed"}"
            )
        }
    }

    fun maskSecret(headerValue: String): String {
        return if (headerValue.length > 8) {
            headerValue.take(4) + "••••••••" + headerValue.takeLast(4)
        } else {
            "••••••••"
        }
    }
}
