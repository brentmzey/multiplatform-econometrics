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
            App()
        }

        // Verify we are on the Login screen initially
        composeTestRule.onNodeWithText("Econometrics Suite").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Enter dummy credentials
        composeTestRule.onNodeWithText("Email").performTextInput("admin@demo.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password1234")
        
        // Click Login button
        composeTestRule.onNodeWithText("LOGIN").performClick()
        
        // Verify we navigated to the Dashboard screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Econometrics Dashboard").assertExists()
        
        // Verify Upload Data button exists
        composeTestRule.onNodeWithText("Upload Data").assertExists()
        
        // Click Logout button
        composeTestRule.onNodeWithText("Logout").performClick()
        
        // Verify we are back on the Login screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Econometrics Suite").assertExists()
    }
}
