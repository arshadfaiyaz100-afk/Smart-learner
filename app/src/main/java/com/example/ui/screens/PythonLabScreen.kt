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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AIProvider
import com.example.data.model.TestCase
import com.example.python.PythonEngine
import com.example.python.PythonExecutionResult
import com.example.ui.components.PythonCodeEditor
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Ruby600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

data class PythonChallenge(
    val title: String,
    val level: String,
    val prompt: String,
    val initialCode: String,
    val testCases: List<TestCase>,
    val hint: String
)

@Composable
fun PythonLabScreen(
    aiProvider: AIProvider,
    onSaveSnippet: (title: String, code: String, output: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val challenges = remember {
        listOf(
            PythonChallenge(
                title = "FizzBuzz Classic",
                level = "Beginner",
                prompt = "Print numbers 1 to 15. For multiples of 3, print 'Fizz'. For multiples of 5, print 'Buzz'. For multiples of both, print 'FizzBuzz'.",
                initialCode = """
# Task: Complete the FizzBuzz loop
for n in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]:
    if n % 3 == 0 and n % 5 == 0:
        print("FizzBuzz")
    elif n % 3 == 0:
        print("Fizz")
    elif n % 5 == 0:
        print("Buzz")
    else:
        print(n)
""".trimIndent(),
                testCases = listOf(
                    TestCase("Sample", "", "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz")
                ),
                hint = "Check n % 15 == 0 (both 3 and 5) first before individual cases."
            ),
            PythonChallenge(
                title = "Reverse String Function",
                level = "Beginner",
                prompt = "Define a function reverse_str(text) that returns the reversed string.",
                initialCode = """
def reverse_str(s):
    # Hint: you can use slicing or loop
    return s[::-1]

print("cat ->", reverse_str("cat"))
print("learning ->", reverse_str("learning"))
""".trimIndent(),
                testCases = listOf(
                    TestCase("Reverse cat", "reverse_str('cat')", "tac")
                ),
                hint = "In Python, slice s[::-1] steps backwards through the string."
            ),
            PythonChallenge(
                title = "Sum of Evens",
                level = "Intermediate",
                prompt = "Define sum_evens(nums) returning the total sum of even integers.",
                initialCode = """
def sum_evens(nums):
    total = 0
    for x in nums:
        if x % 2 == 0:
            total = total + x
    return total

print("Sum:", sum_evens([1, 2, 3, 4, 5, 6]))
""".trimIndent(),
                testCases = listOf(
                    TestCase("Evens test", "sum_evens([2, 4, 6])", "12")
                ),
                hint = "Use % 2 == 0 to check if a number is even."
            )
        )
    }

    var selectedChallengeIndex by remember { mutableIntStateOf(0) }
    val currentChallenge = challenges[selectedChallengeIndex]

    var currentCode by remember(selectedChallengeIndex) { mutableStateOf(currentChallenge.initialCode) }
    var executionResult by remember { mutableStateOf<PythonExecutionResult?>(null) }
    var isExecuting by remember { mutableStateOf(false) }
    var aiReviewText by remember { mutableStateOf<String?>(null) }
    var showHint by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("python_lab_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Emerald500)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Python Lab",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Safe Sandboxed Runtime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Save snippet button
            FilledTonalButton(
                onClick = {
                    onSaveSnippet(
                        currentChallenge.title,
                        currentCode,
                        executionResult?.output ?: ""
                    )
                    saveSuccessMessage = "Saved to My Code!"
                },
                modifier = Modifier.testTag("save_code_snippet_btn")
            ) {
                Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Save")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }

        if (saveSuccessMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = saveSuccessMessage!!,
                style = MaterialTheme.typography.labelSmall,
                color = Emerald500,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Challenge Selector horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            challenges.forEachIndexed { index, ch ->
                FilterChip(
                    selected = selectedChallengeIndex == index,
                    onClick = {
                        selectedChallengeIndex = index
                        executionResult = null
                        aiReviewText = null
                        showHint = false
                        saveSuccessMessage = null
                    },
                    label = { Text(ch.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Challenge prompt card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentChallenge.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showHint) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Hint: ${currentChallenge.hint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showHint = !showHint }) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint", tint = Amber500)
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val review = aiProvider.reviewCode(currentCode)
                                aiReviewText = review.getOrNull() ?: "Review unavailable"
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI Review", tint = Indigo500)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code Editor & Console in scrollable column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Interactive Code Editor
            PythonCodeEditor(
                code = currentCode,
                onCodeChange = { currentCode = it },
                onRunCode = {
                    isExecuting = true
                    scope.launch {
                        executionResult = PythonEngine.execute(currentCode, currentChallenge.testCases)
                        isExecuting = false
                    }
                },
                onResetCode = {
                    currentCode = currentChallenge.initialCode
                    executionResult = null
                    aiReviewText = null
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI Code Review Banner if generated
            if (!aiReviewText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Indigo500)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Code Review",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = aiReviewText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Output Console Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate950),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("python_output_console")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STDOUT CONSOLE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        if (executionResult != null) {
                            Text(
                                text = "${executionResult!!.executionTimeMs} ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val outText = executionResult?.output
                    val errText = executionResult?.error

                    if (isExecuting) {
                        Text(
                            text = "Executing in isolated sandbox...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber500,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (errText != null) {
                        Text(
                            text = errText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Ruby600,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (!outText.isNullOrBlank()) {
                        Text(
                            text = outText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF34D399),
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "Press 'Run' to execute in sandbox.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Test Cases Pass/Fail results
                    executionResult?.testCaseResults?.let { testResults ->
                        if (testResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "TEST SUITE RESULTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            testResults.forEach { tr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (tr.isPassed) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (tr.isPassed) Emerald500 else Ruby600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${tr.name}: ${if (tr.isPassed) "PASSED" else "FAILED (Expected: ${tr.expected})"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (tr.isPassed) Emerald500 else Ruby600,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
