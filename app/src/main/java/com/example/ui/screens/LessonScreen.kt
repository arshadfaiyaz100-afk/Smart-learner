package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.Lesson
import com.example.learning.AdaptiveMasteryEngine
import com.example.speech.SpeechRecognitionProvider
import com.example.speech.TTSProvider
import com.example.ui.components.AudioPlayBar
import com.example.ui.components.SpeechPracticeCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Ruby600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun LessonScreen(
    lesson: Lesson,
    hearts: Int,
    supportLanguage: com.example.data.model.SupportLanguage = com.example.data.model.SupportLanguage.ENGLISH_DIRECT,
    ttsProvider: TTSProvider,
    speechProvider: SpeechRecognitionProvider,
    onDeductHeart: () -> Unit,
    onRecordMistake: (concept: String, question: String, userAnswer: String, correctAnswer: String, explanation: String) -> Unit,
    onCompleteLesson: (stars: Int, mastery: Int, xpEarned: Int) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onExit)

    // Stage: -1 = Teach Phase, 0..N-1 = Exercise Phase, N = Completion Screen
    var currentStage by remember { mutableIntStateOf(-1) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var userTextInput by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }
    var hintsUsedTotal by remember { mutableIntStateOf(0) }
    var mistakesCount by remember { mutableIntStateOf(0) }
    var correctFirstTryCount by remember { mutableIntStateOf(0) }

    // Feedback bottom sheet states
    var isAnswerChecked by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }

    // Speech states
    var isListening by remember { mutableStateOf(false) }
    var recognizedSpeech by remember { mutableStateOf<String?>(null) }
    var speechConfidence by remember { mutableStateOf<Float?>(null) }

    val totalExercises = lesson.exercises.size
    val progressFraction = if (totalExercises > 0 && currentStage >= 0) {
        (currentStage.toFloat() / totalExercises.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("lesson_screen")
    ) {
        // Top Navigation & Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onExit, modifier = Modifier.testTag("exit_lesson_btn")) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit")
            }

            if (currentStage >= 0 && currentStage < totalExercises) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Emerald500,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Hearts display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Hearts",
                    tint = Ruby600,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = " $hearts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ruby600
                )
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                // 1. TEACH STAGE
                currentStage == -1 -> {
                    TeachPhaseContent(
                        lesson = lesson,
                        supportLanguage = supportLanguage,
                        ttsProvider = ttsProvider,
                        onStartPractice = { currentStage = 0 }
                    )
                }

                // 2. EXERCISE STAGE
                currentStage in 0 until totalExercises -> {
                    val exercise = lesson.exercises[currentStage]
                    ExercisePhaseContent(
                        exercise = exercise,
                        supportLanguage = supportLanguage,
                        selectedOptionIndex = selectedOptionIndex,
                        userTextInput = userTextInput,
                        showHint = showHint,
                        isListening = isListening,
                        recognizedSpeech = recognizedSpeech,
                        speechConfidence = speechConfidence,
                        ttsProvider = ttsProvider,
                        onOptionSelected = { selectedOptionIndex = it },
                        onTextChanged = { userTextInput = it },
                        onToggleHint = {
                            showHint = !showHint
                            if (showHint) hintsUsedTotal++
                        },
                        onStartListening = {
                            isListening = true
                            speechProvider.startListening(
                                onResult = { text, conf ->
                                    isListening = false
                                    recognizedSpeech = text
                                    speechConfidence = conf
                                    // Verify speech
                                    val target = exercise.targetPhrase ?: ""
                                    if (text.contains(target, ignoreCase = true)) {
                                        selectedOptionIndex = 0 // Marks correct
                                    }
                                },
                                onError = {
                                    isListening = false
                                    recognizedSpeech = "Mic: $it"
                                }
                            )
                        },
                        onStopListening = {
                            isListening = false
                            speechProvider.stopListening()
                        }
                    )
                }

                // 3. COMPLETION STAGE
                else -> {
                    val stars = AdaptiveMasteryEngine.calculateStars(totalExercises, mistakesCount, hintsUsedTotal)
                    val mastery = AdaptiveMasteryEngine.calculateMasteryPercent(totalExercises, correctFirstTryCount)
                    LessonCompleteContent(
                        lesson = lesson,
                        stars = stars,
                        mastery = mastery,
                        xpEarned = lesson.xpReward,
                        onFinish = { onCompleteLesson(stars, mastery, lesson.xpReward) }
                    )
                }
            }
        }

        // Bottom Action Bar during exercises
        if (currentStage in 0 until totalExercises) {
            val exercise = lesson.exercises[currentStage]
            LessonBottomBar(
                isAnswerChecked = isAnswerChecked,
                isAnswerCorrect = isAnswerCorrect,
                explanation = exercise.getLocalizedExplanation(supportLanguage),
                onCheckAnswer = {
                    val isCorrect = when (exercise.type) {
                        ExerciseType.MCQ, ExerciseType.TRUE_FALSE, ExerciseType.CODE_PREDICTION, ExerciseType.DEBUGGING, ExerciseType.CODE_COMPLETION, ExerciseType.MATCHING -> {
                            selectedOptionIndex == exercise.correctIndex
                        }
                        ExerciseType.FILL_BLANKS -> {
                            userTextInput.trim().equals(exercise.correctText?.trim(), ignoreCase = true)
                        }
                        ExerciseType.SPEAKING -> {
                            val target = exercise.targetPhrase ?: ""
                            recognizedSpeech?.contains(target, ignoreCase = true) == true
                        }
                        else -> selectedOptionIndex == exercise.correctIndex
                    }

                    isAnswerChecked = true
                    isAnswerCorrect = isCorrect

                    if (isCorrect) {
                        if (mistakesCount == 0 && !showHint) {
                            correctFirstTryCount++
                        }
                    } else {
                        mistakesCount++
                        onDeductHeart()
                        onRecordMistake(
                            lesson.title,
                            exercise.prompt,
                            userTextInput.ifBlank { selectedOptionIndex?.toString() ?: "None" },
                            exercise.correctText ?: exercise.options.getOrNull(exercise.correctIndex ?: 0)?.text ?: "",
                            exercise.getLocalizedExplanation(supportLanguage)
                        )
                    }
                },
                onNextExercise = {
                    isAnswerChecked = false
                    isAnswerCorrect = false
                    selectedOptionIndex = null
                    userTextInput = ""
                    showHint = false
                    recognizedSpeech = null
                    speechConfidence = null
                    currentStage++
                }
            )
        }
    }
}

