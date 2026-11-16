package com.shelem.scoresheet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shelem.scoresheet.R
import com.shelem.scoresheet.data.GameEntity
import com.shelem.scoresheet.data.RoundEntity
import com.shelem.scoresheet.domain.ComprehensiveGameStats
import com.shelem.scoresheet.domain.GameStatisticsCalculator
import com.shelem.scoresheet.ui.components.BidDistributionHistogramCard
import com.shelem.scoresheet.ui.components.MatchRecordsAndHighlightsCard
import com.shelem.scoresheet.ui.components.OutcomesDonutChartCard
import com.shelem.scoresheet.ui.components.PlayerPerformanceLeaderboardCard
import com.shelem.scoresheet.ui.components.RoundByRoundBarChart
import com.shelem.scoresheet.ui.components.ScoreProgressionLineChart
import com.shelem.scoresheet.ui.components.TeamComparisonRadarCard
import com.shelem.scoresheet.ui.theme.EmeraldGreen
import com.shelem.scoresheet.ui.theme.FeltGreenDark
import com.shelem.scoresheet.ui.theme.FeltGreenPrimary
import com.shelem.scoresheet.ui.theme.GoldAccent
import com.shelem.scoresheet.ui.theme.PersianRed
import com.shelem.scoresheet.util.PersianNumberHelper.formatScore
import com.shelem.scoresheet.util.PersianNumberHelper.toPersianDigits
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameStatisticsBottomSheet(
    game: GameEntity,
    rounds: List<RoundEntity>,
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val completedRounds = remember(rounds) {
        rounds.filter { it.isCompleted && it.status != "Ongoing" }
    }
    val stats = remember(game, rounds) {
        GameStatisticsCalculator.calculate(game, rounds)
    }

    // 0 = All, 1 = Charts & Flow, 2 = Bidding & Hakem, 3 = Players MVP, 4 = Match Records
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.size(44.dp)
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
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.game_statistics_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExportCsv,
                        modifier = Modifier.testTag("header_export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(R.string.export_csv_action),
                            tint = GoldAccent
                        )
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
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            if (stats.totalCompletedRounds == 0) {
                // Empty State Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(52.dp)
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
                // 1. Hero Scoreboard & Target Progress Card
                StatisticsHeroBanner(
                    game = game,
                    stats = stats
                )

                // 2. 4-Card KPI Bento Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatKpiCard(
                            title = stringResource(R.string.total_rounds_played),
                            value = stats.totalCompletedRounds.toPersianDigits(),
                            subtitle = stringResource(
                                R.string.rounds_count_format,
                                stats.totalCompletedRounds.toPersianDigits()
                            ),
                            icon = Icons.Default.BarChart,
                            accentColor = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )

                        StatKpiCard(
                            title = stringResource(R.string.highest_single_round_score),
                            value = formatScore(stats.overallHighestScore),
                            subtitle = if (stats.overallHighestScore > 0) {
                                stringResource(
                                    R.string.highest_round_detail,
                                    stats.highestScoreRoundNumber.toPersianDigits(),
                                    stats.highestScoreTeamName
                                )
                            } else {
                                stringResource(R.string.highest_round_empty)
                            },
                            icon = Icons.Default.EmojiEvents,
                            accentColor = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatKpiCard(
                            title = stringResource(R.string.stat_kpi_hakem_rate),
                            value = "${stats.overallHakemSuccessRate.toPersianDigits()}%",
                            subtitle = "${stringResource(R.string.stat_kpi_avg_bid)}: ${stats.overallAvgBid.toPersianDigits()}",
                            icon = Icons.Default.Gavel,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        StatKpiCard(
                            title = stringResource(R.string.stat_kpi_win_streak),
                            value = stringResource(
                                R.string.stat_kpi_streak_value,
                                stats.bestStreakCount.toPersianDigits()
                            ),
                            subtitle = stats.bestStreakTeamName,
                            icon = Icons.Default.LocalFireDepartment,
                            accentColor = Color(0xFFFF6D00),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Category Filter Pills Row
                val showAceStats = game.trackAceInGround || stats.aceRoundsCount > 0
                val allLabel = stringResource(R.string.stat_tab_all)
                val chartsLabel = stringResource(R.string.stat_tab_charts)
                val biddingLabel = stringResource(R.string.stat_tab_bidding)
                val playersLabel = stringResource(R.string.stat_tab_players)
                val recordsLabel = stringResource(R.string.stat_tab_records)
                val aceLabel = stringResource(R.string.stat_tab_ace)

                val tabs = remember(showAceStats, allLabel, chartsLabel, biddingLabel, playersLabel, recordsLabel, aceLabel) {
                    buildList {
                        add(0 to allLabel)
                        add(1 to chartsLabel)
                        add(2 to biddingLabel)
                        add(3 to playersLabel)
                        add(4 to recordsLabel)
                        if (showAceStats) {
                            add(5 to aceLabel)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { (tabId, label) ->
                        val selected = selectedTab == tabId
                        Surface(
                            color = if (selected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = tabId }
                                .testTag("stat_tab_chip_$tabId")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) FeltGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // --- SECTION A: Cumulative Line Chart & Round-by-Round Bar Chart ---
                if (selectedTab == 0 || selectedTab == 1) {
                    ScoreProgressionLineChart(
                        game = game,
                        rounds = completedRounds
                    )

                    RoundByRoundBarChart(
                        game = game,
                        rounds = completedRounds
                    )
                }

                // --- SECTION B: Donut Chart (Contract Outcomes & Win Share) + Radar Chart ---
                if (selectedTab == 0 || selectedTab == 1 || selectedTab == 2) {
                    OutcomesDonutChartCard(
                        game = game,
                        stats = stats
                    )
                }

                if (selectedTab == 0 || selectedTab == 1) {
                    TeamComparisonRadarCard(
                        game = game,
                        stats = stats
                    )
                }

                // --- SECTION C: Bid Distribution Histogram & Hakem Performance ---
                if (selectedTab == 0 || selectedTab == 2) {
                    BidDistributionHistogramCard(
                        game = game,
                        stats = stats
                    )
                }

                // --- SECTION D: Individual Player Performance Leaderboard & MVP ---
                if (selectedTab == 0 || selectedTab == 3) {
                    PlayerPerformanceLeaderboardCard(
                        stats = stats
                    )
                }

                // --- SECTION E: Match Records & Highlights ---
                if (selectedTab == 0 || selectedTab == 4) {
                    MatchRecordsAndHighlightsCard(
                        game = game,
                        stats = stats
                    )
                }

                // --- SECTION F: Ace in Ground Dedicated Section ---
                if (showAceStats && (selectedTab == 0 || selectedTab == 5)) {
                    AceInGroundStatisticsCard(
                        game = game,
                        totalCompletedRounds = stats.totalCompletedRounds,
                        aceRoundsCount = stats.aceRoundsCount,
                        aceRoundsRatio = stats.aceRoundsRatio,
                        aceTeam1Wins = stats.aceTeam1Wins,
                        aceTeam2Wins = stats.aceTeam2Wins,
                        aceTeam1WinRate = stats.aceTeam1WinRate,
                        aceTeam2WinRate = stats.aceTeam2WinRate,
                        aceHakemAccomplished = stats.aceHakemAccomplished,
                        aceHakemSuccessRate = stats.aceHakemSuccessRate
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dismiss_statistics_button"),
                shape = RoundedCornerShape(14.dp)
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
private fun StatisticsHeroBanner(
    game: GameEntity,
    stats: ComprehensiveGameStats
) {
    val scoreDiff = abs(stats.team1TotalScore - stats.team2TotalScore)
    val leadingTeam = when {
        stats.team1TotalScore > stats.team2TotalScore -> game.team1Name
        stats.team2TotalScore > stats.team1TotalScore -> game.team2Name
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FeltGreenDark,
                            FeltGreenPrimary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.stat_hero_live_score,
                            stats.totalCompletedRounds.toPersianDigits()
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = GoldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (leadingTeam != null) {
                                stringResource(
                                    R.string.stat_hero_leading_team,
                                    leadingTeam,
                                    scoreDiff.toPersianDigits()
                                )
                            } else {
                                stringResource(R.string.stat_hero_tied_game)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Scores Comparison Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.team1Name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${game.team1Player1Name} • ${game.team1Player2Name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatScore(stats.team1TotalScore),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (stats.team1TotalScore < 0) Color(0xFFFF8A80) else Color(0xFF69F0AE)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = game.team2Name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${game.team2Player1Name} • ${game.team2Player2Name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatScore(stats.team2TotalScore),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (stats.team2TotalScore < 0) Color(0xFFFF8A80) else GoldAccent
                        )
                    }
                }

                // Target Score Progress Bars
                val t1Effective = maxOf(stats.team1TotalScore, stats.team1TotalScore - stats.team2TotalScore).coerceAtLeast(0)
                val t2Effective = maxOf(stats.team2TotalScore, stats.team2TotalScore - stats.team1TotalScore).coerceAtLeast(0)
                val p1 = (t1Effective.toFloat() / game.targetScore.coerceAtLeast(1)).coerceIn(0f, 1f)
                val p2 = (t2Effective.toFloat() / game.targetScore.coerceAtLeast(1)).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(p1 * 100).toInt().toPersianDigits()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF69F0AE)
                        )
                        Text(
                            text = stringResource(
                                R.string.stat_hero_target_progress,
                                game.targetScore.toPersianDigits()
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "${(p2 * 100).toInt().toPersianDigits()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { p1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF69F0AE),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                        LinearProgressIndicator(
                            progress = { p2 },
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GoldAccent,
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                    }
                }
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AceInGroundStatisticsCard(
    game: GameEntity,
    totalCompletedRounds: Int,
    aceRoundsCount: Int,
    aceRoundsRatio: Int,
    aceTeam1Wins: Int,
    aceTeam2Wins: Int,
    aceTeam1WinRate: Int,
    aceTeam2WinRate: Int,
    aceHakemAccomplished: Int,
    aceHakemSuccessRate: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stat_ace_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
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
                    color = GoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spade),
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.ace_statistics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.ace_statistics_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (aceRoundsCount == 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ace_no_rounds_recorded),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ace_rounds_total_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.rounds_count_format, aceRoundsCount.toPersianDigits()),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                            Text(
                                text = stringResource(
                                    R.string.ace_rounds_from_total_format,
                                    totalCompletedRounds.toPersianDigits(),
                                    aceRoundsRatio.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ace_hakem_win_rate_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${aceHakemSuccessRate.toPersianDigits()}٪",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                            Text(
                                text = stringResource(
                                    R.string.ace_hakem_wins_fraction,
                                    aceHakemAccomplished.toPersianDigits(),
                                    aceRoundsCount.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.ace_team_performance_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (aceTeam1Wins > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(aceTeam1Wins.toFloat())
                                    .fillMaxHeight()
                                    .background(EmeraldGreen)
                            )
                        }
                        if (aceTeam2Wins > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(aceTeam2Wins.toFloat())
                                    .fillMaxHeight()
                                    .background(GoldAccent)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Text(
                                text = stringResource(
                                    R.string.ace_team_wins_format,
                                    game.team1Name,
                                    aceTeam1Wins.toPersianDigits(),
                                    aceTeam1WinRate.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GoldAccent, CircleShape)
                            )
                            Text(
                                text = stringResource(
                                    R.string.ace_team_wins_format,
                                    game.team2Name,
                                    aceTeam2Wins.toPersianDigits(),
                                    aceTeam2WinRate.toPersianDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
