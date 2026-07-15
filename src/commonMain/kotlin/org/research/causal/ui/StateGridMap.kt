package org.research.causal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.research.causal.PollData

// Standard Grid layout for US States
val usGrid = listOf(
    listOf("AK", "", "", "", "", "", "", "", "", "", "ME"),
    listOf("", "", "", "", "", "", "", "", "", "VT", "NH"),
    listOf("WA", "ID", "MT", "ND", "MN", "IL", "WI", "MI", "NY", "RI", "MA"),
    listOf("OR", "NV", "WY", "SD", "IA", "IN", "OH", "PA", "NJ", "CT", ""),
    listOf("CA", "UT", "CO", "NE", "KS", "MO", "KY", "WV", "VA", "MD", "DE"),
    listOf("", "AZ", "NM", "OK", "AR", "TN", "NC", "SC", "DC", "", ""),
    listOf("HI", "", "", "TX", "LA", "MS", "AL", "GA", "", "", ""),
    listOf("", "", "", "", "", "", "", "FL", "", "", "")
)

// Mapping NYT State names to Abbreviations
val stateAbbreviations = mapOf(
    "Alabama" to "AL", "Alaska" to "AK", "Arizona" to "AZ", "Arkansas" to "AR", "California" to "CA",
    "Colorado" to "CO", "Connecticut" to "CT", "Delaware" to "DE", "Florida" to "FL", "Georgia" to "GA",
    "Hawaii" to "HI", "Idaho" to "ID", "Illinois" to "IL", "Indiana" to "IN", "Iowa" to "IA",
    "Kansas" to "KS", "Kentucky" to "KY", "Louisiana" to "LA", "Maine" to "ME", "Maryland" to "MD",
    "Massachusetts" to "MA", "Michigan" to "MI", "Minnesota" to "MN", "Mississippi" to "MS", "Missouri" to "MO",
    "Montana" to "MT", "Nebraska" to "NE", "Nevada" to "NV", "New Hampshire" to "NH", "New Jersey" to "NJ",
    "New Mexico" to "NM", "New York" to "NY", "North Carolina" to "NC", "North Dakota" to "ND", "Ohio" to "OH",
    "Oklahoma" to "OK", "Oregon" to "OR", "Pennsylvania" to "PA", "Rhode Island" to "RI", "South Carolina" to "SC",
    "South Dakota" to "SD", "Tennessee" to "TN", "Texas" to "TX", "Utah" to "UT", "Vermont" to "VT",
    "Virginia" to "VA", "Washington" to "WA", "West Virginia" to "WV", "Wisconsin" to "WI", "Wyoming" to "WY",
    "District of Columbia" to "DC"
)

@Composable
fun StateGridMap(polls: List<PollData>, modifier: Modifier = Modifier) {
    // Calculate margins per state
    val stateMargins = mutableMapOf<String, Pair<String, Double>>() // Abbreviation -> Pair(WinningParty, MarginPct)
    
    // Process the most recent poll for each state
    val statePolls = polls.groupBy { it.geography }
    for ((state, statePollList) in statePolls) {
        val abbrev = stateAbbreviations[state] ?: continue
        // Sort by start date descending to get the latest
        val latestPoll = statePollList.sortedByDescending { it.startDate }.firstOrNull() ?: continue
        
        // Find top 2 candidates
        val sortedResults = latestPoll.results.sortedByDescending { it.pct }
        if (sortedResults.isNotEmpty()) {
            val winner = sortedResults[0]
            val runnerUp = if (sortedResults.size > 1) sortedResults[1] else null
            
            val margin = winner.pct - (runnerUp?.pct ?: 0.0)
            stateMargins[abbrev] = Pair(winner.party.uppercase(), margin)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Text("Latest Polling Margins Map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            usGrid.forEach { row ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    row.forEach { abbrev ->
                        if (abbrev.isBlank()) {
                            Spacer(modifier = Modifier.size(32.dp).padding(2.dp))
                        } else {
                            val marginData = stateMargins[abbrev]
                            val backgroundColor = if (marginData != null) {
                                val (party, margin) = marginData
                                val baseColor = when (party) {
                                    "DEM", "DEMOCRAT" -> Color(0xFF3B82F6) // Blue
                                    "REP", "REPUBLICAN" -> Color(0xFFEF4444) // Red
                                    else -> Color(0xFFF59E0B) // Yellow
                                }
                                // Fade based on margin (0-20% margin scales opacity)
                                val alpha = (0.4f + (margin / 20.0f) * 0.6f).toFloat().coerceIn(0.4f, 1.0f)
                                baseColor.copy(alpha = alpha)
                            } else {
                                Color.DarkGray
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(backgroundColor)
                                    .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = abbrev,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
