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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No polling data available for chart", color = Color(0xFF94A3B8))
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
            Text("Polling Trends Over Time", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Text("State-by-state and national aggregate trends", fontSize = 14.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(24.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            val width = size.width
            val height = size.height

            // Draw grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = height - (i * height / gridLines)
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Optional: Draw Y axis labels
                // Not supported cleanly in Canvas without native text layout, skipping for minimal UI.
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
                val fillPath = Path() // For gradient fill under the line
                
                fillPath.moveTo(0f, height)
                
                points.forEachIndexed { index, point ->
                    val x = ((point.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                    val y = height - (((point.value - minValue).toFloat() / valueRange.toFloat()) * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth cubic bezier calculation (simplified for points)
                        val prevPoint = points[index - 1]
                        val prevX = ((prevPoint.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                        val prevY = height - (((prevPoint.value - minValue).toFloat() / valueRange.toFloat()) * height)
                        
                        val controlX1 = prevX + (x - prevX) / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + (x - prevX) / 2f
                        val controlY2 = y
                        
                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }
                }
                
                // Complete fill path
                val lastX = ((points.last().timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                fillPath.lineTo(lastX, height)
                fillPath.close()

                // Draw Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                
                // Draw glowing dots
                points.forEach { point ->
                    val x = ((point.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                    val y = height - (((point.value - minValue).toFloat() / valueRange.toFloat()) * height)
                    
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // Legend
        Spacer(modifier = Modifier.height(24.dp))
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(candidateName, color = Color(0xFFF1F5F9), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
