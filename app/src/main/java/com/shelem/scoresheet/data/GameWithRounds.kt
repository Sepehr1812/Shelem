package com.shelem.scoresheet.data

import androidx.room.Embedded
import androidx.room.Relation

data class GameWithRounds(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val rounds: List<RoundEntity>
) {
    val team1TotalScore: Int
        get() = rounds.filter { it.isCompleted }.sumOf { it.team1FinalScore }

    val team2TotalScore: Int
        get() = rounds.filter { it.isCompleted }.sumOf { it.team2FinalScore }
}
