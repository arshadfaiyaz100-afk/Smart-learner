package com.example.learning

import com.example.data.model.SpacedRepetitionEntity
import kotlin.math.max

object SpacedRepetitionEngine {

    /**
     * Updates an SM-2 spaced repetition record based on learner's answer quality.
     * quality rating: 0 (total blackout), 1-2 (wrong), 3 (hard), 4 (good), 5 (perfect)
     */
    fun calculateNextReview(
        current: SpacedRepetitionEntity,
        quality: Int // 0 to 5
    ): SpacedRepetitionEntity {
        val now = System.currentTimeMillis()
        var newRepetitions = current.repetitions
        var newIntervalDays = current.intervalDays
        var newConsecutive = current.consecutiveCorrect

        // SM-2 Ease Factor formula: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val delta = 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)
        var newEase = current.easeFactor + delta
        if (newEase < 1.3f) newEase = 1.3f

        if (quality >= 3) {
            // Success
            when (newRepetitions) {
                0 -> newIntervalDays = 1
                1 -> newIntervalDays = 6
                else -> newIntervalDays = (current.intervalDays * newEase).toInt()
            }
            newRepetitions += 1
            newConsecutive += 1
        } else {
            // Failure
            newRepetitions = 0
            newIntervalDays = 1
            newConsecutive = 0
        }

        val millisInDay = 86400000L
        val nextReviewMillis = now + (max(1, newIntervalDays) * millisInDay)

        return current.copy(
            intervalDays = newIntervalDays,
            easeFactor = newEase,
            repetitions = newRepetitions,
            nextReviewDate = nextReviewMillis,
            lastReviewedDate = now,
            consecutiveCorrect = newConsecutive
        )
    }
}
