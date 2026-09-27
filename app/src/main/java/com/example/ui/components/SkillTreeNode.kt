package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.data.model.LessonProgressEntity
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun SkillTreeNode(
    lesson: Lesson,
    progress: LessonProgressEntity?,
    onSelectLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = progress?.isUnlocked ?: (lesson.orderIndex == 1)
    val isCompleted = progress?.isCompleted ?: false
    val stars = progress?.starsEarned ?: 0

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUnlocked && !isCompleted) 1.07f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 10.dp)
            .testTag("skill_tree_node_${lesson.id}")
    ) {
        // Node circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(pulseScale)
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Emerald500
                        isUnlocked -> MaterialTheme.colorScheme.primary
                        else -> Slate800
                    }
                )
                .border(
                    width = 4.dp,
                    color = when {
                        isCompleted -> Emerald500.copy(alpha = 0.4f)
                        isUnlocked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else -> Slate700
                    },
                    shape = CircleShape
                )
                .clickable(enabled = isUnlocked) {
                    onSelectLesson(lesson)
                }
        ) {
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                isUnlocked -> {
                    Text(
                        text = "${lesson.orderIndex}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Stars earned
        if (isCompleted) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (index < stars) Amber500 else Slate700,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Lesson title
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(140.dp),
            maxLines = 2
        )

        // XP Reward tag
        if (isUnlocked && !isCompleted) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Amber500.copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Amber500,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "+${lesson.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                }
            }
        }
    }
}
