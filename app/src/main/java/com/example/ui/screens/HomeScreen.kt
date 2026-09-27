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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.curriculum.CurriculumRegistry
import com.example.data.model.CapabilityEntity
import com.example.data.model.CapabilityStatus
import com.example.data.model.LearningTrack
import com.example.data.model.Lesson
import com.example.data.model.LessonProgressEntity
import com.example.data.model.SupportLanguage
import com.example.data.model.UserProfileEntity
import com.example.learning.CapabilityEngine
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Ruby600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun HomeScreen(
    profile: UserProfileEntity?,
    capabilities: List<CapabilityEntity>,
    progressMap: Map<String, LessonProgressEntity>,
    dueReviewsCount: Int,
    unresolvedMistakesCount: Int,
    onNavigateToLesson: (Lesson) -> Unit,
    onCapabilityClick: (CapabilityEntity) -> Unit,
    onLanguageChange: (SupportLanguage) -> Unit,
    onNavigateToReview: () -> Unit,
    onTrackClick: (LearningTrack) -> Unit = {},
    onNavigateToPlacementTest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSupportLang = remember(profile?.supportLanguage) {
        SupportLanguage.values().find { it.name == profile?.supportLanguage } ?: SupportLanguage.ENGLISH_DIRECT
    }

    // Determine the current next lesson to learn
    val allLessons = CurriculumRegistry.getAllLessons()
    val currentLesson = allLessons.find { lesson ->
        val prog = progressMap[lesson.id]
        prog?.isUnlocked == true && prog.isCompleted == false
    } ?: allLessons.first()

    val nextLesson = CurriculumRegistry.getNextLesson(currentLesson.id)

    // Calculate overall progression percentage
    val completedCount = progressMap.values.count { it.isCompleted }
    val totalLessons = allLessons.size
    val totalProgressPercent = if (totalLessons > 0) (completedCount.toFloat() / totalLessons * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Support Language selector header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Explanations in: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportLanguage.values().forEach { lang ->
                        FilterChip(
                            selected = currentSupportLang == lang,
                            onClick = { onLanguageChange(lang) },
                            label = { Text(lang.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // ADAPTIVE DAILY TARGET & OVERACHIEVEMENT HUB (No Hard Maximum)
        item {
            val targetActions = profile?.dailyTargetActions ?: 10
            val completedActions = profile?.dailyActionsCompleted ?: 0
            val isDoubleDay = completedActions >= targetActions * 2 && targetActions > 0
            val isTargetMet = completedActions >= targetActions

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isDoubleDay -> Amber500.copy(alpha = 0.12f)
                        isTargetMet -> Emerald500.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_target_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    isDoubleDay -> Icons.Default.LocalFireDepartment
                                    isTargetMet -> Icons.Default.CheckCircle
                                    else -> Icons.Default.ElectricBolt
                                },
                                contentDescription = null,
                                tint = when {
                                    isDoubleDay -> Amber500
                                    isTargetMet -> Emerald500
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isDoubleDay -> "🔥 DOUBLE DAY REACHED!"
                                    isTargetMet -> "🎯 DAILY MINIMUM MET"
                                    else -> "DAILY TARGET (MINIMUM)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    isDoubleDay -> Amber500
                                    isTargetMet -> Emerald500
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Text(
                            text = "$completedActions / $targetActions Actions",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val fraction = if (targetActions > 0) {
                        (completedActions.toFloat() / targetActions.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isDoubleDay -> Amber500
                            isTargetMet -> Emerald500
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            isDoubleDay -> "Incredible momentum! 2x minimum target completed (+100 XP awarded). No daily limit — keep learning!"
                            isTargetMet -> "Minimum goal completed! Continue learning at your own pace — there is NO hard maximum."
                            else -> "${(targetActions - completedActions).coerceAtLeast(0)} more meaningful actions to reach today's minimum. Lessons, reviews, speaking & coding all count!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // MASTER HERO CARD: "THIS IS YOUR NEXT STEP" (User Never Gets Lost)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_step_hero_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "CURRENT STEP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentLesson.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentLesson.objective,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next / Unlock preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEXT: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = nextLesson?.title ?: "Stage Mastery",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Amber500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UNLOCKS: ${currentLesson.track.title} Competence & Tools",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onNavigateToLesson(currentLesson) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_current_lesson_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (progressMap[currentLesson.id]?.isCompleted == true) "Review Step" else "Start This Step",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // PROGRESSIVE CAPABILITIES & TOOLS GATING (Everything Starts Locked)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Capabilities & Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${capabilities.count { CapabilityEngine.isCapabilityUnlocked(capabilities, it.id) }} / ${capabilities.size} Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Capability Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                capabilities.forEach { cap ->
                    val isUnlocked = CapabilityEngine.isCapabilityUnlocked(capabilities, cap.id)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(170.dp)
                            .clickable { onCapabilityClick(cap) }
                            .testTag("cap_card_${cap.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnlocked) Emerald500.copy(alpha = 0.2f) else Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isUnlocked) Emerald500 else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isUnlocked) Emerald500.copy(alpha = 0.15f) else Color.Transparent
                                ) {
                                    Text(
                                        text = if (isUnlocked) "READY" else "LOCKED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Emerald500 else Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = cap.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isUnlocked) cap.description else "Requires ${cap.requiredTrack.replace("_", " ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // ZERO → HERO LEARNING TRACKS OVERVIEW
        item {
            Text(
                text = "Progressive Learning Journey",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Each track unlocks strictly after mastering prerequisites.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(CurriculumRegistry.tracks) { track ->
            val trackLessons = CurriculumRegistry.units.filter { it.track == track }.flatMap { it.lessons }
            val trackCompleted = trackLessons.count { progressMap[it.id]?.isCompleted == true }
            val isTrackUnlocked = trackLessons.any { progressMap[it.id]?.isUnlocked == true }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isTrackUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isTrackUnlocked) 2.dp else 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isTrackUnlocked) { onTrackClick(track) }
                    .testTag("track_row_${track.name}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (trackCompleted == trackLessons.size && trackLessons.isNotEmpty()) Emerald500
                                else if (isTrackUnlocked) Indigo500.copy(alpha = 0.2f)
                                else Slate800
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (trackCompleted == trackLessons.size && trackLessons.isNotEmpty()) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        } else if (isTrackUnlocked) {
                            Text(
                                text = "${track.orderIndex}",
                                fontWeight = FontWeight.Bold,
                                color = Indigo500
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTrackUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF64748B)
                        )
                        Text(
                            text = track.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    if (isTrackUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$trackCompleted/${trackLessons.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Diagnostic Assessment banner if not yet completed
        if (profile?.isPlacementCompleted == false) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlacementTest() }
                        .testTag("placement_banner_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Indigo500)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Already know some English or coding?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(text = "Take a quick 5-question test to place into your level.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Indigo500)
                    }
                }
            }
        }

        // Daily Review & Mistakes notice if any
        if (dueReviewsCount > 0 || unresolvedMistakesCount > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReview() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Ruby600)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Targeted Review Queue", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(text = "$dueReviewsCount items due • $unresolvedMistakesCount weaknesses recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
