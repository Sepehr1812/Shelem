package com.shelem.scoresheet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shelem.scoresheet.domain.Team

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
    val trackAceInGround: Boolean = false,
    val initialDealerSeat: Int = 0
) {
    /**
     * Returns the 4 players in counter-clockwise (پادساعتگرد) table order:
     * Index 0: Bottom ("شما" / Team 1 Player 1)
     * Index 1: Right (Team 2 Player 1)
     * Index 2: Top (Team 1 Player 2)
     * Index 3: Left (Team 2 Player 2)
     */
    fun getCounterClockwiseSeating(): List<Pair<String, Team>> = listOf(
        Pair(team1Player1Name, Team.TEAM_1),
        Pair(team2Player1Name, Team.TEAM_2),
        Pair(team1Player2Name, Team.TEAM_1),
        Pair(team2Player2Name, Team.TEAM_2)
    )

    /**
     * Computes the dealer for the next round based on counter-clockwise rotation.
     * If [previousDealerName] is null (first round), returns the player at [initialDealerSeat].
     */
    fun getCurrentDealer(previousDealerName: String?): Pair<String, Team> {
        val seating = getCounterClockwiseSeating()
        if (previousDealerName != null) {
            val prevIdx = seating.indexOfFirst { it.first.equals(previousDealerName, ignoreCase = true) }
            if (prevIdx != -1) {
                return seating[(prevIdx + 1) % seating.size]
            }
        }
        val safeSeat = initialDealerSeat.coerceIn(0, seating.lastIndex)
        return seating[safeSeat]
    }
}

