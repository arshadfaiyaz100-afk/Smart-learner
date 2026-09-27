package com.example.learning

import com.example.data.model.CapabilityEntity
import com.example.data.model.CapabilityStatus
import com.example.data.model.LearningTrack
import com.example.data.model.LessonProgressEntity
import com.example.data.model.UnlockCelebrationEvent

object CapabilityEngine {

    val defaultCapabilities: List<CapabilityEntity> = listOf(
        // 1. Core Subjects
        CapabilityEntity(
            id = "cap_subject_english",
            name = "English from Zero",
            iconName = "School",
            description = "Letter recognition, phonics, vocabulary and everyday sentences",
            category = "SUBJECT",
            requiredTrack = LearningTrack.ZERO_ENGLISH.name,
            requiredLessonsCount = 0,
            requiredMasteryPercent = 0,
            status = CapabilityStatus.AVAILABLE.name,
            unlockedAt = System.currentTimeMillis(),
            unlockMessage = "Welcome! Your journey begins with letter recognition."
        ),
        CapabilityEntity(
            id = "cap_tool_code_viewer",
            name = "Code & Text Viewer",
            iconName = "Visibility",
            description = "Read formatted instruction text and sample syntax",
            category = "TOOL",
            requiredTrack = LearningTrack.ZERO_ENGLISH.name,
            requiredLessonsCount = 0,
            requiredMasteryPercent = 0,
            status = CapabilityStatus.AVAILABLE.name,
            unlockedAt = System.currentTimeMillis(),
            unlockMessage = "You can now read formatted lesson text and examples."
        ),
        CapabilityEntity(
            id = "cap_tool_speaking",
            name = "Speaking Lab",
            iconName = "RecordVoiceOver",
            description = "Interactive pronunciation practice with native audio and speech evaluation",
            category = "TOOL",
            requiredTrack = LearningTrack.ZERO_ENGLISH.name,
            requiredLessonsCount = 2,
            requiredMasteryPercent = 60,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Speaking Lab Unlocked! Practice pronunciation with live microphone feedback."
        ),
        CapabilityEntity(
            id = "cap_subject_math",
            name = "Mathematics from Zero",
            iconName = "Calculate",
            description = "Numbers, counting, comparisons, addition, subtraction and numeric logic",
            category = "SUBJECT",
            requiredTrack = LearningTrack.ZERO_ENGLISH.name,
            requiredLessonsCount = 3,
            requiredMasteryPercent = 70,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Mathematics Unlocked! You have built the basic English vocabulary for numbers."
        ),
        CapabilityEntity(
            id = "cap_subject_computer",
            name = "Computer Fundamentals",
            iconName = "Computer",
            description = "Screens, keyboards, touch inputs, files, folders, extensions and web concepts",
            category = "SUBJECT",
            requiredTrack = LearningTrack.MATH_ZERO.name,
            requiredLessonsCount = 2,
            requiredMasteryPercent = 70,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Computer Fundamentals Unlocked! Learn how hardware, operating systems, and files work."
        ),
        CapabilityEntity(
            id = "cap_subject_logic",
            name = "Programming Logic",
            iconName = "Psychology",
            description = "Recipes, sequences, conditions (if/else), repetition loops and memory containers",
            category = "SUBJECT",
            requiredTrack = LearningTrack.COMPUTER_LITERACY.name,
            requiredLessonsCount = 2,
            requiredMasteryPercent = 70,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Programming Logic Unlocked! Understand computational thinking before writing code."
        ),
        CapabilityEntity(
            id = "cap_tool_ai_tutor",
            name = "AI Learning Tutor",
            iconName = "AutoAwesome",
            description = "Adaptive contextual tutor for concept explanations and hints",
            category = "TOOL",
            requiredTrack = LearningTrack.PROGRAMMING_LOGIC.name,
            requiredLessonsCount = 1,
            requiredMasteryPercent = 70,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 AI Tutor Unlocked! Ask questions and receive tailored, beginner-friendly explanations."
        ),
        CapabilityEntity(
            id = "cap_subject_python",
            name = "Python Foundation",
            iconName = "Terminal",
            description = "Write instructions for computers using clean, readable Python code",
            category = "SUBJECT",
            requiredTrack = LearningTrack.PROGRAMMING_LOGIC.name,
            requiredLessonsCount = 3,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🐍 PYTHON UNLOCKED! You are ready to write and understand your first Python programs."
        ),
        CapabilityEntity(
            id = "cap_tool_code_editor",
            name = "Python Code Editor",
            iconName = "EditNote",
            description = "Syntax-highlighted code editor with line numbers and quick-tokens",
            category = "TOOL",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 1,
            requiredMasteryPercent = 70,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Python Code Editor Unlocked! Write and format code with IDE guidance."
        ),
        CapabilityEntity(
            id = "cap_tool_python_runner",
            name = "Python Sandboxed Runner",
            iconName = "PlayArrow",
            description = "Isolated safe code execution engine capturing standard output in milliseconds",
            category = "TOOL",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 2,
            requiredMasteryPercent = 75,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Python Runner Unlocked! Run and test programs right on your device."
        ),
        CapabilityEntity(
            id = "cap_tool_test_cases",
            name = "Automated Test Suite",
            iconName = "CheckCircle",
            description = "Verify code correctness against automated input/expected-output test cases",
            category = "TOOL",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 3,
            requiredMasteryPercent = 75,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Automated Test Engine Unlocked! Validate algorithm correctness."
        ),
        CapabilityEntity(
            id = "cap_tool_debugger",
            name = "Logic Debugger & Stepper",
            iconName = "BugReport",
            description = "Step through variable assignments, inspect execution flow and error causes",
            category = "TOOL",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 4,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Debugger Unlocked! Find and resolve errors with step-by-step guidance."
        ),
        CapabilityEntity(
            id = "cap_subject_software",
            name = "Software Engineering",
            iconName = "Architecture",
            description = "Git version control, HTTP REST APIs, status codes, testing and clean design",
            category = "SUBJECT",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 5,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🚀 Software Engineering Unlocked! Move from scripts to production applications."
        ),
        CapabilityEntity(
            id = "cap_tool_api_lab",
            name = "Safe API Lab",
            iconName = "Http",
            description = "Authorized HTTP request tester supporting GET, POST, PUT, DELETE with latency metrics",
            category = "TOOL",
            requiredTrack = LearningTrack.SOFTWARE_ENGINEERING.name,
            requiredLessonsCount = 1,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Safe API Lab Unlocked! Send HTTP requests and inspect JSON responses."
        ),
        CapabilityEntity(
            id = "cap_tool_projects",
            name = "Industry Projects Lab",
            iconName = "Code",
            description = "Step-by-step real portfolio projects with architectures, test suites and XP rewards",
            category = "WORKSPACE",
            requiredTrack = LearningTrack.PYTHON_MASTERY.name,
            requiredLessonsCount = 4,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🎉 Industry Projects Lab Unlocked! Build calculators, quiz engines, and automation tools."
        ),
        CapabilityEntity(
            id = "cap_subject_ai",
            name = "AI & Prompt Engineering",
            iconName = "Memory",
            description = "LLMs, tokens, context windows, embeddings, RAG architectures and AI agents",
            category = "SUBJECT",
            requiredTrack = LearningTrack.SOFTWARE_ENGINEERING.name,
            requiredLessonsCount = 2,
            requiredMasteryPercent = 80,
            status = CapabilityStatus.LOCKED.name,
            unlockMessage = "🤖 AI Engineering Unlocked! Build intelligent retrieval and agentic workflows."
        )
    )

