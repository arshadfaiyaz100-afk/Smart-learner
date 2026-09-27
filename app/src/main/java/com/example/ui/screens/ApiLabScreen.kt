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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.api.ApiLabClient
import com.example.api.ApiTestResult
import com.example.data.model.ApiRequestHistoryEntity
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Ruby600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun ApiLabScreen(
    historyList: List<ApiRequestHistoryEntity>,
    onSaveHistory: (name: String, method: String, url: String, headersJson: String, bodyJson: String, code: Int, body: String, latency: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val methods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
    var selectedMethod by remember { mutableStateOf("GET") }
    var endpointUrl by remember { mutableStateOf("https://httpbin.org/get") }
    var headerKey by remember { mutableStateOf("Content-Type") }
    var headerValue by remember { mutableStateOf("application/json") }
    var requestBody by remember { mutableStateOf("{\n  \"title\": \"Learning Lab\",\n  \"status\": \"learning\"\n}") }

    var maskSecrets by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ApiTestResult?>(null) }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Request, 1 = Response, 2 = History
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("api_lab_screen")
    ) {
        // Header
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
                        .background(Cyan500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Http, contentDescription = null, tint = Cyan500)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Safe API Lab",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Authorized HTTP Request Workbench",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { maskSecrets = !maskSecrets }) {
                Icon(
                    imageVector = if (maskSecrets) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Mask Secrets",
                    tint = Cyan500
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Endpoints shortcut chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = endpointUrl.contains("httpbin.org/get"),
                onClick = {
                    selectedMethod = "GET"
                    endpointUrl = "https://httpbin.org/get"
                },
                label = { Text("httpbin GET") }
            )
            FilterChip(
                selected = endpointUrl.contains("httpbin.org/post"),
                onClick = {
                    selectedMethod = "POST"
                    endpointUrl = "https://httpbin.org/post"
                },
                label = { Text("httpbin POST") }
            )
            FilterChip(
                selected = endpointUrl.contains("httpbin.org/status/404"),
                onClick = {
                    selectedMethod = "GET"
                    endpointUrl = "https://httpbin.org/status/404"
                },
                label = { Text("404 Sim") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Method & URL Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when (selectedMethod) {
                    "GET" -> Emerald500
                    "POST" -> Indigo500
                    "PUT" -> Amber500
                    "DELETE" -> Ruby600
                    else -> Cyan500
                }
            ) {
                Text(
                    text = selectedMethod,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedMethod == "GET") Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            OutlinedTextField(
                value = endpointUrl,
                onValueChange = { endpointUrl = it },
                label = { Text("Endpoint URL") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("api_url_input")
            )

            Button(
                onClick = {
                    isRunning = true
                    scope.launch {
                        val headers = mapOf(headerKey to headerValue)
                        val body = if (selectedMethod in listOf("POST", "PUT", "PATCH")) requestBody else null
                        val res = ApiLabClient.executeRequest(selectedMethod, endpointUrl, headers, body)
                        testResult = res
                        isRunning = false
                        activeTab = 1 // Switch to response view

                        onSaveHistory(
                            "Request ${selectedMethod}",
                            selectedMethod,
                            endpointUrl,
                            headers.toString(),
                            body ?: "",
                            res.statusCode,
                            res.responseBody,
                            res.latencyMs
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                modifier = Modifier.testTag("api_send_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Request vs Response vs History
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Headers & Body") })
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Text(
                        if (testResult != null) "Response (${testResult!!.statusCode})" else "Response"
                    )
                }
            )
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("History (${historyList.size})") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeTab) {
            0 -> {
                // Request Config
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "HTTP Method", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = selectedMethod == m,
                                onClick = { selectedMethod = m },
                                label = { Text(m) }
                            )
                        }
                    }

                    Text(text = "Header (Key : Value)", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = headerKey,
                            onValueChange = { headerKey = it },
                            label = { Text("Key") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = if (maskSecrets && headerKey.contains("auth", ignoreCase = true)) ApiLabClient.maskSecret(headerValue) else headerValue,
                            onValueChange = { headerValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    if (selectedMethod in listOf("POST", "PUT", "PATCH")) {
                        Text(text = "JSON Request Body", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = requestBody,
                            onValueChange = { requestBody = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            1 -> {
                // Response View
                if (isRunning) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sending HTTP Request...", color = Cyan500, fontWeight = FontWeight.Bold)
                    }
                } else if (testResult != null) {
                    val r = testResult!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Status & Latency Banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (r.isSuccess) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${r.statusCode} ${r.statusMessage}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.isSuccess) Emerald500 else Ruby600
                                )
                                Text(
                                    text = "${r.latencyMs} ms",
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate800
                                )
                            }
                        }

                        // Response Body
                        Text(text = "Response Payload", style = MaterialTheme.typography.labelMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate950)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = r.responseBody.ifBlank { "(Empty response body)" },
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (r.isSuccess) Color(0xFF38BDF8) else Ruby600
                            )
                        }

                        // Headers
                        Text(text = "Response Headers (${r.responseHeaders.size})", style = MaterialTheme.typography.labelMedium)
                        r.responseHeaders.entries.take(5).forEach { (k, v) ->
                            Text(
                                text = "$k: $v",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No request executed yet. Press Send.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            2 -> {
                // History
                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No request history recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { h ->
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
                                        Text(text = "${h.method} ${h.url}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(text = "Status ${h.responseCode} • ${h.latencyMs}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
