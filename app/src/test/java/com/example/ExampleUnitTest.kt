package com.example

import com.example.data.RoundEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun gameStatistics_highestScoreAndWinLossCalculation() {
    val sampleRounds = listOf(
      RoundEntity(
        id = 1,
        gameId = 1,
        roundNumber = 1,
        hakemTeam = 1,
        hakemPlayerName = "Ali",
        dealerPlayerName = "Reza",
        bidAmount = 100,
        team1FinalScore = 120,
        team2FinalScore = 45,
        status = "BidAccomplished",
        isCompleted = true
      ),
      RoundEntity(
        id = 2,
        gameId = 1,
        roundNumber = 2,
        hakemTeam = 2,
        hakemPlayerName = "Reza",
        dealerPlayerName = "Ali",
        bidAmount = 165,
        team1FinalScore = 0,
        team2FinalScore = 330,
        status = "Shelem",
        isCompleted = true
      ),
      RoundEntity(
        id = 3,
        gameId = 1,
        roundNumber = 3,
        hakemTeam = 1,
        hakemPlayerName = "Sepehr",
        dealerPlayerName = "Arash",
        bidAmount = 110,
        team1FinalScore = -110,
        team2FinalScore = 165,
        status = "BidFailed",
        isCompleted = true
      )
    )

    val completed = sampleRounds.filter { it.isCompleted && it.status != "Ongoing" }
    assertEquals(3, completed.size)

    val team1Wins = completed.count { it.team1FinalScore > it.team2FinalScore }
    val team2Wins = completed.count { it.team2FinalScore > it.team1FinalScore }
    assertEquals(1, team1Wins)
    assertEquals(2, team2Wins)

    val team1Highest = completed.maxByOrNull { it.team1FinalScore }?.team1FinalScore ?: 0
    val team2Highest = completed.maxByOrNull { it.team2FinalScore }?.team2FinalScore ?: 0
    assertEquals(120, team1Highest)
    assertEquals(330, team2Highest)

    val overallHighest = maxOf(team1Highest, team2Highest)
    assertEquals(330, overallHighest)
  }

  @Test
  fun dealerRotation_clockwiseOrderAroundTable() {
    val playerSeating = listOf("Ali", "Reza", "Hassan", "Mehdi")

    // If previous round dealer was Ali (index 0), next dealer should be Reza (index 1)
    val prevDealer1 = "Ali"
    val idx1 = playerSeating.indexOfFirst { it.equals(prevDealer1, ignoreCase = true) }
    val nextDealer1 = playerSeating[(idx1 + 1) % playerSeating.size]
    assertEquals("Reza", nextDealer1)

    // If previous round dealer was Reza (index 1), next dealer should be Hassan (index 2)
    val prevDealer2 = "Reza"
    val idx2 = playerSeating.indexOfFirst { it.equals(prevDealer2, ignoreCase = true) }
    val nextDealer2 = playerSeating[(idx2 + 1) % playerSeating.size]
    assertEquals("Hassan", nextDealer2)

    // If previous round dealer was Hassan (index 2), next dealer should be Mehdi (index 3)
    val prevDealer3 = "Hassan"
    val idx3 = playerSeating.indexOfFirst { it.equals(prevDealer3, ignoreCase = true) }
    val nextDealer3 = playerSeating[(idx3 + 1) % playerSeating.size]
    assertEquals("Mehdi", nextDealer3)

    // If previous round dealer was Mehdi (index 3), next dealer rotates back to Ali (index 0)
    val prevDealer4 = "Mehdi"
    val idx4 = playerSeating.indexOfFirst { it.equals(prevDealer4, ignoreCase = true) }
    val nextDealer4 = playerSeating[(idx4 + 1) % playerSeating.size]
    assertEquals("Ali", nextDealer4)
  }

  @Test
  fun cumulativeScoreProgression_calculatesAccurately() {
    val roundScores = listOf(
      Pair(165, 0),
      Pair(0, 165),
      Pair(100, 65),
      Pair(-165, 165)
    )

    var team1Cum = 0
    var team2Cum = 0
    val cumulativeT1 = mutableListOf<Int>()
    val cumulativeT2 = mutableListOf<Int>()

    roundScores.forEach { (r1, r2) ->
      team1Cum += r1
      team2Cum += r2
      cumulativeT1.add(team1Cum)
      cumulativeT2.add(team2Cum)
    }

    assertEquals(listOf(165, 165, 265, 100), cumulativeT1)
    assertEquals(listOf(0, 165, 230, 395), cumulativeT2)
  }
}

