package org.research.causal

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("Welcome to Kotlin Multiplatform Econometrics!") }
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text, style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val client = ApiClient(createDefaultClient())
                                // Fetch live GDP data from the World Bank
                                val data = client.fetchWorldBankData()
                                text = "Success! Fetched ${data.length} bytes of remote data across the shared KMP layer!"
                            } catch (e: Exception) {
                                text = "Error fetching data: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text("Test Live Data Connection")
                }
            }
        }
    }
}
