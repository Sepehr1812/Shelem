package com.shelem.scoresheet.domain

sealed class RoundResult {
    abstract val hakemFinalScore: Int
    abstract val otherFinalScore: Int

    data class Shelem(
        override val hakemFinalScore: Int = 330,
        override val otherFinalScore: Int = 0
    ) : RoundResult()

    data class NegativeShelem(
        override val hakemFinalScore: Int,
        override val otherFinalScore: Int
    ) : RoundResult()

    data class BidAccomplished(
        override val hakemFinalScore: Int,
        override val otherFinalScore: Int
    ) : RoundResult()

    data class BidFailed(
        override val hakemFinalScore: Int,
        override val otherFinalScore: Int
    ) : RoundResult()
}

enum class Team {
    TEAM_1,
    TEAM_2
}

data class CalculatedRoundScores(
    val team1FinalScore: Int,
    val team2FinalScore: Int,
    val result: RoundResult,
    val team1RestrictedBy1000: Boolean = false,
    val team2RestrictedBy1000: Boolean = false
)
