package com.example.data.model

enum class LearningTrack(val title: String, val subtitle: String, val tag: String, val orderIndex: Int) {
    ZERO_ENGLISH(
        "English from Zero",
        "Letter shapes, phonics, everyday vocabulary & simple sentences",
        "Foundation 1",
        1
    ),
    MATH_ZERO(
        "Mathematics from Zero",
        "Numbers, counting objects, comparison, addition & subtraction",
        "Foundation 2",
        2
    ),
    COMPUTER_LITERACY(
        "Computer Fundamentals",
        "Screen, mouse/touch, files, folders, extensions & internet",
        "Foundation 3",
        3
    ),
    PROGRAMMING_LOGIC(
        "Programming Logic",
        "Instructions, sequence, conditions, loops & memory boxes",
        "Core Logic",
        4
    ),
    PYTHON_MASTERY(
        "Python Foundation & Core",
        "print(), variables, types, branching, lists, loops & functions",
        "Languages",
        5
    ),
    SOFTWARE_ENGINEERING(
        "Software Engineering & Tools",
        "Git/GitHub, REST APIs, automated testing & terminal commands",
        "Engineering",
        6
    ),
    AI_ENGINEERING(
        "AI & Industry Projects",
        "LLMs, Prompt Engineering, Embeddings, RAG & Real Portfolio Projects",
        "Industry AI",
        7
    )
}

enum class ExerciseType {
    MCQ,
    TRUE_FALSE,
    MATCHING,
    REORDER,
    FILL_BLANKS,
    LISTENING,
    SPEAKING,
    TRANSLATION,
    CODE_COMPLETION,
    CODE_PREDICTION,
    DEBUGGING,
    CODE_WRITING
}

data class ExerciseOption(
    val id: String,
    val text: String,
    val subtext: String? = null
)

data class TestCase(
    val name: String,
    val input: String = "",
    val expectedOutput: String,
    val isHidden: Boolean = false
)

data class Exercise(
    val id: String,
    val lessonId: String,
    val type: ExerciseType,
    val prompt: String,
    val targetPhrase: String? = null,
    val options: List<ExerciseOption> = emptyList(),
    val correctIndex: Int? = null,
    val correctText: String? = null,
    val correctOrder: List<String> = emptyList(),
    val codeSnippet: String? = null,
    val testCases: List<TestCase> = emptyList(),
    val explanation: String,
    val explanationHinglish: String? = null,
    val explanationSimple: String? = null,
    val hint: String,
    val hintHinglish: String? = null,
    val difficulty: Int = 1
) {
    fun getLocalizedExplanation(language: SupportLanguage): String {
        return when (language) {
            SupportLanguage.HINGLISH -> explanationHinglish ?: explanation
            SupportLanguage.SIMPLE_ENGLISH -> explanationSimple ?: explanation
            SupportLanguage.ENGLISH_DIRECT -> explanation
        }
    }

    fun getLocalizedHint(language: SupportLanguage): String {
        return when (language) {
            SupportLanguage.HINGLISH -> hintHinglish ?: hint
            else -> hint
        }
    }
}

data class Lesson(
    val id: String,
    val unitId: String,
    val title: String,
    val track: LearningTrack,
    val objective: String,
    val teachContent: String,
    val teachContentHinglish: String? = null,
    val codeExample: String? = null,
    val xpReward: Int = 20,
    val orderIndex: Int,
    val exercises: List<Exercise>
) {
    fun getLocalizedTeachContent(language: SupportLanguage): String {
        return when (language) {
            SupportLanguage.HINGLISH -> teachContentHinglish ?: teachContent
            else -> teachContent
        }
    }
}

data class CourseUnit(
    val id: String,
    val track: LearningTrack,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val lessons: List<Lesson>
)
