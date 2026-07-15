package org.research.causal

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginAndDashboardNavigation() {
        composeTestRule.setContent {
            val driverFactory = org.research.causal.db.JvmDatabaseDriverFactory()
            App(driverFactory)
        }

        // Verify we are on the Login screen initially
        composeTestRule.onNodeWithText("NYT Polling Data").assertExists()
        composeTestRule.onNodeWithText("Admin Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Enter dummy credentials
        composeTestRule.onNodeWithText("Admin Email").performTextInput("admin@demo.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password1234")
        
        // Click Login button
        composeTestRule.onNodeWithText("LOGIN").performClick()
        
        // Verify we navigated to the Dashboard screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2026 Midterms Polling").assertExists()
        
        // Verify Logout button exists
        composeTestRule.onNodeWithText("Logout").assertExists()
        
        // Click Logout button
        composeTestRule.onNodeWithText("Logout").performClick()
        
        // Verify we are back on the Login screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("NYT Polling Data").assertExists()
    }
}
