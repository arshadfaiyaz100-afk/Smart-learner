package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.ApiRequestHistoryEntity
import com.example.data.model.CapabilityEntity
import com.example.data.model.LessonProgressEntity
import com.example.data.model.MistakeRecordEntity
import com.example.data.model.SavedCodeSnippetEntity
import com.example.data.model.SavedNoteEntity
import com.example.data.model.SpacedRepetitionEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        LessonProgressEntity::class,
        MistakeRecordEntity::class,
        SpacedRepetitionEntity::class,
        SavedNoteEntity::class,
        SavedCodeSnippetEntity::class,
        ApiRequestHistoryEntity::class,
        AchievementEntity::class,
        CapabilityEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun learningDao(): LearningDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learning_lab_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
