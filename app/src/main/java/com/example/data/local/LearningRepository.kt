package com.example.data.local

import com.example.curriculum.CurriculumRegistry
import com.example.data.model.AchievementEntity
import com.example.data.model.ApiRequestHistoryEntity
import com.example.data.model.CapabilityEntity
import com.example.data.model.DailyMilestoneEvent
import com.example.data.model.LessonProgressEntity
import com.example.data.model.MistakeRecordEntity
import com.example.data.model.SavedCodeSnippetEntity
import com.example.data.model.SavedNoteEntity
import com.example.data.model.SpacedRepetitionEntity
import com.example.data.model.UnlockCelebrationEvent
import com.example.data.model.UserProfileEntity
import com.example.learning.AdaptiveMasteryEngine
import com.example.learning.CapabilityEngine
import kotlinx.coroutines.flow.Flow

class LearningRepository(private val dao: LearningDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allProgress: Flow<List<LessonProgressEntity>> = dao.getAllLessonProgress()
    val allCapabilities: Flow<List<CapabilityEntity>> = dao.getAllCapabilities()
    val unresolvedMistakes: Flow<List<MistakeRecordEntity>> = dao.getUnresolvedMistakes()
    val dueReviews: Flow<List<SpacedRepetitionEntity>> = dao.getDueReviews()
    val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val notes: Flow<List<SavedNoteEntity>> = dao.getAllNotes()
    val savedCode: Flow<List<SavedCodeSnippetEntity>> = dao.getAllSavedCode()
    val apiHistory: Flow<List<ApiRequestHistoryEntity>> = dao.getApiHistory()

    suspend fun initializeIfFirstRun() {
        var profile = dao.getUserProfileSync()
        if (profile == null) {
            profile = UserProfileEntity(currentLessonId = CurriculumRegistry.getFirstLesson().id)
            dao.insertOrUpdateProfile(profile)

            // Seed capabilities
            dao.insertDefaultCapabilities(CapabilityEngine.defaultCapabilities)

            // Seed initial achievements
            val defaultAchievements = listOf(
                AchievementEntity("ach_first_lesson", "First Step from Zero", "Complete your first lesson in Learning Lab", 50),
                AchievementEntity("ach_100_xp", "Century Club", "Accumulate 100 total XP", 50),
                AchievementEntity("ach_streak_3", "Spark of Discipline", "Maintain a 3-day learning streak", 100),
                AchievementEntity("ach_first_speaking", "Voice of Code", "Successfully complete a speaking challenge", 75),
                AchievementEntity("ach_first_python", "Hello World", "Run and pass your first Python test suite", 100),
                AchievementEntity("ach_first_project", "Junior Builder", "Complete an industry lab project", 150)
            )
            dao.insertDefaultAchievements(defaultAchievements)

            // ONLY THE FIRST LESSON IS UNLOCKED! Everything else starts LOCKED!
            val allLessons = CurriculumRegistry.getAllLessons()
            val initialProgress = allLessons.mapIndexed { idx, lesson ->
                LessonProgressEntity(
                    lessonId = lesson.id,
                    trackName = lesson.track.name,
                    isUnlocked = (idx == 0), // Only lesson 0 is unlocked
                    isCompleted = false
                )
            }
            dao.insertAllProgress(initialProgress)
        } else {
            // Ensure capabilities are present
            dao.insertDefaultCapabilities(CapabilityEngine.defaultCapabilities)
        }
    }

    suspend fun recordLessonCompletion(
        lessonId: String,
        trackName: String,
        stars: Int,
        mastery: Int,
        xpEarned: Int
    ): List<UnlockCelebrationEvent> {
        val currentProgress = dao.getLessonProgressSync(lessonId)
        val newProgress = LessonProgressEntity(
            lessonId = lessonId,
            trackName = trackName,
            isUnlocked = true,
            isCompleted = true,
            starsEarned = maxOf(currentProgress?.starsEarned ?: 0, stars),
            masteryPercent = maxOf(currentProgress?.masteryPercent ?: 0, mastery),
            attemptsCount = (currentProgress?.attemptsCount ?: 0) + 1,
            lastAttemptTime = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(newProgress)

        // Unlock next sequential lesson
        val nextLesson = CurriculumRegistry.getNextLesson(lessonId)
        if (nextLesson != null) {
            val nextProg = dao.getLessonProgressSync(nextLesson.id)
            if (nextProg == null || !nextProg.isUnlocked) {
                dao.insertOrUpdateProgress(
                    LessonProgressEntity(
                        lessonId = nextLesson.id,
                        trackName = nextLesson.track.name,
                        isUnlocked = true,
                        isCompleted = nextProg?.isCompleted ?: false
                    )
                )
            }
            dao.updateCurrentLessonId(nextLesson.id)
        }

        // Evaluate Capabilities Unlock Engine
        val allCompleted = dao.getAllLessonProgressSync()
        val currentCapabilities = dao.getAllCapabilitiesSync()
        val (updatedCapabilities, newEvents) = CapabilityEngine.evaluateUnlocks(currentCapabilities, allCompleted)
        if (newEvents.isNotEmpty()) {
            dao.updateCapabilities(updatedCapabilities)
        }

        // Update User Profile XP, Daily Target & Streak via AdaptiveMasteryEngine
        dao.getUserProfileSync()?.let { currentProfile ->
            val (updatedProfile, milestone) = AdaptiveMasteryEngine.recordAction(
                profile = currentProfile,
                xpEarned = xpEarned
            )
            dao.insertOrUpdateProfile(updatedProfile)

            // Check achievements
            dao.unlockAchievement("ach_first_lesson")
            if (updatedProfile.currentXp >= 100) dao.unlockAchievement("ach_100_xp")
            if (updatedProfile.streakCount >= 3) dao.unlockAchievement("ach_streak_3")

            val allEvents = newEvents.toMutableList()
            if (milestone != null) {
                allEvents.add(
                    UnlockCelebrationEvent(
                        capabilityId = if (milestone.isDoubleDay) "milestone_doubleday" else "milestone_dailytarget",
                        title = milestone.title,
                        description = milestone.description,
                        iconName = if (milestone.isDoubleDay) "ic_fire" else "ic_target"
                    )
                )
            }
            return allEvents
        }

        return newEvents
    }

    suspend fun recordLearningAction(xpEarned: Int = 10): DailyMilestoneEvent? {
        val currentProfile = dao.getUserProfileSync() ?: return null
        val (updatedProfile, milestone) = AdaptiveMasteryEngine.recordAction(currentProfile, xpEarned)
        dao.insertOrUpdateProfile(updatedProfile)
        return milestone
    }

    suspend fun setSupportLanguage(lang: String) {
        dao.updateSupportLanguage(lang)
    }

    suspend fun recordMistake(
        lessonId: String,
        concept: String,
        question: String,
        userAnswer: String,
        correctAnswer: String,
        mistakeType: String,
        explanation: String
    ) {
        dao.insertMistake(
            MistakeRecordEntity(
                lessonId = lessonId,
                concept = concept,
                question = question,
                userAnswer = userAnswer,
                correctAnswer = correctAnswer,
                mistakeType = mistakeType,
                explanation = explanation
            )
        )

        dao.insertOrUpdateSpacedItem(
            SpacedRepetitionEntity(
                id = "${lessonId}_${concept.hashCode()}",
                concept = concept,
                prompt = question,
                answer = correctAnswer,
                lessonId = lessonId,
                intervalDays = 1,
                repetitions = 0,
                nextReviewDate = System.currentTimeMillis() + (12 * 3600 * 1000L)
            )
        )
    }

    suspend fun resolveMistake(id: Long) {
        dao.resolveMistake(id)
        recordLearningAction(xpEarned = 15)
    }

    suspend fun updateSpacedItem(item: SpacedRepetitionEntity) {
        dao.insertOrUpdateSpacedItem(item)
        recordLearningAction(xpEarned = 10)
    }

    suspend fun addHeart() {
        dao.getUserProfileSync()?.let {
            if (it.hearts < it.maxHearts) {
                dao.insertOrUpdateProfile(it.copy(hearts = it.hearts + 1))
            }
        }
    }

    suspend fun deductHeart(): Boolean {
        val profile = dao.getUserProfileSync() ?: return false
        if (profile.hearts > 0) {
            dao.insertOrUpdateProfile(profile.copy(hearts = profile.hearts - 1))
            return true
        }
        return false
    }

    suspend fun refillHearts() {
        dao.getUserProfileSync()?.let {
            dao.insertOrUpdateProfile(it.copy(hearts = it.maxHearts))
        }
    }

    suspend fun saveNote(title: String, content: String, tag: String, lessonId: String?) {
        dao.insertNote(SavedNoteEntity(title = title, content = content, tag = tag, lessonId = lessonId))
    }

    suspend fun deleteNote(id: Long) = dao.deleteNote(id)

    suspend fun saveCode(title: String, code: String, output: String) {
        dao.insertCode(SavedCodeSnippetEntity(title = title, code = code, output = output))
    }

    suspend fun saveApiHistory(
        name: String,
        method: String,
        url: String,
        headersJson: String,
        bodyJson: String,
        responseCode: Int,
        responseBody: String,
        latencyMs: Long
    ) {
        dao.insertApiHistory(
            ApiRequestHistoryEntity(
                name = name,
                method = method,
                url = url,
                headersJson = headersJson,
                bodyJson = bodyJson,
                responseCode = responseCode,
                responseBody = responseBody,
                latencyMs = latencyMs
            )
        )
    }

    suspend fun unlockPlacementLevel(track: String) {
        dao.getUserProfileSync()?.let {
            dao.insertOrUpdateProfile(it.copy(activeTrack = track, isPlacementCompleted = true))
        }
        val targetTrack = CurriculumRegistry.tracks.find { it.name == track }
        if (targetTrack != null) {
            val eligibleTracks = CurriculumRegistry.tracks.filter { it.orderIndex <= targetTrack.orderIndex }
            eligibleTracks.forEach { t ->
                CurriculumRegistry.units.filter { it.track == t }.forEach { unit ->
                    unit.lessons.forEach { lesson ->
                        dao.insertOrUpdateProgress(
                            LessonProgressEntity(
                                lessonId = lesson.id,
                                trackName = t.name,
                                isUnlocked = true,
                                isCompleted = false
                            )
                        )
                    }
                }
            }

            // Update capabilities to reflect placement
            val allProg = dao.getAllLessonProgressSync()
            val caps = dao.getAllCapabilitiesSync()
            val (updated, _) = CapabilityEngine.evaluateUnlocks(caps, allProg)
            dao.updateCapabilities(updated)
        }
    }

    suspend fun clearAllUserData() {
        dao.clearProgress()
        dao.clearMistakes()
        dao.clearSpacedRepetition()
        dao.clearNotes()
        dao.clearSavedCode()
        dao.clearApiHistory()
        dao.clearCapabilities()
        dao.insertOrUpdateProfile(UserProfileEntity(currentLessonId = CurriculumRegistry.getFirstLesson().id))
        initializeIfFirstRun()
    }
}
