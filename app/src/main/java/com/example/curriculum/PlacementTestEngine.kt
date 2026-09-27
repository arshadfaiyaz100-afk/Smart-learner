package com.example.curriculum

import com.example.data.model.ExerciseOption
import com.example.data.model.LearningTrack

data class PlacementQuestion(
    val id: String,
    val track: LearningTrack,
    val question: String,
    val options: List<ExerciseOption>,
    val correctIndex: Int,
    val explanation: String
)

object PlacementTestEngine {

    val diagnosticQuestions: List<PlacementQuestion> = listOf(
        PlacementQuestion(
            id = "place_q1",
            track = LearningTrack.ZERO_ENGLISH,
            question = "Which letter is a vowel?",
            options = listOf(
                ExerciseOption("1", "D"),
                ExerciseOption("2", "O"),
                ExerciseOption("3", "P"),
                ExerciseOption("4", "Z")
            ),
            correctIndex = 1,
            explanation = "'O' is an English vowel (A, E, I, O, U)."
        ),
        PlacementQuestion(
            id = "place_q2",
            track = LearningTrack.MATH_ZERO,
            question = "If you have 4 apples and you get 3 more, how many apples do you have?",
            options = listOf(
                ExerciseOption("1", "6"),
                ExerciseOption("2", "7"),
                ExerciseOption("3", "8"),
                ExerciseOption("4", "12")
            ),
            correctIndex = 1,
            explanation = "4 + 3 = 7 apples."
        ),
        PlacementQuestion(
            id = "place_q3",
            track = LearningTrack.COMPUTER_LITERACY,
            question = "Which computer component provides volatile working memory that clears when powered off?",
            options = listOf(
                ExerciseOption("1", "Hard Disk Drive (HDD)"),
                ExerciseOption("2", "Random Access Memory (RAM)"),
                ExerciseOption("3", "Solid State Drive (SSD)"),
                ExerciseOption("4", "Power Supply Unit (PSU)")
            ),
            correctIndex = 1,
            explanation = "RAM is high-speed volatile temporary memory."
        ),
        PlacementQuestion(
            id = "place_q4",
            track = LearningTrack.PROGRAMMING_LOGIC,
            question = "If x = 10, what does `x > 5 and x < 8` evaluate to?",
            options = listOf(
                ExerciseOption("1", "True"),
                ExerciseOption("2", "False"),
                ExerciseOption("3", "10"),
                ExerciseOption("4", "Null")
            ),
            correctIndex = 1,
            explanation = "While x > 5 is True, x < 8 is False (10 is not < 8). True and False yields False."
        ),
        PlacementQuestion(
            id = "place_q5",
            track = LearningTrack.PYTHON_MASTERY,
            question = "In Python, which built-in function returns the number of elements in a list or characters in a string?",
            options = listOf(
                ExerciseOption("1", "count()"),
                ExerciseOption("2", "size()"),
                ExerciseOption("3", "len()"),
                ExerciseOption("4", "length()")
            ),
            correctIndex = 2,
            explanation = "`len(collection)` is Python's standard built-in to retrieve sequence length."
        )
    )

    fun determineRecommendedTrack(correctAnswersCount: Int): LearningTrack {
        return when (correctAnswersCount) {
            0, 1 -> LearningTrack.ZERO_ENGLISH
            2 -> LearningTrack.MATH_ZERO
            3 -> LearningTrack.COMPUTER_LITERACY
            4 -> LearningTrack.PROGRAMMING_LOGIC
            else -> LearningTrack.PYTHON_MASTERY
        }
    }
}
