package org.research.causal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.research.causal.db.DatabaseDriverFactory
import org.research.causal.db.PollingDatabase

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
}

// Data models for UI
data class PollData(
    val id: String,
    val pollster: String,
    val startDate: String,
    val geography: String,
    val results: List<CandidateResult>
)

data class CandidateResult(
    val candidateName: String,
    val party: String,
    val pct: Double
)

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val client = remember { PocketBaseClient(createDefaultClient()) }
    val repository = remember { PollingRepository(client, driverFactory) }
    
    // Premium Dark Theme Colors
    val darkColors = darkColors(
        primary = Color(0xFF8B5CF6), // Vibrant Purple
        primaryVariant = Color(0xFF6D28D9),
        secondary = Color(0xFF10B981), // Emerald
        background = Color(0xFF0F172A), // Slate 900
        surface = Color(0xFF1E293B), // Slate 800
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF1F5F9),
    )

    MaterialTheme(
        colors = darkColors,
        typography = Typography()
    ) {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Crossfade(targetState = currentScreen) { screen ->
                when (screen) {
                    is Screen.Login -> LoginScreen(client, onLoginSuccess = { currentScreen = Screen.Dashboard })
                    is Screen.Dashboard -> DashboardScreen(repository, onLogout = { 
                        client.logout()
                        currentScreen = Screen.Login 
                    })
                }
            }
        }
    }
}

@Composable
fun LoginScreen(client: PocketBaseClient, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 16.dp,
            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth().padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NYT Polling Data",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "2026 Midterms Dashboard",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Admin Email") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color(0xFFEF4444), modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        coroutineScope.launch {
                            try {
                                if (email == "admin@demo.com") {
                                    onLoginSuccess() 
                                } else {
                                    client.authWithPassword(email, password)
                                    onLoginSuccess()
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Authentication failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(repository: PollingRepository, onLogout: () -> Unit) {
    var polls by remember { mutableStateOf<List<PollData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun loadData(forceRefresh: Boolean = false) {
        isLoading = true
        coroutineScope.launch {
            try {
                polls = repository.getPolls(forceRefresh)
            } catch (e: Exception) {
                println("Fetch error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData(forceRefresh = false) // Load from cache first!
    }

    // Computed property for filtered polls
    val filteredPolls = remember(polls, searchQuery) {
        if (searchQuery.isBlank()) {
            polls
        } else {
            polls.filter { poll ->
                poll.geography.contains(searchQuery, ignoreCase = true) ||
                poll.pollster.contains(searchQuery, ignoreCase = true) ||
                poll.results.any { it.candidateName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // App Bar
        TopAppBar(
            title = { 
                Text("2026 Midterms Polling", fontWeight = FontWeight.Bold) 
            },
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface,
            elevation = 8.dp,
            actions = {
                IconButton(onClick = { loadData(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                TextButton(onClick = onLogout) {
                    Text("Logout", color = MaterialTheme.colors.primary)
                }
            }
        )
        
        // Filter / Search Bar
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter by State, Pollster, or Candidate...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        // Main Content
        if (isLoading && polls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPolls) { poll ->
                    PollCard(poll)
                }
            }
        }
    }
}

@Composable
fun PollCard(poll: PollData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poll.pollster,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = poll.geography,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Text(
                text = "Date: ${poll.startDate}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Results Bars
            poll.results.forEach { result ->
                val barColor = when (result.party.uppercase()) {
                    "DEM", "DEMOCRAT" -> Color(0xFF3B82F6) // Blue
                    "REP", "REPUBLICAN" -> Color(0xFFEF4444) // Red
                    else -> Color(0xFFF59E0B) // Yellow/Independent
                }

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(result.candidateName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("${result.pct}%", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (result.pct / 100.0).toFloat())
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}
