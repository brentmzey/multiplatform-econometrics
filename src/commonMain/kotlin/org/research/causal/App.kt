package org.research.causal

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("Welcome to Kotlin Multiplatform Econometrics!") }
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text, style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    text = "Hello from Compose Multiplatform! (Identical UX across iOS, Android, Web, Desktop)"
                }
            ) {
                Text("Test Kotlin Multiplatform UI")
            }
        }
    }
}
