package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CapabilityStatus {
    LOCKED,
    PREVIEW,
    AVAILABLE,
    MASTERED
}

enum class SupportLanguage(val displayName: String, val hint: String) {
    ENGLISH_DIRECT("Direct English", "Standard English explanations"),
    HINGLISH("Hinglish (Hindi in Latin script)", "English concepts with friendly Hindi explanations"),
    SIMPLE_ENGLISH("Simple English", "Short words, gentle explanations for absolute beginners")
}

@Entity(tableName = "capabilities")
data class CapabilityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val category: String, // "SUBJECT", "TOOL", "WORKSPACE"
    val requiredTrack: String,
    val requiredLessonsCount: Int,
    val requiredMasteryPercent: Int,
    val status: String = CapabilityStatus.LOCKED.name,
    val unlockedAt: Long = 0L,
    val unlockMessage: String = ""
)

data class UnlockCelebrationEvent(
    val capabilityId: String,
    val title: String,
    val description: String,
    val iconName: String,
    val targetDestination: String? = null
)
