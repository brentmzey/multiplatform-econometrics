package org.research.causal.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import org.research.causal.CandidateResult
import org.research.causal.PollData

class VisualizationTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePolls = listOf(
        PollData(
            id = "1",
            pollster = "Siena College",
            startDate = "2026-10-01T00:00:00Z",
            geography = "Pennsylvania",
            results = listOf(
                CandidateResult("Fetterman", "DEM", 51.0),
                CandidateResult("McCormick", "REP", 48.0)
            )
        ),
        PollData(
            id = "2",
            pollster = "Quinnipiac",
            startDate = "2026-10-05T00:00:00Z",
            geography = "Florida",
            results = listOf(
                CandidateResult("Rubio", "REP", 53.0),
                CandidateResult("Demings", "DEM", 45.0)
            )
        )
    )

    @Test
    fun testTimeSeriesChartRendersWithData() {
        composeTestRule.setContent {
            TimeSeriesLineChart(polls = samplePolls)
        }

        // Verify the title is displayed
        composeTestRule.onNodeWithText("Polling Trends Over Time").assertIsDisplayed()
        
        // Verify candidate legends are rendered
        composeTestRule.onNodeWithText("Fetterman").assertIsDisplayed()
        composeTestRule.onNodeWithText("McCormick").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Demings").assertIsDisplayed()
    }

    @Test
    fun testStateGridMapRendersWithData() {
        composeTestRule.setContent {
            StateGridMap(polls = samplePolls)
        }

        // Verify title
        composeTestRule.onNodeWithText("Latest Polling Margins Map").assertIsDisplayed()
        
        // Verify states are rendered (using PA and FL from our mock data)
        composeTestRule.onNodeWithText("PA").assertIsDisplayed()
        composeTestRule.onNodeWithText("FL").assertIsDisplayed()
        
        // And verify an empty/non-polled state also renders
        composeTestRule.onNodeWithText("NY").assertIsDisplayed()
    }

    @Test
    fun testEmptyChartRendersEmptyState() {
        composeTestRule.setContent {
            TimeSeriesLineChart(polls = emptyList())
        }

        composeTestRule.onNodeWithText("No polling data available for chart").assertIsDisplayed()
    }
}
