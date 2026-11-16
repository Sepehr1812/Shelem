package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.GameEntity
import com.example.data.RoundEntity
import com.example.domain.ComprehensiveGameStats
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PersianRed
import com.example.util.PersianNumberHelper.formatScore
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val AmberWarning = Color(0xFFFF8F00)

/**
 * 1. Round-by-Round Diverging Vertical Bar Chart
 * Shows positive and negative scores per round for both teams, with interactive round selection.
 */
@Composable
fun RoundByRoundBarChart(
    game: GameEntity,
    rounds: List<RoundEntity>,
    modifier: Modifier = Modifier
) {
    val completedRounds = remember(rounds) {
        rounds.filter { it.isCompleted && it.status != "Ongoing" }
            .sortedBy { it.roundNumber }
    }
    if (completedRounds.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedRound = selectedIndex?.let { completedRounds.getOrNull(it) }

    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animTriggered = true }
    val animProgress by animateFloatAsState(
        targetValue = if (animTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "barChartAnim"
    )

    val context = LocalContext.current
    val vazirTypeface = remember(context) {
        runCatching { ResourcesCompat.getFont(context, R.font.vazir_regular) }.getOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("round_by_round_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.round_bar_chart_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.round_bar_chart_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = game.team1Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
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
                        text = game.team2Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PersianRed, CircleShape)
                    )
                    Text(
                        text = stringResource(R.string.outcome_failed_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = PersianRed
                    )
                }
            }

            // Interactive Selected Round Detail Card
            AnimatedVisibility(
                visible = selectedRound != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedRound != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = 4.dp,
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
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
                                    text = stringResource(
                                        R.string.chart_round_label,
                                        selectedRound.roundNumber.toPersianDigits()
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "👑 ${selectedRound.hakemPlayerName} (${stringResource(R.string.bid_label_with_value, selectedRound.bidAmount.toPersianDigits())})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${game.team1Name}: ${formatScore(selectedRound.team1FinalScore)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRound.team1FinalScore < 0) PersianRed else EmeraldGreen
                                )
                                Text(
                                    text = "${game.team2Name}: ${formatScore(selectedRound.team2FinalScore)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRound.team2FinalScore < 0) PersianRed else GoldAccent
                                )
                            }
                            if (selectedRound.team1CollectedRaw + selectedRound.team2CollectedRaw > 0) {
                                Text(
                                    text = stringResource(
                                        R.string.round_bar_raw_points,
                                        game.team1Name,
                                        selectedRound.team1CollectedRaw.toPersianDigits(),
                                        game.team2Name,
                                        selectedRound.team2CollectedRaw.toPersianDigits()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant
            val highlightBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

            val textPaint = remember(textColor, vazirTypeface) {
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (textColor.alpha * 255).toInt(),
                        (textColor.red * 255).toInt(),
                        (textColor.green * 255).toInt(),
                        (textColor.blue * 255).toInt()
                    )
                    textSize = 26f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    vazirTypeface?.let { typeface = it }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .pointerInput(completedRounds) {
                        detectTapGestures { offset ->
                            val paddingLeft = 68f
                            val paddingRight = 20f
                            val chartWidth = size.width - paddingLeft - paddingRight
                            if (completedRounds.isNotEmpty() && chartWidth > 0) {
                                val slotWidth = chartWidth / completedRounds.size
                                val idx = ((offset.x - paddingLeft) / slotWidth)
                                    .toInt()
                                    .coerceIn(0, completedRounds.lastIndex)
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 68f
                    val paddingRight = 20f
                    val paddingTop = 24f
                    val paddingBottom = 38f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    var maxScore = 165
                    var minScore = 0
                    completedRounds.forEach { r ->
                        maxScore = max(maxScore, max(r.team1FinalScore, r.team2FinalScore))
                        minScore = min(minScore, min(r.team1FinalScore, r.team2FinalScore))
                    }
                    val yMax = ((maxScore + 35) / 50) * 50
                    val yMin = if (minScore < 0) ((minScore - 35) / 50) * 50 else 0
                    val totalSpan = max(165, yMax - yMin)

                    fun getY(score: Int): Float {
                        val fraction = (score - yMin).toFloat() / totalSpan.toFloat()
                        return paddingTop + chartHeight * (1f - fraction)
                    }

                    val zeroY = getY(0)

                    // Grid lines
                    val gridValues = buildList {
                        add(yMax)
                        if (yMax > 100) add(yMax / 2)
                        add(0)
                        if (yMin < 0) add(yMin)
                    }.distinct()

                    gridValues.forEach { gv ->
                        val gy = getY(gv)
                        drawLine(
                            color = if (gv == 0) onSurfaceColor.copy(alpha = 0.45f) else outlineColor,
                            start = Offset(paddingLeft, gy),
                            end = Offset(width - paddingRight, gy),
                            strokeWidth = if (gv == 0) 1.5.dp.toPx() else 1.dp.toPx(),
                            pathEffect = if (gv == 0) null else PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            formatScore(gv),
                            paddingLeft - 32f,
                            gy + 9f,
                            textPaint
                        )
                    }

                    val count = completedRounds.size
                    val slotWidth = chartWidth / count.coerceAtLeast(1)
                    val barWidth = (slotWidth * 0.34f).coerceIn(6f, 28f)
                    val gap = (slotWidth * 0.06f).coerceIn(2f, 6f)

                    completedRounds.forEachIndexed { idx, round ->
                        val slotStart = paddingLeft + idx * slotWidth
                        val centerX = slotStart + slotWidth / 2f

                        if (selectedIndex == idx) {
                            drawRoundRect(
                                color = highlightBg,
                                topLeft = Offset(slotStart + 2f, paddingTop - 6f),
                                size = Size(slotWidth - 4f, chartHeight + 12f),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }

                        // Team 1 Bar
                        val t1Score = (round.team1FinalScore * animProgress).toInt()
                        val y1 = getY(t1Score)
                        val t1Top = min(zeroY, y1)
                        val t1Height = abs(y1 - zeroY).coerceAtLeast(3f)
                        val t1Color = if (round.team1FinalScore < 0) PersianRed else EmeraldGreen

                        drawRoundRect(
                            color = t1Color,
                            topLeft = Offset(centerX - barWidth - gap / 2f, t1Top),
                            size = Size(barWidth, t1Height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Team 2 Bar
                        val t2Score = (round.team2FinalScore * animProgress).toInt()
                        val y2 = getY(t2Score)
                        val t2Top = min(zeroY, y2)
                        val t2Height = abs(y2 - zeroY).coerceAtLeast(3f)
                        val t2Color = if (round.team2FinalScore < 0) PersianRed else GoldAccent

                        drawRoundRect(
                            color = t2Color,
                            topLeft = Offset(centerX + gap / 2f, t2Top),
                            size = Size(barWidth, t2Height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Round label on X-axis (skip some if > 14 rounds to prevent overlap)
                        val step = if (count > 14) 2 else 1
                        if (idx % step == 0 || idx == count - 1) {
                            drawContext.canvas.nativeCanvas.drawText(
                                round.roundNumber.toPersianDigits(),
                                centerX,
                                height - 8f,
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Interactive Donut / Pie Chart Card for Contract Outcomes & Team Win Distribution
 */
@Composable
fun OutcomesDonutChartCard(
    game: GameEntity,
    stats: ComprehensiveGameStats,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Outcomes, 1 = Team Wins

    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(selectedMode) {
        animTriggered = false
        animTriggered = true
    }
    val sweepProgress by animateFloatAsState(
        targetValue = if (animTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "donutSweep"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stat_win_loss_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DonutLarge,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.outcomes_donut_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.outcomes_donut_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Segmented Toggle Pills
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        stringResource(R.string.donut_mode_outcomes),
                        stringResource(R.string.donut_mode_team_wins)
                    )
                    modes.forEachIndexed { index, title ->
                        val isSelected = selectedMode == index
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { selectedMode = index }
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            val total = stats.totalCompletedRounds.coerceAtLeast(1)

            val slices = if (selectedMode == 0) {
                listOf(
                    DonutSlice(
                        label = stringResource(R.string.outcome_accomplished_short),
                        count = stats.accomplishedCount,
                        color = EmeraldGreen
                    ),
                    DonutSlice(
                        label = stringResource(R.string.outcome_shelem_short),
                        count = stats.positiveShelemCount,
                        color = GoldAccent
                    ),
                    DonutSlice(
                        label = stringResource(R.string.outcome_failed_short),
                        count = stats.failedBidCount,
                        color = AmberWarning
                    ),
                    DonutSlice(
                        label = stringResource(R.string.outcome_neg_shelem_short),
                        count = stats.negativeShelemCount,
                        color = PersianRed
                    )
                )
            } else {
                listOf(
                    DonutSlice(
                        label = stringResource(R.string.team1_wins_label, game.team1Name),
                        count = stats.team1Wins,
                        color = EmeraldGreen
                    ),
                    DonutSlice(
                        label = stringResource(R.string.team2_wins_label, game.team2Name),
                        count = stats.team2Wins,
                        color = GoldAccent
                    )
                )
            }

            // Centered Donut Chart + Spacious Grid Legend Below (Prevents any truncation or text wrapping)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = MaterialTheme.colorScheme.surface
                    Canvas(modifier = Modifier.size(140.dp)) {
                        val strokeWidth = 22.dp.toPx()
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        var currentStartAngle = -90f
                        slices.filter { it.count > 0 }.forEach { slice ->
                            val sweep = (slice.count.toFloat() / total.toFloat()) * 360f * sweepProgress
                            drawArc(
                                color = slice.color,
                                startAngle = currentStartAngle,
                                sweepAngle = max(sweep - 2f, 1f),
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            currentStartAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedMode == 0) {
                                "${stats.overallHakemSuccessRate.toPersianDigits()}%"
                            } else {
                                stats.totalCompletedRounds.toPersianDigits()
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (selectedMode == 0) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedMode == 0) {
                                stringResource(R.string.donut_center_success)
                            } else {
                                stringResource(R.string.donut_center_rounds)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2-Column Grid Legend for 4 outcomes, or full-width rows for Team Win Share
                if (selectedMode == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slices.chunked(2).forEach { rowSlices ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSlices.forEach { slice ->
                                    DonutLegendCard(
                                        slice = slice,
                                        total = total,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slices.forEach { slice ->
                            DonutLegendCard(
                                slice = slice,
                                total = total,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutLegendCard(
    slice: DonutSlice,
    total: Int,
    modifier: Modifier = Modifier
) {
    val pct = ((slice.count.toFloat() / total) * 100).toInt()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, slice.color.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(slice.color, CircleShape)
                )
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${slice.count.toPersianDigits()} (${pct.toPersianDigits()}%)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = slice.color,
                softWrap = false
            )
        }
    }
}

private data class DonutSlice(
    val label: String,
    val count: Int,
    val color: Color
)

/**
 * 3. Multi-Dimensional 5-Axis Radar / Spider Chart + Clickable Guide + Raw Card Points Comparison
 */
@Composable
fun TeamComparisonRadarCard(
    game: GameEntity,
    stats: ComprehensiveGameStats,
    modifier: Modifier = Modifier
) {
    var showRadarGuide by remember { mutableStateOf(false) }

    val axisLabels = listOf(
        stringResource(R.string.radar_axis_win_rate),
        stringResource(R.string.radar_axis_bid_success),
        stringResource(R.string.radar_axis_raw_points),
        stringResource(R.string.radar_axis_aggression),
        stringResource(R.string.radar_axis_efficiency)
    )

    val axisGuideDescriptions = listOf(
        stringResource(R.string.radar_axis_win_rate) to stringResource(R.string.radar_guide_win_rate_desc),
        stringResource(R.string.radar_axis_bid_success) to stringResource(R.string.radar_guide_bid_success_desc),
        stringResource(R.string.radar_axis_raw_points) to stringResource(R.string.radar_guide_raw_points_desc),
        stringResource(R.string.radar_axis_aggression) to stringResource(R.string.radar_guide_aggression_desc),
        stringResource(R.string.radar_axis_efficiency) to stringResource(R.string.radar_guide_efficiency_desc)
    )

    val t1Values = listOf(
        stats.team1Radar.winRate,
        stats.team1Radar.bidSuccess,
        stats.team1Radar.rawPointsShare,
        stats.team1Radar.bidAggression,
        stats.team1Radar.scoreEfficiency
    )
    val t2Values = listOf(
        stats.team2Radar.winRate,
        stats.team2Radar.bidSuccess,
        stats.team2Radar.rawPointsShare,
        stats.team2Radar.bidAggression,
        stats.team2Radar.scoreEfficiency
    )

    val context = LocalContext.current
    val vazirTypeface = remember(context) {
        runCatching { ResourcesCompat.getFont(context, R.font.vazir_regular) }.getOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("team_radar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.radar_chart_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.radar_chart_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Subtle Interactive Guide Toggle Pill (Hidden by default, shown on click)
                Surface(
                    color = if (showRadarGuide) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = if (showRadarGuide) 0.45f else 0.2f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showRadarGuide = !showRadarGuide }
                        .testTag("radar_guide_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.guide_toggle_label),
                            tint = if (showRadarGuide) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.guide_toggle_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (showRadarGuide) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Collapsible Radar Metrics Guide Box
            AnimatedVisibility(
                visible = showRadarGuide,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("radar_guide_content")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.radar_guide_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        axisGuideDescriptions.forEach { (axisTitle, axisDesc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .background(GoldAccent, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = axisTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = axisDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Team Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
                        text = game.team1Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
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
                        text = game.team2Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            val labelColor = MaterialTheme.colorScheme.onSurface
            val labelPaint = remember(labelColor, vazirTypeface) {
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue * 255).toInt()
                    )
                    textSize = 27f
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    vazirTypeface?.let { typeface = it }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            ) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f + 6f
                val maxRadius = min(size.width, size.height) * 0.34f
                val axes = 5

                fun pointOnAxis(axisIndex: Int, fraction: Float): Offset {
                    val angleRad = (-PI / 2.0) + (2.0 * PI * axisIndex / axes)
                    val r = maxRadius * fraction
                    return Offset(
                        x = centerX + (r * cos(angleRad)).toFloat(),
                        y = centerY + (r * sin(angleRad)).toFloat()
                    )
                }

                // Draw Concentric Pentagons (0.25, 0.5, 0.75, 1.0)
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { level ->
                    val ringPath = Path()
                    for (i in 0 until axes) {
                        val pt = pointOnAxis(i, level)
                        if (i == 0) ringPath.moveTo(pt.x, pt.y) else ringPath.lineTo(pt.x, pt.y)
                    }
                    ringPath.close()
                    drawPath(
                        path = ringPath,
                        color = gridColor,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Draw Spokes & Labels
                for (i in 0 until axes) {
                    val outerPt = pointOnAxis(i, 1.0f)
                    drawLine(
                        color = gridColor,
                        start = Offset(centerX, centerY),
                        end = outerPt,
                        strokeWidth = 1.dp.toPx()
                    )

                    val labelPt = pointOnAxis(i, 1.29f)
                    drawContext.canvas.nativeCanvas.drawText(
                        axisLabels[i],
                        labelPt.x,
                        labelPt.y + 10f,
                        labelPaint
                    )
                }

                // Team 1 Polygon
                val t1Path = Path()
                for (i in 0 until axes) {
                    val pt = pointOnAxis(i, t1Values[i])
                    if (i == 0) t1Path.moveTo(pt.x, pt.y) else t1Path.lineTo(pt.x, pt.y)
                }
                t1Path.close()
                drawPath(
                    path = t1Path,
                    color = EmeraldGreen.copy(alpha = 0.28f)
                )
                drawPath(
                    path = t1Path,
                    color = EmeraldGreen,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Team 2 Polygon
                val t2Path = Path()
                for (i in 0 until axes) {
                    val pt = pointOnAxis(i, t2Values[i])
                    if (i == 0) t2Path.moveTo(pt.x, pt.y) else t2Path.lineTo(pt.x, pt.y)
                }
                t2Path.close()
                drawPath(
                    path = t2Path,
                    color = GoldAccent.copy(alpha = 0.25f)
                )
                drawPath(
                    path = t2Path,
                    color = GoldAccent,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Draw Vertices
                for (i in 0 until axes) {
                    val pt1 = pointOnAxis(i, t1Values[i])
                    val pt2 = pointOnAxis(i, t2Values[i])
                    drawCircle(color = EmeraldGreen, radius = 4.dp.toPx(), center = pt1)
                    drawCircle(color = GoldAccent, radius = 4.dp.toPx(), center = pt2)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Raw Card Points Comparison Section (Separated into two distinct cards so they never touch!)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.raw_points_comparison_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val w1 = stats.team1RawSharePercent.coerceAtLeast(5).toFloat()
                    val w2 = stats.team2RawSharePercent.coerceAtLeast(5).toFloat()
                    Box(
                        modifier = Modifier
                            .weight(w1)
                            .fillMaxHeight()
                            .background(EmeraldGreen)
                    )
                    Box(
                        modifier = Modifier
                            .weight(w2)
                            .fillMaxHeight()
                            .background(GoldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = game.team1Name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(
                                    R.string.raw_points_count_only,
                                    stats.team1TotalRawPoints.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.raw_points_percent_only,
                                    stats.team1RawSharePercent.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.25f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = game.team2Name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(
                                    R.string.raw_points_count_only,
                                    stats.team2TotalRawPoints.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.raw_points_percent_only,
                                    stats.team2RawSharePercent.toPersianDigits()
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

/**
 * 4. Bid Distribution Histogram & Detailed Team Bidding Performance Card
 */
@Composable
fun BidDistributionHistogramCard(
    game: GameEntity,
    stats: ComprehensiveGameStats,
    modifier: Modifier = Modifier
) {
    val maxExactBidCount = (stats.exactBidStats.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bid_distribution_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.bid_distribution_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.bid_distribution_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Exact Non-Zero Bid Amounts Vertical Bar Chart with Horizontal Scroll, Edge Arrows & Scrollbar
            if (stats.exactBidStats.isNotEmpty()) {
                val scrollState = rememberScrollState()
                val coroutineScope = rememberCoroutineScope()
                val density = LocalDensity.current
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val isScrollable = scrollState.maxValue > 0

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val horizontalPad = 12.dp
                            val viewportMaxWidth = (maxWidth - horizontalPad * 2).coerceAtLeast(160.dp)
                            val itemCount = stats.exactBidStats.size.coerceAtLeast(1)
                            val spacing = 10.dp
                            val minColumnWidth = 76.dp
                            val columnWidth = ((viewportMaxWidth - spacing * (itemCount - 1)) / itemCount)
                                .coerceAtLeast(minColumnWidth)
                            val scrollStepPx = with(density) { (columnWidth + spacing).toPx().toInt() }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(scrollState)
                                        .padding(horizontal = horizontalPad),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        spacing,
                                        Alignment.CenterHorizontally
                                    ),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    stats.exactBidStats.forEach { exactBid ->
                                        Column(
                                            modifier = Modifier.width(columnWidth),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = exactBid.totalCount.toPersianDigits(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )

                                            // Bar container
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(96.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                val barFraction = (exactBid.totalCount.toFloat() / maxExactBidCount).coerceIn(0.18f, 1f)
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(barFraction)
                                                        .clip(RoundedCornerShape(8.dp))
                                                ) {
                                                    if (exactBid.failedCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .weight(exactBid.failedCount.toFloat())
                                                                .background(PersianRed)
                                                        )
                                                    }
                                                    if (exactBid.accomplishedCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .weight(exactBid.accomplishedCount.toFloat())
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(EmeraldGreen, EmeraldGreen.copy(alpha = 0.8f))
                                                                    )
                                                                )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = exactBid.bidAmount.toPersianDigits(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                softWrap = false
                                            )

                                            Surface(
                                                color = if (exactBid.successRate >= 50) {
                                                    EmeraldGreen.copy(alpha = 0.15f)
                                                } else {
                                                    PersianRed.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(
                                                        R.string.bid_bracket_success_label,
                                                        exactBid.successRate.toPersianDigits()
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (exactBid.successRate >= 50) {
                                                        EmeraldGreen
                                                    } else {
                                                        PersianRed
                                                    },
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Visible,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isScrollable) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = 4.dp)
                                            .size(30.dp)
                                            .alpha(if (scrollState.canScrollBackward) 0.95f else 0.35f)
                                            .clip(CircleShape)
                                            .clickable(enabled = scrollState.canScrollBackward) {
                                                coroutineScope.launch {
                                                    scrollState.animateScrollTo(
                                                        (scrollState.value - scrollStepPx).coerceAtLeast(0)
                                                    )
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = CircleShape,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 4.dp)
                                            .size(30.dp)
                                            .alpha(if (scrollState.canScrollForward) 0.95f else 0.35f)
                                            .clip(CircleShape)
                                            .clickable(enabled = scrollState.canScrollForward) {
                                                coroutineScope.launch {
                                                    scrollState.animateScrollTo(
                                                        (scrollState.value + scrollStepPx).coerceAtMost(scrollState.maxValue)
                                                    )
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Persistent Slim Scrollbar Track & Thumb when horizontally scrollable
                        if (isScrollable) {
                            val trackColor = MaterialTheme.colorScheme.surfaceVariant
                            val thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            val totalContentPx = (scrollState.viewportSize + scrollState.maxValue).coerceAtLeast(1)
                            val thumbFraction = (scrollState.viewportSize.toFloat() / totalContentPx.toFloat()).coerceIn(0.18f, 0.85f)
                            val scrollProgress = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(4.dp)
                            ) {
                                val corner = CornerRadius(size.height / 2f, size.height / 2f)
                                drawRoundRect(
                                    color = trackColor,
                                    cornerRadius = corner
                                )
                                val thumbWidth = size.width * thumbFraction
                                val maxOffset = (size.width - thumbWidth).coerceAtLeast(0f)
                                val thumbX = if (isRtl) {
                                    maxOffset * (1f - scrollProgress)
                                } else {
                                    maxOffset * scrollProgress
                                }
                                drawRoundRect(
                                    color = thumbColor,
                                    topLeft = Offset(thumbX, 0f),
                                    size = Size(thumbWidth, size.height),
                                    cornerRadius = corner
                                )
                            }
                        }
                    }
                }
            }

            // Team-by-Team Bidding Performance Comparison Cards
            Text(
                text = stringResource(R.string.bidding_performance),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeamBiddingDetailBox(
                    teamName = game.team1Name,
                    accentColor = EmeraldGreen,
                    totalBids = stats.team1HakemCount,
                    successfulBids = stats.team1SuccessfulBids,
                    failedBids = stats.team1FailedBids,
                    successRate = stats.team1HakemSuccessRate,
                    avgBid = stats.team1AvgBid,
                    maxBidMade = stats.team1MaxBidMade,
                    avgOvertrick = stats.team1AvgOvertrick,
                    modifier = Modifier.weight(1f)
                )
                TeamBiddingDetailBox(
                    teamName = game.team2Name,
                    accentColor = GoldAccent,
                    totalBids = stats.team2HakemCount,
                    successfulBids = stats.team2SuccessfulBids,
                    failedBids = stats.team2FailedBids,
                    successRate = stats.team2HakemSuccessRate,
                    avgBid = stats.team2AvgBid,
                    maxBidMade = stats.team2MaxBidMade,
                    avgOvertrick = stats.team2AvgOvertrick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamBiddingDetailBox(
    teamName: String,
    accentColor: Color,
    totalBids: Int,
    successfulBids: Int,
    failedBids: Int,
    successRate: Int,
    avgBid: Int,
    maxBidMade: Int,
    avgOvertrick: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${successRate.toPersianDigits()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${stringResource(R.string.successful_bids)}: ${successfulBids.toPersianDigits()}",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldGreen,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${stringResource(R.string.failed_bids)}: ${failedBids.toPersianDigits()}",
                style = MaterialTheme.typography.bodySmall,
                color = PersianRed,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            Text(
                text = stringResource(
                    R.string.avg_bid_label,
                    if (avgBid > 0) avgBid.toPersianDigits() else "—"
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.max_bid_made_label,
                    if (maxBidMade > 0) maxBidMade.toPersianDigits() else "—"
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (successfulBids > 0) {
                Text(
                    text = stringResource(
                        R.string.bid_accuracy_overtrick,
                        "+${avgOvertrick.toPersianDigits()}"
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
            Text(
                text = "${stringResource(R.string.bid_prefix)}${totalBids.toPersianDigits()} (${successRate.toPersianDigits()}%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
