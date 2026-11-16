package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.GameEntity
import com.example.data.RoundEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.util.PersianNumberHelper.formatScore
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class ProgressionPoint(
    val roundIndex: Int,
    val roundNumber: Int,
    val team1Score: Int,
    val team2Score: Int,
    val team1Delta: Int,
    val team2Delta: Int,
    val status: String,
    val hakemPlayerName: String
)

@Composable
fun ScoreProgressionLineChart(
    game: GameEntity,
    rounds: List<RoundEntity>,
    modifier: Modifier = Modifier
) {
    val completedRounds = remember(rounds) {
        rounds.filter { it.isCompleted && it.status != "Ongoing" }
            .sortedBy { it.roundNumber }
    }

    if (completedRounds.isEmpty()) {
        return
    }

    // Build cumulative points starting with Round 0 (0, 0)
    val points = remember(completedRounds) {
        val list = mutableListOf<ProgressionPoint>()
        var t1Cum = 0
        var t2Cum = 0

        // Round 0 baseline
        list.add(
            ProgressionPoint(
                roundIndex = 0,
                roundNumber = 0,
                team1Score = 0,
                team2Score = 0,
                team1Delta = 0,
                team2Delta = 0,
                status = "Start",
                hakemPlayerName = ""
            )
        )

        completedRounds.forEachIndexed { index, round ->
            t1Cum += round.team1FinalScore
            t2Cum += round.team2FinalScore
            list.add(
                ProgressionPoint(
                    roundIndex = index + 1,
                    roundNumber = round.roundNumber,
                    team1Score = t1Cum,
                    team2Score = t2Cum,
                    team1Delta = round.team1FinalScore,
                    team2Delta = round.team2FinalScore,
                    status = round.status,
                    hakemPlayerName = round.hakemPlayerName
                )
            )
        }
        list
    }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val selectedPoint = selectedPointIndex?.let { idx -> points.getOrNull(idx) }

    val lastPoint = points.last()
    val team1Final = lastPoint.team1Score
    val team2Final = lastPoint.team2Score

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("score_progression_line_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.score_progression_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.score_progression_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Legend & Current Scores Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldGreen, CircleShape)
                    )
                    Text(
                        text = "${game.team1Name}: ${formatScore(team1Final)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }

                // Team 2 Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(GoldAccent, CircleShape)
                    )
                    Text(
                        text = "${game.team2Name}: ${formatScore(team2Final)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            // Interactive Tooltip / Detail Card if a point is tapped
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedPoint != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedPoint.roundNumber == 0) {
                                        stringResource(R.string.chart_start_point)
                                    } else {
                                        stringResource(R.string.chart_round_label, selectedPoint.roundNumber.toPersianDigits())
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (selectedPoint.hakemPlayerName.isNotBlank()) {
                                    Text(
                                        text = "👑 ${selectedPoint.hakemPlayerName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${game.team1Name}: ${formatScore(selectedPoint.team1Score)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    if (selectedPoint.roundNumber > 0) {
                                        Text(
                                            text = stringResource(
                                                R.string.round_delta_format,
                                                formatScore(selectedPoint.team1Delta)
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${game.team2Name}: ${formatScore(selectedPoint.team2Score)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                    if (selectedPoint.roundNumber > 0) {
                                        Text(
                                            text = stringResource(
                                                R.string.round_delta_format,
                                                formatScore(selectedPoint.team2Delta)
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Canvas Chart
            val context = LocalContext.current
            val vazirTypeface = remember(context) {
                runCatching { ResourcesCompat.getFont(context, R.font.vazir_regular) }.getOrNull()
            }
            val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant
            val surfaceColor = MaterialTheme.colorScheme.surface
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val textPaint = remember(textColor, vazirTypeface) {
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (textColor.alpha * 255).toInt(),
                        (textColor.red * 255).toInt(),
                        (textColor.green * 255).toInt(),
                        (textColor.blue * 255).toInt()
                    )
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    vazirTypeface?.let { typeface = it }
                }
            }

            val targetScore = game.targetScore

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val width = size.width.toFloat()
                            val paddingStart = 70f
                            val paddingEnd = 30f
                            val chartWidth = width - paddingStart - paddingEnd
                            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

                            // Find nearest point
                            var closestIdx = 0
                            var minDist = Float.MAX_VALUE
                            points.indices.forEach { idx ->
                                val ptX = paddingStart + idx * stepX
                                val dist = abs(offset.x - ptX)
                                if (dist < minDist) {
                                    minDist = dist
                                    closestIdx = idx
                                }
                            }

                            selectedPointIndex = if (selectedPointIndex == closestIdx) null else closestIdx
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 70f
                    val paddingRight = 30f
                    val paddingTop = 30f
                    val paddingBottom = 40f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // Determine Min and Max scores for Y scaling
                    var minVal = 0
                    var maxVal = targetScore

                    points.forEach { pt ->
                        if (pt.team1Score < minVal) minVal = pt.team1Score
                        if (pt.team2Score < minVal) minVal = pt.team2Score
                        if (pt.team1Score > maxVal) maxVal = pt.team1Score
                        if (pt.team2Score > maxVal) maxVal = pt.team2Score
                    }

                    // Add 10% breathing room to max/min
                    val range = max(100, maxVal - minVal)
                    val yMin = (minVal / 50) * 50
                    val yMax = ((maxVal + 49) / 50) * 50
                    val totalSpan = max(100, yMax - yMin)

                    fun getY(score: Int): Float {
                        val fraction = (score - yMin).toFloat() / totalSpan.toFloat()
                        return paddingTop + chartHeight * (1f - fraction)
                    }

                    fun getX(index: Int): Float {
                        if (points.size <= 1) return paddingLeft + chartWidth / 2
                        return paddingLeft + index * (chartWidth / (points.size - 1))
                    }

                    // Draw Horizontal Grid Lines and Y-Axis Labels
                    val ySteps = 4
                    val stepVal = totalSpan / ySteps
                    for (i in 0..ySteps) {
                        val gridScore = yMin + i * stepVal
                        val y = getY(gridScore)

                        // Grid line
                        drawLine(
                            color = outlineColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Y-axis text
                        drawContext.canvas.nativeCanvas.drawText(
                            formatScore(gridScore),
                            paddingLeft - 35f,
                            y + 10f,
                            textPaint
                        )
                    }

                    // Draw Target Score Line if within range
                    if (targetScore in (yMin + 1) until yMax) {
                        val targetY = getY(targetScore)
                        drawLine(
                            color = GoldAccent.copy(alpha = 0.5f),
                            start = Offset(paddingLeft, targetY),
                            end = Offset(width - paddingRight, targetY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        )
                    }

                    // Draw 0 Baseline if min is negative
                    if (yMin < 0 && yMax > 0) {
                        val zeroY = getY(0)
                        drawLine(
                            color = onSurfaceColor.copy(alpha = 0.4f),
                            start = Offset(paddingLeft, zeroY),
                            end = Offset(width - paddingRight, zeroY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Draw Vertical Selection Line
                    selectedPointIndex?.let { selIdx ->
                        val selX = getX(selIdx)
                        drawLine(
                            color = primaryColor.copy(alpha = 0.7f),
                            start = Offset(selX, paddingTop),
                            end = Offset(selX, height - paddingBottom),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }

                    // Build and Draw Paths for Team 1 & Team 2
                    val team1Path = Path()
                    val team2Path = Path()

                    val team1FillPath = Path()
                    val team2FillPath = Path()

                    val zeroOrBottomY = getY(minVal.coerceAtLeast(yMin))

                    points.forEachIndexed { i, pt ->
                        val x = getX(i)
                        val y1 = getY(pt.team1Score)
                        val y2 = getY(pt.team2Score)

                        if (i == 0) {
                            team1Path.moveTo(x, y1)
                            team2Path.moveTo(x, y2)

                            team1FillPath.moveTo(x, zeroOrBottomY)
                            team1FillPath.lineTo(x, y1)

                            team2FillPath.moveTo(x, zeroOrBottomY)
                            team2FillPath.lineTo(x, y2)
                        } else {
                            team1Path.lineTo(x, y1)
                            team2Path.lineTo(x, y2)

                            team1FillPath.lineTo(x, y1)
                            team2FillPath.lineTo(x, y2)
                        }
                    }

                    val lastX = getX(points.size - 1)
                    team1FillPath.lineTo(lastX, zeroOrBottomY)
                    team1FillPath.close()

                    team2FillPath.lineTo(lastX, zeroOrBottomY)
                    team2FillPath.close()

                    // Fill gradient under Team 1
                    drawPath(
                        path = team1FillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(EmeraldGreen.copy(alpha = 0.22f), Color.Transparent),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                    )

                    // Fill gradient under Team 2
                    drawPath(
                        path = team2FillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(GoldAccent.copy(alpha = 0.18f), Color.Transparent),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                    )

                    // Draw Line Strokes
                    drawPath(
                        path = team1Path,
                        color = EmeraldGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = team2Path,
                        color = GoldAccent,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Data Point Circles
                    points.forEachIndexed { i, pt ->
                        val x = getX(i)
                        val y1 = getY(pt.team1Score)
                        val y2 = getY(pt.team2Score)

                        val isSelected = selectedPointIndex == i

                        // Team 1 Node
                        drawCircle(
                            color = surfaceColor,
                            radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                            center = Offset(x, y1)
                        )
                        drawCircle(
                            color = EmeraldGreen,
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = Offset(x, y1)
                        )

                        // Team 2 Node
                        drawCircle(
                            color = surfaceColor,
                            radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                            center = Offset(x, y2)
                        )
                        drawCircle(
                            color = GoldAccent,
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = Offset(x, y2)
                        )

                        // X-axis Round Number labels
                        val label = pt.roundNumber.toPersianDigits()
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            x,
                            height - 10f,
                            textPaint
                        )
                    }
                }
            }

            // Bottom Hint
            Text(
                text = stringResource(R.string.tap_point_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
