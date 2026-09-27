package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "Explorer",
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 100,
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val lastHeartRegenTime: Long = System.currentTimeMillis(),
    val streakCount: Int = 1,
    val longestStreak: Int = 1,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val dailyGoalMinutes: Int = 15,
    val dailyGoalXp: Int = 50,
    val dailyGoalCompletedToday: Boolean = false,
    val isPlacementCompleted: Boolean = false,
    val activeTrack: String = LearningTrack.ZERO_ENGLISH.name,
    val supportLanguage: String = SupportLanguage.ENGLISH_DIRECT.name,
    val currentLessonId: String = "lesson_welcome_00",
    val dailyTargetActions: Int = 10,
    val dailyActionsCompleted: Int = 0,
    val lastActionDate: Long = System.currentTimeMillis()
)

data class DailyMilestoneEvent(
    val title: String,
    val description: String,
    val bonusXp: Int,
    val isDoubleDay: Boolean = false
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val trackName: String,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0, // 0 to 3
    val masteryPercent: Int = 0, // 0 to 100
    val attemptsCount: Int = 0,
    val lastAttemptTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "mistake_records")
data class MistakeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: String,
    val concept: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val mistakeType: String,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val isResolved: Boolean = false
)

@Entity(tableName = "spaced_repetition")
data class SpacedRepetitionEntity(
    @PrimaryKey val id: String, // concept or question hash
    val concept: String,
    val prompt: String,
    val answer: String,
    val lessonId: String,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis() + 86400000L,
    val lastReviewedDate: Long = System.currentTimeMillis(),
    val consecutiveCorrect: Int = 0
)

@Entity(tableName = "saved_notes")
data class SavedNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "General",
    val lessonId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_code")
data class SavedCodeSnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val code: String,
    val language: String = "python",
    val output: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_history")
data class ApiRequestHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val method: String,
    val url: String,
    val headersJson: String,
    val bodyJson: String,
    val responseCode: Int,
    val responseBody: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long = 0
)

data class ProjectItem(
    val id: String,
    val title: String,
    val track: LearningTrack,
    val level: String,
    val description: String,
    val architecture: String,
    val starterCode: String,
    val solutionCode: String,
    val testCases: List<TestCase>,
    val xpReward: Int = 100
)
