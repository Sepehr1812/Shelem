package com.example.domain

import com.example.data.GameEntity
import com.example.data.RoundEntity
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

data class ExactBidStat(
    val bidAmount: Int,
    val totalCount: Int,
    val accomplishedCount: Int,
    val failedCount: Int,
    val team1Count: Int,
    val team2Count: Int
) {
    val successRate: Int
        get() = if (totalCount > 0) ((accomplishedCount.toFloat() / totalCount) * 100).roundToInt() else 0
}

data class PlayerStat(
    val playerName: String,
    val teamNumber: Int,
    val teamName: String,
    val hakemCount: Int,
    val successfulBids: Int,
    val failedBids: Int,
    val successRate: Int,
    val avgBid: Int,
    val maxBidMade: Int,
    val netHakemScore: Int,
    val dealerCount: Int
)

data class RadarMetrics(
    val winRate: Float,        // 0f..1f
    val bidSuccess: Float,     // 0f..1f
    val rawPointsShare: Float, // 0f..1f
    val bidAggression: Float,  // 0f..1f
    val scoreEfficiency: Float // 0f..1f
)

data class ComprehensiveGameStats(
    val totalCompletedRounds: Int,
    val team1TotalScore: Int,
    val team2TotalScore: Int,
    val team1AvgScore: Int,
    val team2AvgScore: Int,
    val team1Wins: Int,
    val team2Wins: Int,
    val team1WinRate: Int,
    val team2WinRate: Int,
    val team1BestScore: Int,
    val team2BestScore: Int,
    val team1BestRoundNumber: Int,
    val team2BestRoundNumber: Int,
    val overallHighestScore: Int,
    val highestScoreTeamName: String,
    val highestScoreRoundNumber: Int,
    // Streaks
    val team1MaxStreak: Int,
    val team2MaxStreak: Int,
    val bestStreakCount: Int,
    val bestStreakTeamName: String,
    // Outcomes breakdown
    val accomplishedCount: Int,
    val positiveShelemCount: Int,
    val failedBidCount: Int,
    val negativeShelemCount: Int,
    val overallHakemSuccessRate: Int,
    val overallAvgBid: Int,
    val shelemsCount: Int,
    val narasCount: Int,
    // Team Hakem & Bidding
    val team1HakemCount: Int,
    val team1SuccessfulBids: Int,
    val team1FailedBids: Int,
    val team1HakemSuccessRate: Int,
    val team1AvgBid: Int,
    val team1MaxBidMade: Int,
    val team1AvgOvertrick: Int,
    val team2HakemCount: Int,
    val team2SuccessfulBids: Int,
    val team2FailedBids: Int,
    val team2HakemSuccessRate: Int,
    val team2AvgBid: Int,
    val team2MaxBidMade: Int,
    val team2AvgOvertrick: Int,
    // Raw points
    val team1TotalRawPoints: Int,
    val team2TotalRawPoints: Int,
    val team1RawSharePercent: Int,
    val team2RawSharePercent: Int,
    // Exact bid distribution (only bids that were actually placed)
    val exactBidStats: List<ExactBidStat>,
    // Player stats
    val playerStats: List<PlayerStat>,
    val mvpPlayer: PlayerStat?,
    // Radar metrics
    val team1Radar: RadarMetrics,
    val team2Radar: RadarMetrics,
    // Records
    val biggestFallRoundNumber: Int,
    val biggestFallTeamName: String,
    val biggestFallScore: Int,
    val clutchBidRoundNumber: Int,
    val clutchBidPlayerName: String,
    val clutchBidAmount: Int,
    val clutchCollectedPoints: Int,
    val maxLeadDiff: Int,
    val maxLeadTeamName: String,
    val maxLeadRoundNumber: Int,
    val luckyDealerName: String,
    val luckyDealerAvgScore: Int,
    val luckyDealerBeneficiaryTeam: String,
    val rule1000BlockedHands: Int,
    // Ace in Ground
    val aceRoundsCount: Int,
    val aceRoundsRatio: Int,
    val aceTeam1Wins: Int,
    val aceTeam2Wins: Int,
    val aceTeam1WinRate: Int,
    val aceTeam2WinRate: Int,
    val aceHakemAccomplished: Int,
    val aceHakemSuccessRate: Int
)

