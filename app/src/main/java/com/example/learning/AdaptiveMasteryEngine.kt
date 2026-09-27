package com.example.learning

import com.example.data.model.DailyMilestoneEvent
import com.example.data.model.UserProfileEntity
import java.util.Calendar

object AdaptiveMasteryEngine {

    fun calculateStars(totalExercises: Int, mistakesCount: Int, hintsUsed: Int): Int {
        if (totalExercises == 0) return 3
        val penalty = (mistakesCount * 2) + hintsUsed
        return when {
            penalty == 0 -> 3
            penalty <= 2 -> 2
            penalty <= 4 -> 1
            else -> 1
        }
    }

    fun calculateMasteryPercent(totalExercises: Int, correctFirstTry: Int): Int {
        if (totalExercises == 0) return 100
        val ratio = (correctFirstTry.toFloat() / totalExercises.toFloat()) * 100f
        return ratio.toInt().coerceIn(0, 100)
    }

    fun calculateStreak(currentProfile: UserProfileEntity): Int {
        val now = Calendar.getInstance()
        val lastActive = Calendar.getInstance().apply { timeInMillis = currentProfile.lastActiveDate }

        val sameDay = now.get(Calendar.YEAR) == lastActive.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastActive.get(Calendar.DAY_OF_YEAR)

        if (sameDay) {
            return currentProfile.streakCount
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val wasYesterday = yesterday.get(Calendar.YEAR) == lastActive.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == lastActive.get(Calendar.DAY_OF_YEAR)

        return if (wasYesterday) {
            currentProfile.streakCount + 1
        } else {
            // Missed a day
            1
        }
    }

    fun calculateLevel(currentXp: Int): Pair<Int, Int> {
        // Level curve: Level 1 = 0..100, Level 2 = 101..250, Level 3 = 251..450, etc.
        var level = 1
        var threshold = 100
        var accumulated = 0

        while (currentXp >= accumulated + threshold) {
            accumulated += threshold
            level += 1
            threshold = (threshold * 1.3f).toInt()
        }

        return Pair(level, accumulated + threshold)
    }

    fun computeAdaptiveDailyTarget(level: Int, weakAreasCount: Int, streak: Int): Int {
        val base = when {
            level <= 1 -> 5
            level <= 3 -> 8
            else -> 10
        }
        val streakBonus = if (streak >= 7) 2 else 0
        val weakBonus = if (weakAreasCount >= 3) 1 else 0
        return (base + streakBonus + weakBonus).coerceIn(5, 15)
    }

    fun checkAndResetDay(profile: UserProfileEntity, weakAreasCount: Int = 0): UserProfileEntity {
        val now = Calendar.getInstance()
        val lastAction = Calendar.getInstance().apply { timeInMillis = profile.lastActionDate }

        val sameDay = now.get(Calendar.YEAR) == lastAction.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastAction.get(Calendar.DAY_OF_YEAR)

        return if (sameDay) {
            profile
        } else {
            val newTarget = computeAdaptiveDailyTarget(profile.level, weakAreasCount, profile.streakCount)
            profile.copy(
                dailyActionsCompleted = 0,
                dailyGoalCompletedToday = false,
                dailyTargetActions = newTarget,
                lastActionDate = System.currentTimeMillis()
            )
        }
    }

    fun recordAction(
        profile: UserProfileEntity,
        xpEarned: Int,
        weakAreasCount: Int = 0
    ): Pair<UserProfileEntity, DailyMilestoneEvent?> {
        val freshProfile = checkAndResetDay(profile, weakAreasCount)
        val newActionsCompleted = freshProfile.dailyActionsCompleted + 1
        var totalXp = freshProfile.currentXp + xpEarned
        var milestoneEvent: DailyMilestoneEvent? = null
        var goalCompletedToday = freshProfile.dailyGoalCompletedToday

        // Check milestones
        if (newActionsCompleted == freshProfile.dailyTargetActions && !goalCompletedToday) {
            goalCompletedToday = true
            val bonus = 50
            totalXp += bonus
            milestoneEvent = DailyMilestoneEvent(
                title = "🎯 Daily Target Reached!",
                description = "You completed your minimum target of ${freshProfile.dailyTargetActions} actions today! Keep going — there is no limit on daily learning.",
                bonusXp = bonus,
                isDoubleDay = false
            )
        } else if (newActionsCompleted == freshProfile.dailyTargetActions * 2 && freshProfile.dailyTargetActions > 0) {
            val bonus = 100
            totalXp += bonus
            milestoneEvent = DailyMilestoneEvent(
                title = "🔥 DOUBLE DAY ACHIEVED!",
                description = "Incredible momentum! You completed $newActionsCompleted actions (2x your minimum goal). Overachievement rewards unlocked!",
                bonusXp = bonus,
                isDoubleDay = true
            )
        }

        val (newLevel, nextThreshold) = calculateLevel(totalXp)
        val newStreak = calculateStreak(freshProfile)

        val updatedProfile = freshProfile.copy(
            currentXp = totalXp,
            level = newLevel,
            nextLevelXp = nextThreshold,
            streakCount = newStreak,
            longestStreak = maxOf(freshProfile.longestStreak, newStreak),
            dailyActionsCompleted = newActionsCompleted,
            dailyGoalCompletedToday = goalCompletedToday,
            lastActiveDate = System.currentTimeMillis(),
            lastActionDate = System.currentTimeMillis()
        )

        return Pair(updatedProfile, milestoneEvent)
    }
}

