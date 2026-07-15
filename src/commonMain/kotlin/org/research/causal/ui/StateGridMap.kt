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

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun StateGridMap(polls: List<PollData>, modifier: Modifier = Modifier, onStateSelected: (String?) -> Unit) {
    // Calculate margins per state
    val stateMargins = mutableMapOf<String, Pair<String, Double>>() // Abbreviation -> Pair(WinningParty, MarginPct)
    
    // Process the most recent poll for each state
    val statePolls = polls.groupBy { it.geography }
    for ((state, statePollList) in statePolls) {
        val abbrev = stateAbbreviations[state] ?: continue
        val latestPoll = statePollList.sortedByDescending { it.startDate }.firstOrNull() ?: continue
        
        val sortedResults = latestPoll.results.sortedByDescending { it.pct }
        if (sortedResults.isNotEmpty()) {
            val winner = sortedResults[0]
            val runnerUp = if (sortedResults.size > 1) sortedResults[1] else null
            val margin = winner.pct - (runnerUp?.pct ?: 0.0)
            stateMargins[abbrev] = Pair(winner.party.uppercase(), margin)
        }
    }

    var hoveredState by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var selectedState by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Electoral Map Forecast", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            if (selectedState != null) {
                androidx.compose.material.TextButton(
                    onClick = { 
                        selectedState = null
                        onStateSelected(null) 
                    }
                ) {
                    Text("Clear Selection", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Latest state-level polling margins. Click a state to filter.", fontSize = 14.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(24.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val boxSize = (maxWidth / 12).coerceAtMost(56.dp)
            val spacing = 4.dp

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                usGrid.forEach { row ->
                    Row(modifier = Modifier.padding(vertical = spacing / 2)) {
                        row.forEach { abbrev ->
                            if (abbrev.isBlank()) {
                                Spacer(modifier = Modifier.size(boxSize).padding(horizontal = spacing / 2))
                            } else {
                                val marginData = stateMargins[abbrev]
                                val isHovered = hoveredState == abbrev
                                val isSelected = selectedState == abbrev
                                
                                val backgroundColor = if (marginData != null) {
                                    val (party, margin) = marginData
                                    val baseColor = when (party) {
                                        "DEM", "DEMOCRAT" -> Color(0xFF3B82F6) // Blue
                                        "REP", "REPUBLICAN" -> Color(0xFFEF4444) // Red
                                        else -> Color(0xFFF59E0B) // Yellow
                                    }
                                    val alpha = (0.3f + (margin / 15.0f) * 0.7f).toFloat().coerceIn(0.3f, 1.0f)
                                    baseColor.copy(alpha = if (isSelected || isHovered) 1.0f else alpha)
                                } else {
                                    if (isHovered || isSelected) Color(0xFF475569) else Color(0xFF1E293B)
                                }

                                val borderColor = if (isSelected) Color.White else if (isHovered) Color(0xFF94A3B8) else Color(0xFF334155)

                                Box(
                                    modifier = Modifier
                                        .size(boxSize)
                                        .padding(horizontal = spacing / 2)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(backgroundColor)
                                        .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
                                        .androidx.compose.foundation.clickable {
                                            if (selectedState == abbrev) {
                                                selectedState = null
                                                onStateSelected(null)
                                            } else {
                                                selectedState = abbrev
                                                onStateSelected(abbrev)
                                            }
                                        }
                                        .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    when (event.type) {
                                                        androidx.compose.ui.input.pointer.PointerEventType.Enter -> hoveredState = abbrev
                                                        androidx.compose.ui.input.pointer.PointerEventType.Exit -> {
                                                            if (hoveredState == abbrev) hoveredState = null
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .androidx.compose.ui.input.pointer.pointerHoverIcon(androidx.compose.ui.input.pointer.PointerIcon.Hand),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = abbrev,
                                            color = Color.White,
                                            fontSize = (boxSize.value * 0.3f).sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        if (marginData != null) {
                                            Text(
                                                text = "+${marginData.second.toInt()}",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = (boxSize.value * 0.22f).sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
