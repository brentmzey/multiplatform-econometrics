package org.research.causal

import androidx.compose.ui.window.ComposeUIViewController

import org.research.causal.db.IosDatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(IosDatabaseDriverFactory()) }
