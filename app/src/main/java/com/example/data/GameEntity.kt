package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val team1Name: String,
    val team2Name: String,
    val team1Player1Name: String,
    val team1Player2Name: String,
    val team2Player1Name: String,
    val team2Player2Name: String,
    val targetScore: Int = 1165,
    val createdAt: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false,
    val winHandOnlyOver1000: Boolean = true,
    val trackAceInGround: Boolean = false
)
