package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.curriculum.CurriculumRegistry
import com.example.data.model.CourseUnit
import com.example.data.model.LearningTrack
import com.example.data.model.Lesson
import com.example.data.model.LessonProgressEntity
import com.example.ui.components.SkillTreeNode
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun LearnScreen(
    currentTrack: LearningTrack,
    onTrackSelected: (LearningTrack) -> Unit,
    progressMap: Map<String, LessonProgressEntity>,
    onSelectLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredUnits = CurriculumRegistry.units.filter { it.track == currentTrack }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("learn_screen")
    ) {
        // Horizontal Track Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurriculumRegistry.tracks.forEach { track ->
                val isSelected = track == currentTrack
                FilterChip(
                    selected = isSelected,
                    onClick = { onTrackSelected(track) },
                    label = {
                        Text(
                            text = track.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("track_chip_${track.name}")
                )
            }
        }

        // Units and Lesson Nodes List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(filteredUnits) { unit ->
                UnitSection(
                    unit = unit,
                    progressMap = progressMap,
                    onSelectLesson = onSelectLesson
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun UnitSection(
    unit: CourseUnit,
    progressMap: Map<String, LessonProgressEntity>,
    onSelectLesson: (Lesson) -> Unit
) {
    val completedCount = unit.lessons.count { progressMap[it.id]?.isCompleted == true }
    val progressFraction = if (unit.lessons.isNotEmpty()) completedCount.toFloat() / unit.lessons.size else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        // Unit Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UNIT ${unit.orderIndex}: ${unit.title.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$completedCount / ${unit.lessons.size} Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = unit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Emerald500,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skill Tree Nodes
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            unit.lessons.forEachIndexed { index, lesson ->
                SkillTreeNode(
                    lesson = lesson,
                    progress = progressMap[lesson.id],
                    onSelectLesson = onSelectLesson
                )

                // Connector vertical line between nodes
                if (index < unit.lessons.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (progressMap[lesson.id]?.isCompleted == true) Emerald500
                                else Slate700
                            )
                    )
                }
            }
        }
    }
}