@Composable
private fun TeachPhaseContent(
    lesson: Lesson,
    supportLanguage: com.example.data.model.SupportLanguage,
    ttsProvider: TTSProvider,
    onStartPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                text = "LESSON OBJECTIVE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = lesson.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = lesson.objective,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Teach card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Core Principles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lesson.getLocalizedTeachContent(supportLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                // Code or phonetic example if present
                if (!lesson.codeExample.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate950)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = lesson.codeExample,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan500
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Listen Bar
        AudioPlayBar(
            textToSpeak = lesson.title,
            onSpeakNormal = { ttsProvider.speak(lesson.title, slow = false) },
            onSpeakSlow = { ttsProvider.speak(lesson.title, slow = true) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartPractice,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_practice_btn")
        ) {
            Text(
                "Start Guided Practice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ExercisePhaseContent(
    exercise: Exercise,
    supportLanguage: com.example.data.model.SupportLanguage,
    selectedOptionIndex: Int?,
    userTextInput: String,
    showHint: Boolean,
    isListening: Boolean,
    recognizedSpeech: String?,
    speechConfidence: Float?,
    ttsProvider: TTSProvider,
    onOptionSelected: (Int) -> Unit,
    onTextChanged: (String) -> Unit,
    onToggleHint: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Exercise Type tag & Hint trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = exercise.type.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onToggleHint) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = Amber500
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Code snippet if present
        if (!exercise.codeSnippet.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
                    .padding(14.dp)
            ) {
                Text(
                    text = exercise.codeSnippet,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        // Hint box if toggled
        if (showHint) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Amber500.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = Amber500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = exercise.getLocalizedHint(supportLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options or Input by Type
        when (exercise.type) {
            ExerciseType.MCQ, ExerciseType.TRUE_FALSE, ExerciseType.CODE_PREDICTION, ExerciseType.DEBUGGING, ExerciseType.CODE_COMPLETION, ExerciseType.MATCHING -> {
                exercise.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOptionIndex == idx
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onOptionSelected(idx) }
                            .testTag("exercise_option_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = opt.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            ExerciseType.FILL_BLANKS -> {
                OutlinedTextField(
                    value = userTextInput,
                    onValueChange = onTextChanged,
                    label = { Text("Your Answer") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fill_blank_input")
                )
            }

            ExerciseType.SPEAKING -> {
                exercise.targetPhrase?.let { phrase ->
                    AudioPlayBar(
                        textToSpeak = phrase,
                        onSpeakNormal = { ttsProvider.speak(phrase, slow = false) },
                        onSpeakSlow = { ttsProvider.speak(phrase, slow = true) }
                    )

                    SpeechPracticeCard(
                        targetPhrase = phrase,
                        isListening = isListening,
                        recognizedText = recognizedSpeech,
                        confidenceScore = speechConfidence,
                        onStartListening = onStartListening,
                        onStopListening = onStopListening
                    )
                }
            }

            else -> {
                OutlinedTextField(
                    value = userTextInput,
                    onValueChange = onTextChanged,
                    label = { Text("Enter your solution") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LessonBottomBar(
    isAnswerChecked: Boolean,
    isAnswerCorrect: Boolean,
    explanation: String,
    onCheckAnswer: () -> Unit,
    onNextExercise: () -> Unit
) {
    Surface(
        color = if (isAnswerChecked) {
            if (isAnswerCorrect) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isAnswerChecked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isAnswerCorrect) Emerald500 else Ruby600,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAnswerCorrect) "Excellent!" else "Incorrect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswerCorrect) Emerald500 else Ruby600
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNextExercise,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswerCorrect) Emerald500 else Ruby600
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_exercise_btn")
                ) {
                    Text(
                        "Continue",
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswerCorrect) Color.Black else Color.White
                    )
                }
            } else {
                Button(
                    onClick = onCheckAnswer,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("check_answer_btn")
                ) {
                    Text(
                        "Check",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonCompleteContent(
    lesson: Lesson,
    stars: Int,
    mastery: Int,
    xpEarned: Int,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration stars
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { idx ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (idx < stars) Amber500 else Slate800,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Lesson Mastered!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Emerald500
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = lesson.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Stats summary card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "+$xpEarned", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Amber500)
                    Text(text = "XP Earned", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$mastery%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Emerald500)
                    Text(text = "Mastery Score", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$stars / 3", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Indigo500)
                    Text(text = "Stars", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("finish_lesson_btn")
        ) {
            Text(
                "Claim Rewards & Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
