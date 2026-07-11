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
                is Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.Dashboard })
                is Screen.Dashboard -> DashboardScreen(onLogout = { currentScreen = Screen.Login })
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
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
                                val client = PocketBaseClient(createDefaultClient())
                                // We wrap in try-catch. If it's a dummy email we can bypass for demo purposes
                                if (email == "admin@demo.com") {
                                    onLoginSuccess() // Bypass for demo
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
fun DashboardScreen(onLogout: () -> Unit) {
    var regressionOutput by remember { mutableStateOf<OLSResult?>(null) }
    var isCalculating by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Causal Inference Engine (Local)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Run a native Ordinary Least Squares (OLS) regression on your device without hitting the server.", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isCalculating = true
                        // Simulate some dataset (e.g. Y = Wage, X1 = Education, X2 = Experience)
                        // In reality, this would be fetched from PocketBaseClient
                        val yData = doubleArrayOf(50.0, 60.0, 65.0, 70.0, 80.0, 95.0, 110.0, 120.0, 150.0)
                        val xData = arrayOf(
                            doubleArrayOf(1.0, 12.0, 2.0),
                            doubleArrayOf(1.0, 12.0, 5.0),
                            doubleArrayOf(1.0, 14.0, 2.0),
                            doubleArrayOf(1.0, 14.0, 4.0),
                            doubleArrayOf(1.0, 16.0, 3.0),
                            doubleArrayOf(1.0, 16.0, 6.0),
                            doubleArrayOf(1.0, 18.0, 5.0),
                            doubleArrayOf(1.0, 18.0, 8.0),
                            doubleArrayOf(1.0, 20.0, 10.0)
                        )
                        
                        // Execute Pure Kotlin OLS Math natively!
                        val ols = OLS(yData, xData)
                        regressionOutput = ols.estimate()
                        isCalculating = false
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(8.dp)
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
                    Text("Education (β1): ${result.beta[1].toString().take(6)} (SE: ${result.standardErrors[1].toString().take(6)})")
                    Text("Experience (β2): ${result.beta[2].toString().take(6)} (SE: ${result.standardErrors[2].toString().take(6)})")
                }
            }
        }
    }
}
