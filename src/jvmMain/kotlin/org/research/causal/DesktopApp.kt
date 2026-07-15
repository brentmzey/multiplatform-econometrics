package org.research.causal

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

import org.research.causal.db.JvmDatabaseDriverFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Econometrics Suite",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App(JvmDatabaseDriverFactory())
    }
}
