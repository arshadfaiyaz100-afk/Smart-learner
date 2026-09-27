package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AchievementEntity
import com.example.data.model.ApiRequestHistoryEntity
import com.example.data.model.CapabilityEntity
import com.example.data.model.LessonProgressEntity
import com.example.data.model.MistakeRecordEntity
import com.example.data.model.SavedCodeSnippetEntity
import com.example.data.model.SavedNoteEntity
import com.example.data.model.SpacedRepetitionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET currentLessonId = :lessonId WHERE id = 1")
    suspend fun updateCurrentLessonId(lessonId: String)

    @Query("UPDATE user_profile SET supportLanguage = :lang WHERE id = 1")
    suspend fun updateSupportLanguage(lang: String)

    // Capabilities (Unlock Engine)
    @Query("SELECT * FROM capabilities")
    fun getAllCapabilities(): Flow<List<CapabilityEntity>>

    @Query("SELECT * FROM capabilities")
    suspend fun getAllCapabilitiesSync(): List<CapabilityEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultCapabilities(capabilities: List<CapabilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCapabilities(capabilities: List<CapabilityEntity>)

    @Update
    suspend fun updateCapability(capability: CapabilityEntity)

    // Lesson Progress
    @Query("SELECT * FROM lesson_progress")
    fun getAllLessonProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress")
    suspend fun getAllLessonProgressSync(): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    fun getLessonProgress(lessonId: String): Flow<LessonProgressEntity?>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getLessonProgressSync(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: LessonProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllProgress(progressList: List<LessonProgressEntity>)

    // Mistakes & Weakness
    @Query("SELECT * FROM mistake_records WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedMistakes(): Flow<List<MistakeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeRecordEntity)

    @Query("UPDATE mistake_records SET isResolved = 1, reviewCount = reviewCount + 1 WHERE id = :id")
    suspend fun resolveMistake(id: Long)

    @Query("SELECT COUNT(*) FROM mistake_records WHERE isResolved = 0")
    fun getUnresolvedMistakesCount(): Flow<Int>

    // Spaced Repetition
    @Query("SELECT * FROM spaced_repetition WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getDueReviews(currentTime: Long = System.currentTimeMillis()): Flow<List<SpacedRepetitionEntity>>

    @Query("SELECT * FROM spaced_repetition")
    fun getAllSpacedRepetitionItems(): Flow<List<SpacedRepetitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSpacedItem(item: SpacedRepetitionEntity)

    // Notes
    @Query("SELECT * FROM saved_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<SavedNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SavedNoteEntity)

    @Query("DELETE FROM saved_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // Saved Code
    @Query("SELECT * FROM saved_code ORDER BY timestamp DESC")
    fun getAllSavedCode(): Flow<List<SavedCodeSnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: SavedCodeSnippetEntity)

    @Query("DELETE FROM saved_code WHERE id = :id")
    suspend fun deleteCode(id: Long)

    // API History
    @Query("SELECT * FROM api_history ORDER BY timestamp DESC LIMIT 50")
    fun getApiHistory(): Flow<List<ApiRequestHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiHistory(history: ApiRequestHistoryEntity)

    @Query("DELETE FROM api_history")
    suspend fun clearApiHistory()

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedDate = :date WHERE id = :id AND isUnlocked = 0")
    suspend fun unlockAchievement(id: String, date: Long = System.currentTimeMillis())

    // Data Export & Reset (Privacy Center)
    @Query("DELETE FROM mistake_records")
    suspend fun clearMistakes()

    @Query("DELETE FROM spaced_repetition")
    suspend fun clearSpacedRepetition()

    @Query("DELETE FROM saved_notes")
    suspend fun clearNotes()

    @Query("DELETE FROM saved_code")
    suspend fun clearSavedCode()

    @Query("DELETE FROM lesson_progress")
    suspend fun clearProgress()

    @Query("DELETE FROM capabilities")
    suspend fun clearCapabilities()
}
