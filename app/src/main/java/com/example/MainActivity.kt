package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AIProvider
import com.example.ai.GeminiProvider
import com.example.ai.LocalAIProvider
import com.example.curriculum.CurriculumRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.LearningRepository
import com.example.data.model.CapabilityEntity
import com.example.data.model.CapabilityStatus
import com.example.data.model.LearningTrack
import com.example.data.model.Lesson
import com.example.data.model.SupportLanguage
import com.example.data.model.UnlockCelebrationEvent
import com.example.data.model.UserProfileEntity
import com.example.learning.CapabilityEngine
import com.example.speech.AndroidSpeechProvider
import com.example.speech.AndroidTTSProvider
import com.example.ui.components.CapabilityStatusDialog
import com.example.ui.components.HeaderStatsBar
import com.example.ui.components.UnlockCelebrationDialog
import com.example.ui.screens.AITutorScreen
import com.example.ui.screens.ApiLabScreen
import com.example.ui.screens.CapabilitiesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LessonScreen
import com.example.ui.screens.PlacementTestScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProjectsLabScreen
import com.example.ui.screens.PythonLabScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeakingLabScreen
import com.example.ui.theme.Emerald500
import com.example.ui.theme.LearningLabTheme
import kotlinx.coroutines.launch

enum class MainDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LEARN("Learn", Icons.Default.School),
    PRACTICE("Practice", Icons.Default.Refresh),
    TOOLS("Tools", Icons.Default.Terminal),
    PROFILE("Profile", Icons.Default.Person)
}

enum class ToolScreenDestination {
    SPEAKING_LAB,
    PYTHON_LAB,
    PROJECTS_LAB,
    API_LAB,
    AI_TUTOR,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var ttsProvider: AndroidTTSProvider
    private lateinit var speechProvider: AndroidSpeechProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsProvider = AndroidTTSProvider(this)
        speechProvider = AndroidSpeechProvider(this)

        val database = AppDatabase.getDatabase(this)
        val repository = LearningRepository(database.learningDao())

        // Preferences for optional Gemini key
        val prefs = getSharedPreferences("learning_lab_prefs", Context.MODE_PRIVATE)

