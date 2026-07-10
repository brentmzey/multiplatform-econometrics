package org.research.causal

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Econometrics Compose Multiplatform UI") {
        App()
    }
}
