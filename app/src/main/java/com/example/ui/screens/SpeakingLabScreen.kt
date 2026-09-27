package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.SpeechRecognitionProvider
import com.example.speech.TTSProvider
import com.example.ui.components.SpeechPracticeCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Ruby600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

data class SpeakingPromptItem(
    val id: String,
    val category: String, // "Vocabulary", "Technical", "Conversation", "Role-Play"
    val phrase: String,
    val phonetics: String,
    val contextDescription: String
)

data class SpeakingAttemptHistory(
    val phrase: String,
    val recognized: String,
    val accuracyPercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun SpeakingLabScreen(
    ttsProvider: TTSProvider,
    speechProvider: SpeechRecognitionProvider,
    modifier: Modifier = Modifier
) {
    val prompts = remember {
        listOf(
            SpeakingPromptItem("sp_1", "Vocabulary", "Algorithm", "/ˈæl.ɡə.rɪ.ðəm/", "A step-by-step sequence of instructions to solve a problem."),
            SpeakingPromptItem("sp_2", "Vocabulary", "Variable", "/ˈver.i.ə.bəl/", "A storage location paired with an associated symbolic name."),
            SpeakingPromptItem("sp_3", "Technical", "Commit the changes to GitHub", "/kəˈmɪt/", "Saving staged files to the local Git repository."),
            SpeakingPromptItem("sp_4", "Technical", "The API returned status code two hundred", "/ˌeɪ.piːˈaɪ/", "Signifying an HTTP successful response."),
            SpeakingPromptItem("sp_5", "Conversation", "Could you review my pull request?", "/rɪˈvjuː/", "Standard engineering team collaboration dialogue."),
            SpeakingPromptItem("sp_6", "Role-Play", "I refactored the database connection handler", "/riːˈfæk.tɚd/", "Communicating architectural changes in a standup.")
        )
    }

    val categories = listOf("All", "Vocabulary", "Technical", "Conversation", "Role-Play")
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPromptIndex by remember { mutableIntStateOf(0) }

    val filteredPrompts = remember(selectedCategory) {
        if (selectedCategory == "All") prompts else prompts.filter { it.category == selectedCategory }
    }

    val currentPrompt = filteredPrompts.getOrNull(selectedPromptIndex) ?: prompts.first()

    // Speech states
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var confidenceScore by remember { mutableStateOf<Float?>(null) }
    val history = remember { mutableStateListOf<SpeakingAttemptHistory>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("speaking_lab_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title & disclaimer banner
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Indigo500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = Indigo500)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Speaking Lab",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Speech recognition & pronunciation estimation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                        selectedPromptIndex = 0
                        recognizedText = null
                        confidenceScore = null
                    },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target Phrase Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = currentPrompt.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentPrompt.phrase,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentPrompt.phonetics,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentPrompt.contextDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio controls
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(
                                onClick = { ttsProvider.speak(currentPrompt.phrase, slow = true) },
                                modifier = Modifier.testTag("tts_slow_speaking_lab")
                            ) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = "Slow")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Slow")
                            }

                            Button(
                                onClick = { ttsProvider.speak(currentPrompt.phrase, slow = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                                modifier = Modifier.testTag("tts_normal_speaking_lab")
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Normal", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Listen", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Interactive Recording Area
            item {
                SpeechPracticeCard(
                    targetPhrase = currentPrompt.phrase,
                    isListening = isListening,
                    recognizedText = recognizedText,
                    confidenceScore = confidenceScore,
                    onStartListening = {
                        isListening = true
                        speechProvider.startListening(
                            onResult = { text, conf ->
                                isListening = false
                                recognizedText = text
                                confidenceScore = conf

                                // Calculate estimate
                                val targetWords = currentPrompt.phrase.lowercase().split(" ")
                                val recognizedWords = text.lowercase().split(" ")
                                val matched = targetWords.count { recognizedWords.contains(it) }
                                val accuracy = if (targetWords.isNotEmpty()) (matched.toFloat() / targetWords.size * 100).toInt() else 0

                                history.add(0, SpeakingAttemptHistory(currentPrompt.phrase, text, accuracy))
                            },
                            onError = {
                                isListening = false
                                recognizedText = "Notice: $it"
                            }
                        )
                    },
                    onStopListening = {
                        isListening = false
                        speechProvider.stopListening()
                    }
                )
            }

            // Word-by-word playback chip list
            item {
                Text(
                    text = "Word-by-Word Articulation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentPrompt.phrase.split(" ").forEach { word ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                ttsProvider.speak(word, slow = true)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = word, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // History of attempts
            if (history.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Speaking History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(history.take(5)) { attempt ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = attempt.phrase,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Heard: \"${attempt.recognized}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (attempt.accuracyPercent >= 70) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${attempt.accuracyPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attempt.accuracyPercent >= 70) Emerald500 else Amber500,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
