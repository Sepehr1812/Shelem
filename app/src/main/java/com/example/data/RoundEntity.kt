package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameId"])]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val roundNumber: Int,
    val hakemTeam: Int, // 1 for Team1, 2 for Team2
    val hakemPlayerName: String,
    val dealerPlayerName: String,
    val bidAmount: Int,
    val isNaras: Boolean = false,
    val team1CollectedRaw: Int = 0,
    val team2CollectedRaw: Int = 0,
    val team1FinalScore: Int = 0,
    val team2FinalScore: Int = 0,
    val status: String = "Ongoing", // "Ongoing", "Shelem", "NegativeShelem", "BidAccomplished", "BidFailed"
    val isCompleted: Boolean = true
)
