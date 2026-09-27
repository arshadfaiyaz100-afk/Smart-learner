package com.example

import com.example.curriculum.PlacementTestEngine
import com.example.data.model.LearningTrack
import com.example.data.model.SpacedRepetitionEntity
import com.example.learning.AdaptiveMasteryEngine
import com.example.learning.SpacedRepetitionEngine
import com.example.python.PythonEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testAdaptiveMasteryStars() {
    assertEquals(3, AdaptiveMasteryEngine.calculateStars(5, 0, 0))
    assertEquals(2, AdaptiveMasteryEngine.calculateStars(5, 1, 0))
    assertEquals(1, AdaptiveMasteryEngine.calculateStars(5, 3, 0))
  }

  @Test
  fun testAdaptiveMasteryLevel() {
    val (level1, next1) = AdaptiveMasteryEngine.calculateLevel(50)
    assertEquals(1, level1)

    val (level2, next2) = AdaptiveMasteryEngine.calculateLevel(150)
    assertEquals(2, level2)
  }

  @Test
  fun testSpacedRepetitionSM2() {
    val item = SpacedRepetitionEntity(
      id = "test_item",
      concept = "Variables",
      prompt = "What is a variable?",
      answer = "A labeled memory container",
      lessonId = "lesson_01",
      intervalDays = 1,
      repetitions = 0
    )

    // Quality 4 (Good) should advance repetition and set interval to 1 then 6
    val next = SpacedRepetitionEngine.calculateNextReview(item, 4)
    assertEquals(1, next.repetitions)
    assertEquals(1, next.intervalDays)

    val next2 = SpacedRepetitionEngine.calculateNextReview(next, 4)
    assertEquals(2, next2.repetitions)
    assertEquals(6, next2.intervalDays)

    // Failure (Quality 1) resets repetitions to 0 and interval to 1
    val failed = SpacedRepetitionEngine.calculateNextReview(next2, 1)
    assertEquals(0, failed.repetitions)
    assertEquals(1, failed.intervalDays)
  }

  @Test
  fun testPlacementDiagnostic() {
    val zero = PlacementTestEngine.determineRecommendedTrack(0)
    assertEquals(LearningTrack.ZERO_ENGLISH, zero)

    val py = PlacementTestEngine.determineRecommendedTrack(5)
    assertEquals(LearningTrack.PYTHON_MASTERY, py)
  }

  @Test
  fun testPythonEngineExecution() = runBlocking {
    val code = """
      x = 5
      y = 10
      print(x + y)
    """.trimIndent()

    val result = PythonEngine.execute(code)
    assertTrue(result.isSuccess)
    assertEquals("15", result.output.trim())
  }

  @Test
  fun testAdaptiveDailyTargetAndDoubleDay() {
    val target = AdaptiveMasteryEngine.computeAdaptiveDailyTarget(level = 1, weakAreasCount = 0, streak = 1)
    assertEquals(5, target)

    val profile = com.example.data.model.UserProfileEntity(dailyTargetActions = 5, dailyActionsCompleted = 4)
    val (updated1, milestone1) = AdaptiveMasteryEngine.recordAction(profile, xpEarned = 20)
    assertEquals(5, updated1.dailyActionsCompleted)
    assertTrue(updated1.dailyGoalCompletedToday)
    assertEquals("🎯 Daily Target Reached!", milestone1?.title)

    // Double Day test (10 completed out of 5 target)
    val profileDouble = updated1.copy(dailyActionsCompleted = 9)
    val (updated2, milestone2) = AdaptiveMasteryEngine.recordAction(profileDouble, xpEarned = 20)
    assertEquals(10, updated2.dailyActionsCompleted)
    assertTrue(milestone2?.isDoubleDay == true)
    assertEquals("🔥 DOUBLE DAY ACHIEVED!", milestone2?.title)
  }
}