        setContent {
            LearningLabTheme {
                LearningLabApp(
                    repository = repository,
                    ttsProvider = ttsProvider,
                    speechProvider = speechProvider,
                    getApiKey = { prefs.getString("gemini_api_key", "") ?: "" },
                    saveApiKey = { key -> prefs.edit().putString("gemini_api_key", key).apply() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsProvider.shutdown()
        speechProvider.destroy()
    }
}

@Composable
fun LearningLabApp(
    repository: LearningRepository,
    ttsProvider: AndroidTTSProvider,
    speechProvider: AndroidSpeechProvider,
    getApiKey: () -> String,
    saveApiKey: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize database on start
    LaunchedEffect(Unit) {
        repository.initializeIfFirstRun()
    }

    val userProfile by repository.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val allProgress by repository.allProgress.collectAsStateWithLifecycle(initialValue = emptyList())
    val capabilities by repository.allCapabilities.collectAsStateWithLifecycle(initialValue = emptyList())
    val dueReviews by repository.dueReviews.collectAsStateWithLifecycle(initialValue = emptyList())
    val unresolvedMistakes by repository.unresolvedMistakes.collectAsStateWithLifecycle(initialValue = emptyList())
    val achievements by repository.allAchievements.collectAsStateWithLifecycle(initialValue = emptyList())
    val notes by repository.notes.collectAsStateWithLifecycle(initialValue = emptyList())
    val apiHistory by repository.apiHistory.collectAsStateWithLifecycle(initialValue = emptyList())

    val progressMap = remember(allProgress) {
        allProgress.associateBy { it.lessonId }
    }

    var mainDestination by remember { mutableStateOf(MainDestination.LEARN) }
    var activeToolScreen by remember { mutableStateOf<ToolScreenDestination?>(null) }
    var activeLesson by remember { mutableStateOf<Lesson?>(null) }
    var activeTreeTrack by remember { mutableStateOf<LearningTrack?>(null) }
    var isTakingPlacementTest by remember { mutableStateOf(false) }

    // Dialog state
    var selectedCapabilityForDialog by remember { mutableStateOf<CapabilityEntity?>(null) }
    var celebrationEvent by remember { mutableStateOf<UnlockCelebrationEvent?>(null) }

    var currentApiKey by remember { mutableStateOf(getApiKey()) }

    // Dynamic AI Provider (Gemini if key provided, otherwise local offline engine)
    val aiProvider: AIProvider = remember(currentApiKey) {
        if (currentApiKey.isNotBlank()) {
            GeminiProvider(apiKeyProvider = { currentApiKey })
        } else {
            LocalAIProvider()
        }
    }

    val supportLang = remember(userProfile?.supportLanguage) {
        SupportLanguage.values().find { it.name == userProfile?.supportLanguage } ?: SupportLanguage.ENGLISH_DIRECT
    }

    // 1. Handle Diagnostic Placement Test view
    if (isTakingPlacementTest) {
        BackHandler { isTakingPlacementTest = false }
        PlacementTestScreen(
            onPlacementCompleted = { recommendedTrack ->
                scope.launch {
                    repository.unlockPlacementLevel(recommendedTrack.name)
                    isTakingPlacementTest = false
                    Toast.makeText(context, "Diagnostic test complete! Placed into: ${recommendedTrack.title}", Toast.LENGTH_LONG).show()
                }
            },
            onCancel = { isTakingPlacementTest = false }
        )
        return
    }

    // 2. Handle Active Lesson view
    if (activeLesson != null) {
        BackHandler { activeLesson = null }
        LessonScreen(
            lesson = activeLesson!!,
            hearts = userProfile?.hearts ?: 5,
            supportLanguage = supportLang,
            ttsProvider = ttsProvider,
            speechProvider = speechProvider,
            onDeductHeart = {
                scope.launch { repository.deductHeart() }
            },
            onRecordMistake = { concept, question, userAns, correctAns, explanation ->
                scope.launch {
                    repository.recordMistake(
                        lessonId = activeLesson!!.id,
                        concept = concept,
                        question = question,
                        userAnswer = userAns,
                        correctAnswer = correctAns,
                        mistakeType = "PracticeError",
                        explanation = explanation
                    )
                }
            },
            onCompleteLesson = { stars, mastery, xp ->
                scope.launch {
                    val events = repository.recordLessonCompletion(
                        lessonId = activeLesson!!.id,
                        trackName = activeLesson!!.track.name,
                        stars = stars,
                        mastery = mastery,
                        xpEarned = xp
                    )
                    activeLesson = null
                    if (events.isNotEmpty()) {
                        celebrationEvent = events.first()
                    }
                }
            },
            onExit = {
                activeLesson = null
            }
        )
        return
    }

    // 3. Handle Interactive Skill Tree view
    if (activeTreeTrack != null) {
        BackHandler { activeTreeTrack = null }
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activeTreeTrack = null },
                        modifier = Modifier.testTag("exit_tree_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Overview"
                        )
                    }
                    Column {
                        Text(
                            text = activeTreeTrack!!.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Interactive Skill Tree",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) { innerPadding ->
            LearnScreen(
                currentTrack = activeTreeTrack!!,
                onTrackSelected = { activeTreeTrack = it },
                progressMap = progressMap,
                onSelectLesson = { lesson ->
                    val prog = progressMap[lesson.id]
                    if (prog?.isUnlocked == true) {
                        activeLesson = lesson
                    } else {
                        Toast.makeText(context, "Complete previous lessons to unlock this step.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // 4. Main App Scaffold with Tab Bar
    Scaffold(
        topBar = {
            HeaderStatsBar(
                profile = userProfile,
                onRefillHearts = {
                    scope.launch {
                        repository.refillHearts()
                        Toast.makeText(context, "Hearts fully restored!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                MainDestination.values().forEach { dest ->
                    val isSelected = activeToolScreen == null && mainDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            activeToolScreen = null
                            mainDestination = dest
                        },
                        icon = { Icon(imageVector = dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = Emerald500
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Secondary Fullscreen Tool Views with Back Navigation
                activeToolScreen != null -> {
                    BackHandler { activeToolScreen = null }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { activeToolScreen = null },
                                modifier = Modifier.testTag("tool_screen_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = when (activeToolScreen) {
                                    ToolScreenDestination.SPEAKING_LAB -> "Speaking Lab"
                                    ToolScreenDestination.PYTHON_LAB -> "Python Lab"
                                    ToolScreenDestination.PROJECTS_LAB -> "Industry Projects"
                                    ToolScreenDestination.API_LAB -> "Safe API Lab"
                                    ToolScreenDestination.AI_TUTOR -> "AI Learning Tutor"
                                    ToolScreenDestination.SETTINGS -> "Settings"
                                    null -> ""
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        when (activeToolScreen) {
                            ToolScreenDestination.SPEAKING_LAB -> {
                                SpeakingLabScreen(
                                    ttsProvider = ttsProvider,
                                    speechProvider = speechProvider
                                )
                            }

                            ToolScreenDestination.PYTHON_LAB -> {
                                PythonLabScreen(
                                    aiProvider = aiProvider,
                                    onSaveSnippet = { title, code, output ->
                                        scope.launch {
                                            repository.saveCode(title, code, output)
                                        }
                                    }
                                )
                            }

                            ToolScreenDestination.PROJECTS_LAB -> {
                                ProjectsLabScreen(
                                    onClaimProjectXp = { xp ->
                                        scope.launch {
                                            userProfile?.let {
                                                repository.recordLessonCompletion(
                                                    lessonId = "project_${System.currentTimeMillis()}",
                                                    trackName = LearningTrack.PYTHON_MASTERY.name,
                                                    stars = 3,
                                                    mastery = 100,
                                                    xpEarned = xp
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            ToolScreenDestination.API_LAB -> {
                                ApiLabScreen(
                                    historyList = apiHistory,
                                    onSaveHistory = { name, method, url, headers, body, code, respBody, latency ->
                                        scope.launch {
                                            repository.saveApiHistory(name, method, url, headers, body, code, respBody, latency)
                                        }
                                    }
                                )
                            }

                            ToolScreenDestination.AI_TUTOR -> {
                                AITutorScreen(
                                    aiProvider = aiProvider,
                                    profile = userProfile
                                )
                            }

                            ToolScreenDestination.SETTINGS -> {
                                SettingsScreen(
                                    currentApiKey = currentApiKey,
                                    onSaveApiKey = { newKey ->
                                        currentApiKey = newKey
                                        saveApiKey(newKey)
                                    },
                                    onExportData = {
                                        val summary = "Learning Lab Progress: Level ${userProfile?.level ?: 1}, XP: ${userProfile?.currentXp ?: 0}, Streak: ${userProfile?.streakCount ?: 1}"
                                        Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                                    },
                                    onDeleteAllData = {
                                        scope.launch {
                                            repository.clearAllUserData()
                                            Toast.makeText(context, "All user progress reset to Absolute Zero.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            null -> {}
                        }
                    }
                }

                // Primary Tabs
                else -> {
                    when (mainDestination) {
                        MainDestination.LEARN -> {
                            HomeScreen(
                                profile = userProfile,
                                capabilities = capabilities,
                                progressMap = progressMap,
                                dueReviewsCount = dueReviews.size,
                                unresolvedMistakesCount = unresolvedMistakes.size,
                                onNavigateToLesson = { lesson ->
                                    activeLesson = lesson
                                },
                                onCapabilityClick = { cap ->
                                    val isUnlocked = CapabilityEngine.isCapabilityUnlocked(capabilities, cap.id)
                                    if (isUnlocked) {
                                        routeToCapability(
                                            capabilityId = cap.id,
                                            onToolRoute = { activeToolScreen = it },
                                            onTrackRoute = { activeTreeTrack = it }
                                        )
                                    } else {
                                        selectedCapabilityForDialog = cap
                                    }
                                },
                                onLanguageChange = { newLang ->
                                    scope.launch {
                                        repository.setSupportLanguage(newLang.name)
                                    }
                                },
                                onNavigateToReview = { mainDestination = MainDestination.PRACTICE },
                                onTrackClick = { track ->
                                    activeTreeTrack = track
                                },
                                onNavigateToPlacementTest = { isTakingPlacementTest = true }
                            )
                        }

                        MainDestination.PRACTICE -> {
                            ReviewScreen(
                                dueReviews = dueReviews,
                                unresolvedMistakes = unresolvedMistakes,
                                onUpdateSpacedItem = { updatedItem ->
                                    scope.launch {
                                        repository.updateSpacedItem(updatedItem)
                                    }
                                },
                                onResolveMistake = { mistakeId ->
                                    scope.launch {
                                        repository.resolveMistake(mistakeId)
                                    }
                                }
                            )
                        }

                        MainDestination.TOOLS -> {
                            CapabilitiesScreen(
                                capabilities = capabilities,
                                onSelectCapability = { cap ->
                                    val isUnlocked = CapabilityEngine.isCapabilityUnlocked(capabilities, cap.id)
                                    if (isUnlocked) {
                                        routeToCapability(
                                            capabilityId = cap.id,
                                            onToolRoute = { activeToolScreen = it },
                                            onTrackRoute = { activeTreeTrack = it }
                                        )
                                    } else {
                                        selectedCapabilityForDialog = cap
                                    }
                                }
                            )
                        }

                        MainDestination.PROFILE -> {
                            ProfileScreen(
                                profile = userProfile,
                                achievements = achievements,
                                notes = notes,
                                onSaveNote = { title, content, tag ->
                                    scope.launch {
                                        repository.saveNote(title, content, tag, null)
                                    }
                                },
                                onDeleteNote = { id ->
                                    scope.launch {
                                        repository.deleteNote(id)
                                    }
                                },
                                onNavigateToSettings = { activeToolScreen = ToolScreenDestination.SETTINGS },
                                onNavigateToPlacementTest = { isTakingPlacementTest = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Capability Status Dialog (When user taps locked capability)
    selectedCapabilityForDialog?.let { cap ->
        CapabilityStatusDialog(
            capability = cap,
            onDismiss = { selectedCapabilityForDialog = null },
            onOpenIfUnlocked = {
                selectedCapabilityForDialog = null
                routeToCapability(
                    capabilityId = cap.id,
                    onToolRoute = { activeToolScreen = it },
                    onTrackRoute = { activeTreeTrack = it }
                )
            }
        )
    }

    // Unlock Celebration Dialog
    celebrationEvent?.let { event ->
        UnlockCelebrationDialog(
            event = event,
            onDismiss = { celebrationEvent = null },
            onAction = { ev ->
                celebrationEvent = null
                routeToCapability(
                    capabilityId = ev.capabilityId,
                    onToolRoute = { activeToolScreen = it },
                    onTrackRoute = { activeTreeTrack = it }
                )
            }
        )
    }
}

private fun routeToCapability(
    capabilityId: String,
    onToolRoute: (ToolScreenDestination) -> Unit,
    onTrackRoute: (LearningTrack) -> Unit = {}
) {
    when (capabilityId) {
        "cap_tool_speaking" -> onToolRoute(ToolScreenDestination.SPEAKING_LAB)
        "cap_tool_code_editor", "cap_tool_python_runner", "cap_tool_test_cases", "cap_tool_debugger" ->
            onToolRoute(ToolScreenDestination.PYTHON_LAB)
        "cap_tool_projects" -> onToolRoute(ToolScreenDestination.PROJECTS_LAB)
        "cap_tool_api_lab" -> onToolRoute(ToolScreenDestination.API_LAB)
        "cap_tool_ai_tutor" -> onToolRoute(ToolScreenDestination.AI_TUTOR)
        "cap_subject_english" -> onTrackRoute(LearningTrack.ZERO_ENGLISH)
        "cap_subject_math" -> onTrackRoute(LearningTrack.MATH_ZERO)
        "cap_subject_computer" -> onTrackRoute(LearningTrack.COMPUTER_LITERACY)
        "cap_subject_logic" -> onTrackRoute(LearningTrack.PROGRAMMING_LOGIC)
        "cap_subject_python" -> onTrackRoute(LearningTrack.PYTHON_MASTERY)
        "cap_subject_software" -> onTrackRoute(LearningTrack.SOFTWARE_ENGINEERING)
        "cap_subject_ai" -> onTrackRoute(LearningTrack.AI_ENGINEERING)
    }
}
