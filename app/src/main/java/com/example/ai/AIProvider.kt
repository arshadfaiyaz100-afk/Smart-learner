package com.example.ai

interface AIProvider {
    val name: String
    val isOnline: Boolean
    val isAvailable: Boolean

    suspend fun askTutor(question: String, contextInfo: String): Result<String>
    suspend fun explainConcept(concept: String, level: String): Result<String>
    suspend fun reviewCode(code: String): Result<String>
    suspend fun providePronunciationTip(phrase: String): Result<String>
}