    fun evaluateUnlocks(
        currentCapabilities: List<CapabilityEntity>,
        completedLessons: List<LessonProgressEntity>
    ): Pair<List<CapabilityEntity>, List<UnlockCelebrationEvent>> {
        val completedByTrack = completedLessons
            .filter { it.isCompleted }
            .groupBy { it.trackName }

        val newEvents = mutableListOf<UnlockCelebrationEvent>()
        val updatedCapabilities = currentCapabilities.map { cap ->
            if (cap.status == CapabilityStatus.AVAILABLE.name || cap.status == CapabilityStatus.MASTERED.name) {
                cap
            } else {
                val trackCompleted = completedByTrack[cap.requiredTrack] ?: emptyList()
                val count = trackCompleted.size
                val avgMastery = if (trackCompleted.isNotEmpty()) {
                    trackCompleted.map { it.masteryPercent }.average().toInt()
                } else 0

                val isMet = count >= cap.requiredLessonsCount && avgMastery >= cap.requiredMasteryPercent

                if (isMet) {
                    newEvents.add(
                        UnlockCelebrationEvent(
                            capabilityId = cap.id,
                            title = "NEW CAPABILITY UNLOCKED",
                            description = cap.unlockMessage.ifBlank { "You have unlocked ${cap.name}!" },
                            iconName = cap.iconName,
                            targetDestination = cap.id
                        )
                    )
                    cap.copy(
                        status = CapabilityStatus.AVAILABLE.name,
                        unlockedAt = System.currentTimeMillis()
                    )
                } else {
                    cap
                }
            }
        }

        return Pair(updatedCapabilities, newEvents)
    }

    fun isCapabilityUnlocked(capabilities: List<CapabilityEntity>, capabilityId: String): Boolean {
        val cap = capabilities.find { it.id == capabilityId }
        return cap?.status == CapabilityStatus.AVAILABLE.name || cap?.status == CapabilityStatus.MASTERED.name
    }
}
