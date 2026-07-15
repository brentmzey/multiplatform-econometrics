package org.research.causal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.research.causal.PollData
import kotlinx.datetime.Instant

// Data class representing a point on the line chart
data class ChartPoint(val timestamp: Long, val value: Double, val candidateName: String, val party: String)

@Composable
fun TimeSeriesLineChart(polls: List<PollData>, modifier: Modifier = Modifier) {
    if (polls.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(250.dp).background(MaterialTheme.colors.surface, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("No polling data available for chart", color = Color.Gray)
        }
        return
    }

    // Process polls into chart points
    val allPoints = mutableListOf<ChartPoint>()
    for (poll in polls) {
        val time = try {
            Instant.parse(poll.startDate).toEpochMilliseconds()
        } catch (e: Exception) {
            continue
        }
        for (res in poll.results) {
            allPoints.add(ChartPoint(time, res.pct, res.candidateName, res.party))
        }
    }

    if (allPoints.isEmpty()) return

    // Group by candidate
    val candidateGroups = allPoints.groupBy { it.candidateName }.mapValues { (_, points) ->
        points.sortedBy { it.timestamp }
    }

    val minTime = allPoints.minOf { it.timestamp }
    val maxTime = allPoints.maxOf { it.timestamp }
    val timeRange = (maxTime - minTime).coerceAtLeast(1)

    val minValue = 0.0
    val maxValue = (allPoints.maxOf { it.value } + 10.0).coerceAtMost(100.0)
    val valueRange = maxValue - minValue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Text("Polling Trends Over Time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val width = size.width
            val height = size.height

            // Draw grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = height - (i * height / gridLines)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            candidateGroups.forEach { (_, points) ->
                if (points.isEmpty()) return@forEach

                val party = points.first().party
                val lineColor = when (party.uppercase()) {
                    "DEM", "DEMOCRAT" -> Color(0xFF3B82F6) // Blue
                    "REP", "REPUBLICAN" -> Color(0xFFEF4444) // Red
                    else -> Color(0xFFF59E0B) // Yellow/Independent
                }

                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = ((point.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                    val y = height - (((point.value - minValue).toFloat() / valueRange.toFloat()) * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    
                    // Draw point dot
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        
        // Legend
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            candidateGroups.keys.take(4).forEach { candidateName ->
                val party = candidateGroups[candidateName]?.firstOrNull()?.party ?: ""
                val color = when (party.uppercase()) {
                    "DEM", "DEMOCRAT" -> Color(0xFF3B82F6)
                    "REP", "REPUBLICAN" -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(candidateName, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}