object GameStatisticsCalculator {

    fun calculate(game: GameEntity, rounds: List<RoundEntity>): ComprehensiveGameStats {
        val completedRounds = rounds
            .filter { it.isCompleted && it.status != "Ongoing" }
            .sortedBy { it.roundNumber }
        val totalCompleted = completedRounds.size

        val team1Total = completedRounds.sumOf { it.team1FinalScore }
        val team2Total = completedRounds.sumOf { it.team2FinalScore }
        val team1Avg = if (totalCompleted > 0) (team1Total.toDouble() / totalCompleted).roundToInt() else 0
        val team2Avg = if (totalCompleted > 0) (team2Total.toDouble() / totalCompleted).roundToInt() else 0

        // Highest round scores
        val t1HighestRound = completedRounds.maxByOrNull { it.team1FinalScore }
        val t2HighestRound = completedRounds.maxByOrNull { it.team2FinalScore }
        val t1BestScore = t1HighestRound?.team1FinalScore ?: 0
        val t2BestScore = t2HighestRound?.team2FinalScore ?: 0

        val overallHighestScore: Int
        val highestScoreTeamName: String
        val highestScoreRoundNumber: Int
        if (t1BestScore >= t2BestScore && t1HighestRound != null) {
            overallHighestScore = t1BestScore
            highestScoreTeamName = game.team1Name
            highestScoreRoundNumber = t1HighestRound.roundNumber
        } else if (t2HighestRound != null) {
            overallHighestScore = t2BestScore
            highestScoreTeamName = game.team2Name
            highestScoreRoundNumber = t2HighestRound.roundNumber
        } else {
            overallHighestScore = 0
            highestScoreTeamName = ""
            highestScoreRoundNumber = 0
        }

        // Wins / Losses (no ties in Shelem)
        val team1Wins = completedRounds.count { it.team1FinalScore > it.team2FinalScore }
        val team2Wins = completedRounds.count { it.team2FinalScore > it.team1FinalScore }
        val team1WinRate = if (totalCompleted > 0) ((team1Wins.toFloat() / totalCompleted) * 100).roundToInt() else 0
        val team2WinRate = if (totalCompleted > 0) ((team2Wins.toFloat() / totalCompleted) * 100).roundToInt() else 0

        // Win Streaks
        var curT1Streak = 0
        var maxT1Streak = 0
        var curT2Streak = 0
        var maxT2Streak = 0
        completedRounds.forEach { r ->
            if (r.team1FinalScore > r.team2FinalScore) {
                curT1Streak++
                curT2Streak = 0
                if (curT1Streak > maxT1Streak) maxT1Streak = curT1Streak
            } else if (r.team2FinalScore > r.team1FinalScore) {
                curT2Streak++
                curT1Streak = 0
                if (curT2Streak > maxT2Streak) maxT2Streak = curT2Streak
            }
        }
        val bestStreakCount = max(maxT1Streak, maxT2Streak)
        val bestStreakTeamName = if (maxT1Streak >= maxT2Streak) game.team1Name else game.team2Name

        // Outcomes breakdown
        val accomplishedCount = completedRounds.count { it.status == "BidAccomplished" }
        val positiveShelemCount = completedRounds.count { it.status == "Shelem" }
        val failedBidCount = completedRounds.count { it.status == "BidFailed" }
        val negativeShelemCount = completedRounds.count { it.status == "NegativeShelem" }
        val totalSuccessBids = accomplishedCount + positiveShelemCount
        val overallHakemSuccessRate = if (totalCompleted > 0) {
            ((totalSuccessBids.toFloat() / totalCompleted) * 100).roundToInt()
        } else 0
        val overallAvgBid = if (totalCompleted > 0) {
            completedRounds.map { it.bidAmount }.average().roundToInt()
        } else 0

        val shelemsCount = positiveShelemCount + negativeShelemCount
        val narasCount = completedRounds.count { it.isNaras }

        // Team Hakem Performance
        val t1HakemRounds = completedRounds.filter { it.hakemTeam == 1 }
        val t1Succ = t1HakemRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
        val t1Fail = t1HakemRounds.count { it.status == "BidFailed" || it.status == "NegativeShelem" }
        val t1HakemRate = if (t1HakemRounds.isNotEmpty()) ((t1Succ.toFloat() / t1HakemRounds.size) * 100).roundToInt() else 0
        val t1AvgBid = if (t1HakemRounds.isNotEmpty()) t1HakemRounds.map { it.bidAmount }.average().roundToInt() else 0
        val t1MaxBidMade = t1HakemRounds
            .filter { it.status == "BidAccomplished" || it.status == "Shelem" }
            .maxOfOrNull { it.bidAmount } ?: 0
        val t1SuccRounds = t1HakemRounds.filter { it.status == "BidAccomplished" || it.status == "Shelem" }
        val t1AvgOvertrick = if (t1SuccRounds.isNotEmpty()) {
            t1SuccRounds.map { (it.team1CollectedRaw - it.bidAmount).coerceAtLeast(0) }.average().roundToInt()
        } else 0

        val t2HakemRounds = completedRounds.filter { it.hakemTeam == 2 }
        val t2Succ = t2HakemRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
        val t2Fail = t2HakemRounds.count { it.status == "BidFailed" || it.status == "NegativeShelem" }
        val t2HakemRate = if (t2HakemRounds.isNotEmpty()) ((t2Succ.toFloat() / t2HakemRounds.size) * 100).roundToInt() else 0
        val t2AvgBid = if (t2HakemRounds.isNotEmpty()) t2HakemRounds.map { it.bidAmount }.average().roundToInt() else 0
        val t2MaxBidMade = t2HakemRounds
            .filter { it.status == "BidAccomplished" || it.status == "Shelem" }
            .maxOfOrNull { it.bidAmount } ?: 0
        val t2SuccRounds = t2HakemRounds.filter { it.status == "BidAccomplished" || it.status == "Shelem" }
        val t2AvgOvertrick = if (t2SuccRounds.isNotEmpty()) {
            t2SuccRounds.map { (it.team2CollectedRaw - it.bidAmount).coerceAtLeast(0) }.average().roundToInt()
        } else 0

        // Raw Card Points (each round has 165 raw points)
        val t1RawTotal = completedRounds.sumOf {
            if (it.team1CollectedRaw + it.team2CollectedRaw > 0) it.team1CollectedRaw
            else if (it.team1FinalScore > 0) it.team1FinalScore.coerceAtMost(165) else 0
        }
        val t2RawTotal = completedRounds.sumOf {
            if (it.team1CollectedRaw + it.team2CollectedRaw > 0) it.team2CollectedRaw
            else if (it.team2FinalScore > 0) it.team2FinalScore.coerceAtMost(165) else 0
        }
        val totalRawAvailable = (t1RawTotal + t2RawTotal).coerceAtLeast(1)
        val t1RawShare = if (totalCompleted > 0) ((t1RawTotal.toFloat() / totalRawAvailable) * 100).roundToInt() else 50
        val t2RawShare = if (totalCompleted > 0) (100 - t1RawShare) else 50

        // Exact Bid Distribution: only include bid amounts that were actually placed in completed rounds
        val exactBidStats = completedRounds
            .groupBy { it.bidAmount }
            .toSortedMap()
            .map { (bidAmt, bidRounds) ->
                val made = bidRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
                val failed = bidRounds.size - made
                ExactBidStat(
                    bidAmount = bidAmt,
                    totalCount = bidRounds.size,
                    accomplishedCount = made,
                    failedCount = failed,
                    team1Count = bidRounds.count { it.hakemTeam == 1 },
                    team2Count = bidRounds.count { it.hakemTeam == 2 }
                )
            }

        // Individual Player Stats
        val playersList = listOf(
            Triple(game.team1Player1Name, 1, game.team1Name),
            Triple(game.team1Player2Name, 1, game.team1Name),
            Triple(game.team2Player1Name, 2, game.team2Name),
            Triple(game.team2Player2Name, 2, game.team2Name)
        )
        val playerStats = playersList.map { (name, teamNum, teamName) ->
            val pHakemRounds = completedRounds.filter {
                it.hakemPlayerName.equals(name, ignoreCase = true)
            }
            val pSucc = pHakemRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
            val pFail = pHakemRounds.size - pSucc
            val pRate = if (pHakemRounds.isNotEmpty()) ((pSucc.toFloat() / pHakemRounds.size) * 100).roundToInt() else 0
            val pAvgBid = if (pHakemRounds.isNotEmpty()) pHakemRounds.map { it.bidAmount }.average().roundToInt() else 0
            val pMaxBid = pHakemRounds
                .filter { it.status == "BidAccomplished" || it.status == "Shelem" }
                .maxOfOrNull { it.bidAmount } ?: 0
            val pNetScore = pHakemRounds.sumOf {
                if (teamNum == 1) it.team1FinalScore else it.team2FinalScore
            }
            val pDealerCount = completedRounds.count {
                it.dealerPlayerName.equals(name, ignoreCase = true)
            }
            PlayerStat(
                playerName = name,
                teamNumber = teamNum,
                teamName = teamName,
                hakemCount = pHakemRounds.size,
                successfulBids = pSucc,
                failedBids = pFail,
                successRate = pRate,
                avgBid = pAvgBid,
                maxBidMade = pMaxBid,
                netHakemScore = pNetScore,
                dealerCount = pDealerCount
            )
        }

        val mvpPlayer = playerStats
            .filter { it.hakemCount > 0 }
            .maxWithOrNull(
                compareBy<PlayerStat> { it.netHakemScore }
                    .thenBy { it.successRate }
                    .thenBy { it.maxBidMade }
            )

        // Radar Metrics (0f..1f)
        val t1Aggression = if (t1AvgBid >= 100) ((t1AvgBid - 95).toFloat() / 70f).coerceIn(0.15f, 1f) else 0.2f
        val t2Aggression = if (t2AvgBid >= 100) ((t2AvgBid - 95).toFloat() / 70f).coerceIn(0.15f, 1f) else 0.2f

        val maxAvgPossible = 165f
        val t1Efficiency = ((team1Avg.coerceAtLeast(0).toFloat()) / maxAvgPossible).coerceIn(0.1f, 1f)
        val t2Efficiency = ((team2Avg.coerceAtLeast(0).toFloat()) / maxAvgPossible).coerceIn(0.1f, 1f)

        val team1Radar = RadarMetrics(
            winRate = (team1WinRate / 100f).coerceIn(0.1f, 1f),
            bidSuccess = if (t1HakemRounds.isNotEmpty()) (t1HakemRate / 100f).coerceIn(0.1f, 1f) else 0.2f,
            rawPointsShare = (t1RawShare / 100f).coerceIn(0.1f, 1f),
            bidAggression = t1Aggression,
            scoreEfficiency = t1Efficiency
        )
        val team2Radar = RadarMetrics(
            winRate = (team2WinRate / 100f).coerceIn(0.1f, 1f),
            bidSuccess = if (t2HakemRounds.isNotEmpty()) (t2HakemRate / 100f).coerceIn(0.1f, 1f) else 0.2f,
            rawPointsShare = (t2RawShare / 100f).coerceIn(0.1f, 1f),
            bidAggression = t2Aggression,
            scoreEfficiency = t2Efficiency
        )

        // Match Records
        // 1. Biggest Fall (lowest negative score)
        var biggestFallScore = 0
        var biggestFallRound = 0
        var biggestFallTeam = ""
        completedRounds.forEach { r ->
            if (r.team1FinalScore < biggestFallScore) {
                biggestFallScore = r.team1FinalScore
                biggestFallRound = r.roundNumber
                biggestFallTeam = game.team1Name
            }
            if (r.team2FinalScore < biggestFallScore) {
                biggestFallScore = r.team2FinalScore
                biggestFallRound = r.roundNumber
                biggestFallTeam = game.team2Name
            }
        }

        // 2. Most precise / clutch bid (smallest non-negative margin between hakem raw points and bid)
        val accomplishedRounds = completedRounds.filter {
            it.status == "BidAccomplished" || it.status == "Shelem"
        }
        val clutchRound = accomplishedRounds.minByOrNull { r ->
            val hakemRaw = if (r.hakemTeam == 1) r.team1CollectedRaw else r.team2CollectedRaw
            (hakemRaw - r.bidAmount).coerceAtLeast(0)
        }
        val clutchBidRoundNumber = clutchRound?.roundNumber ?: 0
        val clutchBidPlayerName = clutchRound?.hakemPlayerName?.ifBlank {
            if (clutchRound.hakemTeam == 1) game.team1Name else game.team2Name
        } ?: ""
        val clutchBidAmount = clutchRound?.bidAmount ?: 0
        val clutchCollectedPoints = if (clutchRound != null) {
            if (clutchRound.hakemTeam == 1) clutchRound.team1CollectedRaw else clutchRound.team2CollectedRaw
        } else 0

        // 3. Largest cumulative lead
        var runT1 = 0
        var runT2 = 0
        var maxLeadDiff = 0
        var maxLeadTeamName = ""
        var maxLeadRoundNumber = 0
        completedRounds.forEach { r ->
            runT1 += r.team1FinalScore
            runT2 += r.team2FinalScore
            val diff = abs(runT1 - runT2)
            if (diff > maxLeadDiff) {
                maxLeadDiff = diff
                maxLeadTeamName = if (runT1 > runT2) game.team1Name else game.team2Name
                maxLeadRoundNumber = r.roundNumber
            }
        }

        // 4. Luckiest Dealer
        var luckyDealerName = ""
        var luckyDealerAvgScore = 0
        var luckyDealerBeneficiary = ""
        val roundsByDealer = completedRounds
            .filter { it.dealerPlayerName.isNotBlank() }
            .groupBy { it.dealerPlayerName }
        roundsByDealer.forEach { (dealer, dRounds) ->
            val t1AvgWithDealer = dRounds.map { it.team1FinalScore }.average().roundToInt()
            val t2AvgWithDealer = dRounds.map { it.team2FinalScore }.average().roundToInt()
            if (t1AvgWithDealer > luckyDealerAvgScore) {
                luckyDealerAvgScore = t1AvgWithDealer
                luckyDealerName = dealer
                luckyDealerBeneficiary = game.team1Name
            }
            if (t2AvgWithDealer > luckyDealerAvgScore) {
                luckyDealerAvgScore = t2AvgWithDealer
                luckyDealerName = dealer
                luckyDealerBeneficiary = game.team2Name
            }
        }

        // 5. Rule 1000 blocked hands
        val rule1000Blocks = completedRounds.count { r ->
            (r.hakemTeam == 1 && r.team2CollectedRaw > 0 && r.team2FinalScore == 0) ||
                (r.hakemTeam == 2 && r.team1CollectedRaw > 0 && r.team1FinalScore == 0)
        }

        // Ace in Ground Stats
        val aceRounds = completedRounds.filter { it.hasAceInGround }
        val aceRoundsCount = aceRounds.size
        val aceRoundsRatio = if (totalCompleted > 0) ((aceRoundsCount.toFloat() / totalCompleted) * 100).roundToInt() else 0
        val aceTeam1Wins = aceRounds.count { it.team1FinalScore > it.team2FinalScore }
        val aceTeam2Wins = aceRounds.count { it.team2FinalScore > it.team1FinalScore }
        val aceTeam1WinRate = if (aceRoundsCount > 0) ((aceTeam1Wins.toFloat() / aceRoundsCount) * 100).roundToInt() else 0
        val aceTeam2WinRate = if (aceRoundsCount > 0) ((aceTeam2Wins.toFloat() / aceRoundsCount) * 100).roundToInt() else 0
        val aceHakemAccomplished = aceRounds.count { it.status == "BidAccomplished" || it.status == "Shelem" }
        val aceHakemSuccessRate = if (aceRoundsCount > 0) ((aceHakemAccomplished.toFloat() / aceRoundsCount) * 100).roundToInt() else 0

        return ComprehensiveGameStats(
            totalCompletedRounds = totalCompleted,
            team1TotalScore = team1Total,
            team2TotalScore = team2Total,
            team1AvgScore = team1Avg,
            team2AvgScore = team2Avg,
            team1Wins = team1Wins,
            team2Wins = team2Wins,
            team1WinRate = team1WinRate,
            team2WinRate = team2WinRate,
            team1BestScore = t1BestScore,
            team2BestScore = t2BestScore,
            team1BestRoundNumber = t1HighestRound?.roundNumber ?: 0,
            team2BestRoundNumber = t2HighestRound?.roundNumber ?: 0,
            overallHighestScore = overallHighestScore,
            highestScoreTeamName = highestScoreTeamName,
            highestScoreRoundNumber = highestScoreRoundNumber,
            team1MaxStreak = maxT1Streak,
            team2MaxStreak = maxT2Streak,
            bestStreakCount = bestStreakCount,
            bestStreakTeamName = bestStreakTeamName,
            accomplishedCount = accomplishedCount,
            positiveShelemCount = positiveShelemCount,
            failedBidCount = failedBidCount,
            negativeShelemCount = negativeShelemCount,
            overallHakemSuccessRate = overallHakemSuccessRate,
            overallAvgBid = overallAvgBid,
            shelemsCount = shelemsCount,
            narasCount = narasCount,
            team1HakemCount = t1HakemRounds.size,
            team1SuccessfulBids = t1Succ,
            team1FailedBids = t1Fail,
            team1HakemSuccessRate = t1HakemRate,
            team1AvgBid = t1AvgBid,
            team1MaxBidMade = t1MaxBidMade,
            team1AvgOvertrick = t1AvgOvertrick,
            team2HakemCount = t2HakemRounds.size,
            team2SuccessfulBids = t2Succ,
            team2FailedBids = t2Fail,
            team2HakemSuccessRate = t2HakemRate,
            team2AvgBid = t2AvgBid,
            team2MaxBidMade = t2MaxBidMade,
            team2AvgOvertrick = t2AvgOvertrick,
            team1TotalRawPoints = t1RawTotal,
            team2TotalRawPoints = t2RawTotal,
            team1RawSharePercent = t1RawShare,
            team2RawSharePercent = t2RawShare,
            exactBidStats = exactBidStats,
            playerStats = playerStats,
            mvpPlayer = mvpPlayer,
            team1Radar = team1Radar,
            team2Radar = team2Radar,
            biggestFallRoundNumber = biggestFallRound,
            biggestFallTeamName = biggestFallTeam,
            biggestFallScore = biggestFallScore,
            clutchBidRoundNumber = clutchBidRoundNumber,
            clutchBidPlayerName = clutchBidPlayerName,
            clutchBidAmount = clutchBidAmount,
            clutchCollectedPoints = clutchCollectedPoints,
            maxLeadDiff = maxLeadDiff,
            maxLeadTeamName = maxLeadTeamName,
            maxLeadRoundNumber = maxLeadRoundNumber,
            luckyDealerName = luckyDealerName,
            luckyDealerAvgScore = luckyDealerAvgScore,
            luckyDealerBeneficiaryTeam = luckyDealerBeneficiary,
            rule1000BlockedHands = rule1000Blocks,
            aceRoundsCount = aceRoundsCount,
            aceRoundsRatio = aceRoundsRatio,
            aceTeam1Wins = aceTeam1Wins,
            aceTeam2Wins = aceTeam2Wins,
            aceTeam1WinRate = aceTeam1WinRate,
            aceTeam2WinRate = aceTeam2WinRate,
            aceHakemAccomplished = aceHakemAccomplished,
            aceHakemSuccessRate = aceHakemSuccessRate
        )
    }
}
