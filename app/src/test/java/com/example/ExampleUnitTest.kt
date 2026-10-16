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

  @Test
  fun scoringEngine_negativeShelem_decreasesDoubleNegativeOfBid() {
    // When Hakem collects less than 85 points, negative shelem applies:
    // Hakem receives -2 * bidAmount, and opponent receives collected points (165 - hakemPoints)

    // Scenario 1: Bid is 100, Hakem collects 80 points (< 85)
    val result100 = com.example.domain.ScoringEngine.calculateRoundScores(bidAmount = 100, hakemCollectedPoints = 80)
    org.junit.Assert.assertTrue(result100 is com.example.domain.RoundResult.NegativeShelem)
    assertEquals(-200, result100.hakemFinalScore)
    assertEquals(85, result100.otherFinalScore)

    // Scenario 2: Bid is 110, Hakem collects 50 points (< 85)
    val result110 = com.example.domain.ScoringEngine.calculateRoundScores(bidAmount = 110, hakemCollectedPoints = 50)
    org.junit.Assert.assertTrue(result110 is com.example.domain.RoundResult.NegativeShelem)
    assertEquals(-220, result110.hakemFinalScore)
    assertEquals(115, result110.otherFinalScore)

    // Scenario 3: Bid is 125, Hakem collects 0 points (< 85)
    val result125 = com.example.domain.ScoringEngine.calculateRoundScores(bidAmount = 125, hakemCollectedPoints = 0)
    org.junit.Assert.assertTrue(result125 is com.example.domain.RoundResult.NegativeShelem)
    assertEquals(-250, result125.hakemFinalScore)
    assertEquals(165, result125.otherFinalScore)

    // Scenario 4: Bid is 165 (full Shelem), Hakem collects 84 points (< 85)
    val result165 = com.example.domain.ScoringEngine.calculateRoundScores(bidAmount = 165, hakemCollectedPoints = 84)
    org.junit.Assert.assertTrue(result165 is com.example.domain.RoundResult.NegativeShelem)
    assertEquals(-330, result165.hakemFinalScore)
    assertEquals(81, result165.otherFinalScore)
  }

  @Test
  fun scoringEngine_teamScoreMapping_accountsForHakemTeam() {
    // Team 1 is Hakem, bid 100, collects 70 points -> Team 1 gets -200, Team 2 gets 95
    val team1Hakem = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_1,
      bidAmount = 100,
      hakemCollectedPoints = 70
    )
    assertEquals(-200, team1Hakem.team1FinalScore)
    assertEquals(95, team1Hakem.team2FinalScore)

    // Team 2 is Hakem, bid 120, collects 60 points -> Team 1 gets 105, Team 2 gets -240
    val team2Hakem = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_2,
      bidAmount = 120,
      hakemCollectedPoints = 60
    )
    assertEquals(105, team2Hakem.team1FinalScore)
    assertEquals(-240, team2Hakem.team2FinalScore)
  }
}

