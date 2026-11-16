package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameEntity
import com.example.domain.ComprehensiveGameStats
import com.example.domain.PlayerStat
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PersianRed
import com.example.util.PersianNumberHelper.formatScore
import com.example.util.PersianNumberHelper.toPersianDigits

/**
 * Individual Player Hakem Leaderboard, Clickable Net Hakem Score Guide, MVP Banner & Horizontal Bar Chart
 */
@Composable
fun PlayerPerformanceLeaderboardCard(
    stats: ComprehensiveGameStats,
    modifier: Modifier = Modifier
) {
    var showHakemGuide by remember { mutableStateOf(false) }
    val maxHakemTurns = (stats.playerStats.maxOfOrNull { it.hakemCount } ?: 1).coerceAtLeast(1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("player_performance_card"),
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
            // Section Title + Clickable Guide Toggle Pill
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
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.player_stats_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.player_stats_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Subtle Interactive Guide Toggle Pill (Hidden by default, shown on click)
                Surface(
                    color = if (showHakemGuide) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = if (showHakemGuide) 0.45f else 0.2f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showHakemGuide = !showHakemGuide }
                        .testTag("player_hakem_guide_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.guide_toggle_label),
                            tint = if (showHakemGuide) {
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
                            color = if (showHakemGuide) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Collapsible Net Hakem Score Guide Box
            AnimatedVisibility(
                visible = showHakemGuide,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_hakem_guide_content")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.player_hakem_guide_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )
                        Text(
                            text = stringResource(R.string.player_hakem_guide_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // MVP Hakem Spotlight Banner
            stats.mvpPlayer?.let { mvp ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GoldAccent.copy(alpha = 0.22f),
                                        EmeraldGreen.copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = GoldAccent,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFF0D231A),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.mvp_hakem_badge, mvp.playerName),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent
                                )
                                Text(
                                    text = stringResource(
                                        R.string.mvp_hakem_desc,
                                        mvp.hakemCount.toPersianDigits(),
                                        mvp.successRate.toPersianDigits(),
                                        formatScore(mvp.netHakemScore)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4 Players Horizontal Bar & Metric Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.playerStats.forEach { player ->
                    PlayerStatRowItem(
                        player = player,
                        maxHakemTurns = maxHakemTurns,
                        isMvp = stats.mvpPlayer?.playerName == player.playerName && player.hakemCount > 0
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerStatRowItem(
    player: PlayerStat,
    maxHakemTurns: Int,
    isMvp: Boolean
) {
    val teamColor = if (player.teamNumber == 1) EmeraldGreen else GoldAccent

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isMvp) 1.5.dp else 1.dp,
            color = if (isMvp) GoldAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            .background(teamColor, CircleShape)
                    )
                    Text(
                        text = if (isMvp) "👑 ${player.playerName}" else player.playerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = teamColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = player.teamName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = teamColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (player.hakemCount > 0) {
                    Text(
                        text = stringResource(R.string.player_net_hakem_score, formatScore(player.netHakemScore)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (player.netHakemScore >= 0) EmeraldGreen else PersianRed
                    )
                } else {
                    Text(
                        text = stringResource(R.string.player_no_hakem_yet),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Horizontal Bar representing Hakem count & Success vs Fail split
            if (player.hakemCount > 0) {
                val widthFraction = (player.hakemCount.toFloat() / maxHakemTurns).coerceIn(0.15f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(widthFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        if (player.successfulBids > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(player.successfulBids.toFloat())
                                    .fillMaxHeight()
                                    .background(EmeraldGreen)
                            )
                        }
                        if (player.failedBids > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(player.failedBids.toFloat())
                                    .fillMaxHeight()
                                    .background(PersianRed)
                            )
                        }
                    }
                }

                // Detailed 2x2 Metric Grid (clean spacing across Persian & English locales)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerMetricPill(
                            text = stringResource(R.string.player_hakem_count, player.hakemCount.toPersianDigits()),
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        PlayerMetricPill(
                            text = stringResource(R.string.player_success_rate, player.successRate.toPersianDigits()),
                            textColor = if (player.successRate >= 50) EmeraldGreen else PersianRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerMetricPill(
                            text = stringResource(R.string.player_avg_bid, player.avgBid.toPersianDigits()),
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        PlayerMetricPill(
                            text = stringResource(
                                R.string.player_max_bid,
                                if (player.maxBidMade > 0) player.maxBidMade.toPersianDigits() else "—"
                            ),
                            textColor = GoldAccent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerMetricPill(
    text: String,
    textColor: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = fontWeight,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Match Records, Highest Single-Round Comparison & Special Highlights Card
 */
@Composable
fun MatchRecordsAndHighlightsCard(
    game: GameEntity,
    stats: ComprehensiveGameStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_records_card"),
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
                    color = GoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.records_highlights_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.records_highlights_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Highest Single Round Score per Team
            Text(
                text = stringResource(R.string.highest_single_round_score),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeamBestRoundBox(
                    teamName = game.team1Name,
                    roundNumber = stats.team1BestRoundNumber,
                    score = stats.team1BestScore,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                TeamBestRoundBox(
                    teamName = game.team2Name,
                    roundNumber = stats.team2BestRoundNumber,
                    score = stats.team2BestScore,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Curated Match Records List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Record 1: Biggest Fall / Penalty
                RecordRowItem(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    iconTint = PersianRed,
                    title = stringResource(R.string.record_biggest_fall_title),
                    value = if (stats.biggestFallScore < 0) {
                        stringResource(
                            R.string.record_biggest_fall_val,
                            stats.biggestFallRoundNumber.toPersianDigits(),
                            stats.biggestFallTeamName,
                            formatScore(stats.biggestFallScore)
                        )
                    } else {
                        stringResource(R.string.record_biggest_fall_none)
                    }
                )

                // Record 2: Most Precise / Clutch Bid
                RecordRowItem(
                    icon = Icons.Default.GpsFixed,
                    iconTint = EmeraldGreen,
                    title = stringResource(R.string.record_clutch_bid_title),
                    value = if (stats.clutchBidRoundNumber > 0) {
                        stringResource(
                            R.string.record_clutch_bid_val,
                            stats.clutchBidRoundNumber.toPersianDigits(),
                            stats.clutchBidPlayerName,
                            stats.clutchBidAmount.toPersianDigits(),
                            stats.clutchCollectedPoints.toPersianDigits()
                        )
                    } else {
                        stringResource(R.string.record_clutch_bid_none)
                    }
                )

                // Record 3: Largest Score Lead
                if (stats.maxLeadDiff > 0) {
                    RecordRowItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = GoldAccent,
                        title = stringResource(R.string.record_max_lead_title),
                        value = stringResource(
                            R.string.record_max_lead_val,
                            stats.maxLeadDiff.toPersianDigits(),
                            stats.maxLeadTeamName,
                            stats.maxLeadRoundNumber.toPersianDigits()
                        )
                    )
                }

                // Record 4: Longest Win Streak
                if (stats.bestStreakCount > 0) {
                    RecordRowItem(
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = Color(0xFFFF6D00),
                        title = stringResource(R.string.stat_kpi_win_streak),
                        value = "${stats.bestStreakTeamName}: ${
                            stringResource(
                                R.string.stat_kpi_streak_value,
                                stats.bestStreakCount.toPersianDigits()
                            )
                        }"
                    )
                }

                // Record 5: Luckiest Dealer
                if (stats.luckyDealerName.isNotBlank() && stats.luckyDealerAvgScore > 0) {
                    RecordRowItem(
                        icon = Icons.Default.Casino,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.record_lucky_dealer_title),
                        value = stringResource(
                            R.string.record_lucky_dealer_val,
                            stats.luckyDealerName,
                            stats.luckyDealerAvgScore.toPersianDigits(),
                            stats.luckyDealerBeneficiaryTeam
                        )
                    )
                }

                // Record 6: Rule 1000 Blocked Hands
                if (game.winHandOnlyOver1000 && stats.rule1000BlockedHands > 0) {
                    RecordRowItem(
                        icon = Icons.Default.Shield,
                        iconTint = PersianRed,
                        title = stringResource(R.string.record_rule_1000_blocks_title),
                        value = stringResource(
                            R.string.record_rule_1000_blocks_val,
                            stats.rule1000BlockedHands.toPersianDigits()
                        )
                    )
                }
            }

            // Averages & Special Events Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.average_score_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${game.team1Name}: ${formatScore(stats.team1AvgScore)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = "${game.team2Name}: ${formatScore(stats.team2AvgScore)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.special_events_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.shelems_count, stats.shelemsCount.toPersianDigits()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stats.shelemsCount > 0) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.naras_count, stats.narasCount.toPersianDigits()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamBestRoundBox(
    teamName: String,
    roundNumber: Int,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatScore(score),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
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
private fun RecordRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = iconTint.copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
