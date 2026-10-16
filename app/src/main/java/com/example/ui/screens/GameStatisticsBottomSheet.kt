package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameEntity
import com.example.data.RoundEntity
import com.example.ui.components.ScoreProgressionLineChart
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PersianRed
import com.example.util.PersianNumberHelper.formatScore
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameStatisticsBottomSheet(
    game: GameEntity,
    rounds: List<RoundEntity>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val completedRounds = rounds.filter { it.isCompleted && it.status != "Ongoing" }
    val totalCompletedRounds = completedRounds.size

    // 1. Highest score in a single round
    val team1HighestRound = completedRounds.maxByOrNull { it.team1FinalScore }
    val team2HighestRound = completedRounds.maxByOrNull { it.team2FinalScore }

    val team1BestScore = team1HighestRound?.team1FinalScore ?: 0
    val team2BestScore = team2HighestRound?.team2FinalScore ?: 0

    val overallHighestScore: Int
    val highestScoreTeamName: String
    val highestScoreRoundNumber: Int

    if (team1BestScore >= team2BestScore && team1HighestRound != null) {
        overallHighestScore = team1BestScore
        highestScoreTeamName = game.team1Name
        highestScoreRoundNumber = team1HighestRound.roundNumber
    } else if (team2HighestRound != null) {
        overallHighestScore = team2BestScore
        highestScoreTeamName = game.team2Name
        highestScoreRoundNumber = team2HighestRound.roundNumber
    } else {
        overallHighestScore = 0
        highestScoreTeamName = ""
        highestScoreRoundNumber = 0
    }

    // 2. Win / Loss Distribution
    val team1Wins = completedRounds.count { it.team1FinalScore > it.team2FinalScore }
    val team2Wins = completedRounds.count { it.team2FinalScore > it.team1FinalScore }
    val tiedRounds = completedRounds.count { it.team1FinalScore == it.team2FinalScore }

    val team1WinRate = if (totalCompletedRounds > 0) ((team1Wins.toFloat() / totalCompletedRounds) * 100).roundToInt() else 0
    val team2WinRate = if (totalCompletedRounds > 0) ((team2Wins.toFloat() / totalCompletedRounds) * 100).roundToInt() else 0

    // 3. Hakem / Bidding performance
    val team1HakemRounds = completedRounds.filter { it.hakemTeam == 1 }
    val team1SuccessfulBids = team1HakemRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
    val team1FailedBids = team1HakemRounds.count { it.status == "BidFailed" || it.status == "NegativeShelem" }

    val team2HakemRounds = completedRounds.filter { it.hakemTeam == 2 }
    val team2SuccessfulBids = team2HakemRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
    val team2FailedBids = team2HakemRounds.count { it.status == "BidFailed" || it.status == "NegativeShelem" }

    // 4. Averages and Specials
    val team1TotalScore = completedRounds.sumOf { it.team1FinalScore }
    val team2TotalScore = completedRounds.sumOf { it.team2FinalScore }
    val team1AvgScore = if (totalCompletedRounds > 0) (team1TotalScore.toDouble() / totalCompletedRounds).roundToInt() else 0
    val team2AvgScore = if (totalCompletedRounds > 0) (team2TotalScore.toDouble() / totalCompletedRounds).roundToInt() else 0

    val shelemsCount = completedRounds.count { it.status == "Shelem" || it.status == "NegativeShelem" }
    val narasCount = completedRounds.count { it.isNaras }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.game_statistics_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.game_statistics_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_game_statistics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            HorizontalDivider()

            if (totalCompletedRounds == 0) {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.highest_round_empty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.no_rounds_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Top KPI Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: Total Rounds
                    StatKpiCard(
                        title = stringResource(R.string.total_rounds_played),
                        value = totalCompletedRounds.toPersianDigits(),
                        subtitle = stringResource(R.string.rounds_count_format, totalCompletedRounds.toPersianDigits()),
                        icon = Icons.Default.BarChart,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    // KPI 2: Highest Score in single round
                    StatKpiCard(
                        title = stringResource(R.string.highest_single_round_score),
                        value = formatScore(overallHighestScore),
                        subtitle = if (overallHighestScore > 0) {
                            stringResource(
                                R.string.highest_round_detail,
                                highestScoreRoundNumber.toPersianDigits(),
                                highestScoreTeamName,
                                overallHighestScore.toPersianDigits()
                            )
                        } else {
                            stringResource(R.string.highest_round_empty)
                        },
                        icon = Icons.Default.EmojiEvents,
                        accentColor = GoldAccent,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // Cumulative Score Progression Line Chart
                ScoreProgressionLineChart(
                    game = game,
                    rounds = completedRounds
                )

                // Section 1: Win / Loss Distribution
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stat_win_loss_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.win_loss_distribution),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Split Distribution Bar
                        val t1Ratio = if (totalCompletedRounds > 0) team1Wins.toFloat() / totalCompletedRounds else 0.5f
                        val t2Ratio = if (totalCompletedRounds > 0) team2Wins.toFloat() / totalCompletedRounds else 0.5f

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (team1Wins > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(if (t1Ratio > 0f) t1Ratio else 0.001f)
                                            .fillMaxWidth()
                                            .background(EmeraldGreen)
                                    )
                                }
                                if (tiedRounds > 0) {
                                    val tieRatio = tiedRounds.toFloat() / totalCompletedRounds
                                    Box(
                                        modifier = Modifier
                                            .weight(if (tieRatio > 0f) tieRatio else 0.001f)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                    )
                                }
                                if (team2Wins > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(if (t2Ratio > 0f) t2Ratio else 0.001f)
                                            .fillMaxWidth()
                                            .background(GoldAccent)
                                    )
                                }
                            }

                            // Legends & Exact Numbers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Team 1
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
                                        text = stringResource(
                                            R.string.win_rate_format,
                                            stringResource(R.string.team1_wins_label, game.team1Name),
                                            team1WinRate.toPersianDigits()
                                        ) + " (${team1Wins.toPersianDigits()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Team 2
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
                                        text = stringResource(
                                            R.string.win_rate_format,
                                            stringResource(R.string.team2_wins_label, game.team2Name),
                                            team2WinRate.toPersianDigits()
                                        ) + " (${team2Wins.toPersianDigits()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (tiedRounds > 0) {
                                Text(
                                    text = "${stringResource(R.string.ties_label)}: ${tiedRounds.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section 2: Highest Round Comparison for Both Teams
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.highest_single_round_score),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Team 1 Best
                            TeamBestRoundItem(
                                teamName = game.team1Name,
                                roundNumber = team1HighestRound?.roundNumber ?: 0,
                                score = team1BestScore,
                                status = team1HighestRound?.status,
                                color = EmeraldGreen,
                                modifier = Modifier.weight(1f)
                            )

                            // Team 2 Best
                            TeamBestRoundItem(
                                teamName = game.team2Name,
                                roundNumber = team2HighestRound?.roundNumber ?: 0,
                                score = team2BestScore,
                                status = team2HighestRound?.status,
                                color = GoldAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section 3: Bidding & Hakemi Performance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.bidding_performance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Team 1 Hakem Stats
                            HakemPerformanceItem(
                                teamName = game.team1Name,
                                totalBids = team1HakemRounds.size,
                                successfulBids = team1SuccessfulBids,
                                failedBids = team1FailedBids,
                                modifier = Modifier.weight(1f)
                            )

                            // Team 2 Hakem Stats
                            HakemPerformanceItem(
                                teamName = game.team2Name,
                                totalBids = team2HakemRounds.size,
                                successfulBids = team2SuccessfulBids,
                                failedBids = team2FailedBids,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section 4: Averages & Special Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Average per Round
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.average_score_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${game.team1Name}: ${formatScore(team1AvgScore)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${game.team2Name}: ${formatScore(team2AvgScore)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Special Events (Shelem & Naras)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.special_events_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.shelems_count, shelemsCount.toPersianDigits()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (shelemsCount > 0) GoldAccent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.naras_count, narasCount.toPersianDigits()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_statistics_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.close),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TeamBestRoundItem(
    teamName: String,
    roundNumber: Int,
    score: Int,
    status: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = formatScore(score),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (score > 0) EmeraldGreen else if (score < 0) PersianRed else MaterialTheme.colorScheme.onSurface
            )
            if (roundNumber > 0) {
                Text(
                    text = stringResource(R.string.round_number, roundNumber.toPersianDigits()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HakemPerformanceItem(
    teamName: String,
    totalBids: Int,
    successfulBids: Int,
    failedBids: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
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
            val successRate = if (totalBids > 0) ((successfulBids.toFloat() / totalBids) * 100).roundToInt() else 0
            Text(
                text = "${stringResource(R.string.bid_prefix)}${totalBids.toPersianDigits()} (${successRate.toPersianDigits()}%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
