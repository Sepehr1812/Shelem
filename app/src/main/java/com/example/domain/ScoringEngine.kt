package com.example.domain

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
     * Helper to compute final team scores considering which team is the Hakem.
     */
    fun computeTeamScores(
        hakemTeam: Team,
        bidAmount: Int,
        hakemCollectedPoints: Int
    ): CalculatedRoundScores {
        val result = calculateRoundScores(bidAmount, hakemCollectedPoints)
        val (t1Score, t2Score) = when (hakemTeam) {
            Team.TEAM_1 -> Pair(result.hakemFinalScore, result.otherFinalScore)
            Team.TEAM_2 -> Pair(result.otherFinalScore, result.hakemFinalScore)
        }
        return CalculatedRoundScores(
            team1FinalScore = t1Score,
            team2FinalScore = t2Score,
            result = result
        )
    }
}
