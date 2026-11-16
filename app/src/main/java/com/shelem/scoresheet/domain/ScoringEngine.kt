package com.shelem.scoresheet.domain

object ScoringEngine {

    /**
     * Calculates the round scores based on PRD Shelem rules.
     * Total points in a standard round = 165.
     */
    fun calculateRoundScores(
        bidAmount: Int,
        hakemCollectedPoints: Int
    ): RoundResult {
        val otherTeamCollectedPoints = 165 - hakemCollectedPoints

        return when {
            hakemCollectedPoints == 165 -> {
                RoundResult.Shelem() // Positive Shelem (+330)
            }
            hakemCollectedPoints < 85 -> {
                RoundResult.NegativeShelem(
                    hakemFinalScore = -2 * bidAmount,
                    otherFinalScore = otherTeamCollectedPoints
                ) // Double Negative: double negative of the bid (-2 * bidAmount)
            }
            hakemCollectedPoints >= bidAmount -> {
                RoundResult.BidAccomplished(
                    hakemFinalScore = hakemCollectedPoints,
                    otherFinalScore = otherTeamCollectedPoints
                ) // Met the bid
            }
            else -> {
                RoundResult.BidFailed(
                    hakemFinalScore = -bidAmount,
                    otherFinalScore = otherTeamCollectedPoints
                ) // Failed to meet bid
            }
        }
    }

    /**
     * Helper to compute final team scores considering which team is the Hakem,
     * with optional rule for teams reaching 1000+ points:
     * When a team's score is >= 1000, it only receives points if it wins that round.
     */
    fun computeTeamScores(
        hakemTeam: Team,
        bidAmount: Int,
        hakemCollectedPoints: Int,
        team1PriorScore: Int = 0,
        team2PriorScore: Int = 0,
        winHandOnlyOver1000: Boolean = false
    ): CalculatedRoundScores {
        val result = calculateRoundScores(bidAmount, hakemCollectedPoints)
        var (t1Score, t2Score) = when (hakemTeam) {
            Team.TEAM_1 -> Pair(result.hakemFinalScore, result.otherFinalScore)
            Team.TEAM_2 -> Pair(result.otherFinalScore, result.hakemFinalScore)
        }

        val hakemWon = result is RoundResult.Shelem || result is RoundResult.BidAccomplished
        val team1Won = if (hakemTeam == Team.TEAM_1) hakemWon else !hakemWon
        val team2Won = if (hakemTeam == Team.TEAM_2) hakemWon else !hakemWon

        var team1RestrictedBy1000 = false
        var team2RestrictedBy1000 = false

        if (winHandOnlyOver1000) {
            if (team1PriorScore >= 1000 && !team1Won && t1Score > 0) {
                t1Score = 0
                team1RestrictedBy1000 = true
            }
            if (team2PriorScore >= 1000 && !team2Won && t2Score > 0) {
                t2Score = 0
                team2RestrictedBy1000 = true
            }
        }

        return CalculatedRoundScores(
            team1FinalScore = t1Score,
            team2FinalScore = t2Score,
            result = result,
            team1RestrictedBy1000 = team1RestrictedBy1000,
            team2RestrictedBy1000 = team2RestrictedBy1000
        )
    }
}
