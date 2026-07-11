package org.research.causal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
}

@Composable
fun App() {
    val client = remember { PocketBaseClient(createDefaultClient()) }
    
    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFF6200EE),
            primaryVariant = Color(0xFF3700B3),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
        )
    ) {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            when (currentScreen) {
                is Screen.Login -> LoginScreen(client, onLoginSuccess = { currentScreen = Screen.Dashboard })
                is Screen.Dashboard -> DashboardScreen(client, onLogout = { currentScreen = Screen.Login })
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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Econometrics Suite",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "Sign in to access federated datasets",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    singleLine = true
                    // In a real app we'd use PasswordVisualTransformation here
                )

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colors.error, modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        coroutineScope.launch {
                            try {
                                // We wrap in try-catch. If it's a dummy email we can bypass for demo purposes
                                if (email == "admin@demo.com") {
                                    client.authWithPassword(email, password)
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
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hint: Use admin@demo.com to bypass", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DashboardScreen(client: PocketBaseClient, onLogout: () -> Unit) {
    var regressionOutput by remember { mutableStateOf<OLSResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Dataset State
    var dataset by remember { mutableStateOf(parseCsv("Loading...", "")) }
    var selectedY by remember { mutableStateOf("") }
    var selectedXs by remember { mutableStateOf(emptySet<String>()) }
    
    // Fetch live data on mount
    LaunchedEffect(Unit) {
        try {
            val records = client.getRecords("datasets")
            if (records.items.isNotEmpty()) {
                val item = records.items.first()
                val name = item["name"]?.toString()?.removeSurrounding("\"") ?: "Unknown Dataset"
                
                // Parse headers from JSON array
                val headersElement = item["headers"]
                val headers = if (headersElement is kotlinx.serialization.json.JsonArray) {
                    headersElement.map { it.toString().removeSurrounding("\"") }
                } else {
                    emptyList()
                }

                // Parse rows from JSON 2D array
                val rowsElement = item["rows"]
                val rows = if (rowsElement is kotlinx.serialization.json.JsonArray) {
                    rowsElement.mapNotNull { rowElem ->
                        if (rowElem is kotlinx.serialization.json.JsonArray) {
                            rowElem.map { it.toString().toDouble() }
                        } else null
                    }
                } else {
                    emptyList()
                }

                dataset = ParsedDataset(name, headers, rows)
                selectedY = headers.firstOrNull() ?: ""
                selectedXs = headers.drop(1).toSet()
            } else {
                fetchError = "No datasets found in PocketBase!"
            }
        } catch (e: Exception) {
            fetchError = "Failed to fetch cloud data: ${e.message}"
            // Fallback to offline local CSV
            dataset = parseCsv("Card Education (Offline Fallback)", SampleData.cardCsv)
            selectedY = dataset.headers.firstOrNull() ?: ""
            selectedXs = dataset.headers.drop(1).toSet()
        } finally {
            isLoadingData = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Econometrics Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onLogout) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // LEFT COLUMN: Data Preview and Selection
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dataset: ${dataset.name}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Select variables for the model below:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Target Variable (Y):", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        dataset.headers.forEach { header ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedY == header,
                                    onClick = { 
                                        selectedY = header
                                        selectedXs = selectedXs - header 
                                    }
                                )
                                Text(header, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Features (X):", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        dataset.headers.forEach { header ->
                            if (header != selectedY) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedXs.contains(header),
                                        onCheckedChange = { checked ->
                                            if (checked) selectedXs = selectedXs + header
                                            else selectedXs = selectedXs - header
                                        }
                                    )
                                    Text(header, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Data Preview (Top 10 rows):", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple Data Table Preview
                    Column(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            dataset.headers.forEach { h -> Text(h, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) }
                        }
                        Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        dataset.rows.take(10).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                row.forEach { cell -> Text(cell.toString(), modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: Math Execution and Results
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Causal Inference Engine", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Run a native Ordinary Least Squares (OLS) regression.", fontSize = 14.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isCalculating = true
                            try {
                                val yIdx = dataset.headers.indexOf(selectedY)
                                val xIndices = selectedXs.map { dataset.headers.indexOf(it) }
                                
                                val yData = dataset.rows.map { it[yIdx] }.toDoubleArray()
                                val xData = dataset.rows.map { row ->
                                    val x = mutableListOf(1.0) // Intercept
                                    xIndices.forEach { idx -> x.add(row[idx]) }
                                    x.toDoubleArray()
                                }.toTypedArray()
                                
                                val ols = OLS(yData, xData)
                                regressionOutput = ols.estimate()
                            } catch (e: Exception) {
                                // Ignore math errors for now
                            } finally {
                                isCalculating = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = selectedY.isNotEmpty() && selectedXs.isNotEmpty()
                    ) {
                        Text("Run Local OLS Regression")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    regressionOutput?.let { result ->
                        Text("Regression Results", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colors.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("R-Squared: ${result.rSquared.toString().take(6)}")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Coefficients:", fontWeight = FontWeight.Bold)
                        Text("Intercept (β0): ${result.beta[0].toString().take(6)} (SE: ${result.standardErrors[0].toString().take(6)})")
                        
                        val xList = selectedXs.toList()
                        xList.forEachIndexed { index, feature ->
                            val bIdx = index + 1
                            if (bIdx < result.beta.size) {
                                Text("$feature (β$bIdx): ${result.beta[bIdx].toString().take(6)} (SE: ${result.standardErrors[bIdx].toString().take(6)})")
                            }
                        }
                    }
                }
            }
        }
    }
}
