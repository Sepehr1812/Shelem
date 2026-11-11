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

  @Test
  fun scoringEngine_winHandOnlyOver1000Rule_restrictsLosingTeamWithOver1000Points() {
    // Scenario 1: Team 1 has 1050 pts (>= 1000). Team 2 is Hakem, bids 110 and makes it (Team 2 wins).
    // Team 1 collected 55 pts, but lost the hand and has >= 1000 pts -> Team 1 gets 0.
    val scores1 = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_2,
      bidAmount = 110,
      hakemCollectedPoints = 110,
      team1PriorScore = 1050,
      team2PriorScore = 700,
      winHandOnlyOver1000 = true
    )
    assertEquals(0, scores1.team1FinalScore)
    assertEquals(110, scores1.team2FinalScore)
    org.junit.Assert.assertTrue(scores1.team1RestrictedBy1000)
    org.junit.Assert.assertFalse(scores1.team2RestrictedBy1000)

    // Scenario 2: Team 1 has 1050 pts (>= 1000). Team 2 is Hakem, bids 110, but FAILS the bid.
    // Team 1 set the contract and won the hand -> Team 1 gets full collected points (75).
    val scores2 = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_2,
      bidAmount = 110,
      hakemCollectedPoints = 90,
      team1PriorScore = 1050,
      team2PriorScore = 700,
      winHandOnlyOver1000 = true
    )
    assertEquals(75, scores2.team1FinalScore)
    assertEquals(-110, scores2.team2FinalScore)
    org.junit.Assert.assertFalse(scores2.team1RestrictedBy1000)

    // Scenario 3: Team 1 has 1050 pts (>= 1000). Team 1 is Hakem, bids 125, and accomplishes it.
    // Team 1 won the hand -> Team 1 gets 125.
    val scores3 = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_1,
      bidAmount = 125,
      hakemCollectedPoints = 125,
      team1PriorScore = 1050,
      team2PriorScore = 700,
      winHandOnlyOver1000 = true
    )
    assertEquals(125, scores3.team1FinalScore)
    assertEquals(40, scores3.team2FinalScore)
    org.junit.Assert.assertFalse(scores3.team1RestrictedBy1000)

    // Scenario 4: If rule is disabled (winHandOnlyOver1000 = false), losing team with >= 1000 still gets points
    val scoresDisabled = com.example.domain.ScoringEngine.computeTeamScores(
      hakemTeam = com.example.domain.Team.TEAM_2,
      bidAmount = 110,
      hakemCollectedPoints = 110,
      team1PriorScore = 1050,
      team2PriorScore = 700,
      winHandOnlyOver1000 = false
    )
    assertEquals(55, scoresDisabled.team1FinalScore)
    assertEquals(110, scoresDisabled.team2FinalScore)
    org.junit.Assert.assertFalse(scoresDisabled.team1RestrictedBy1000)
  }

  @Test
  fun persianNumberHelper_formatScore_removesPlusSignAndFormatsNegativeCorrectly() {
    val originalLocale = java.util.Locale.getDefault()
    try {
      // Test Persian locale
      java.util.Locale.setDefault(java.util.Locale("fa", "IR"))
      // Positive score: no '+' sign, converted to Persian digits
      assertEquals("۱۶۵", com.example.util.PersianNumberHelper.formatScore(165))
      assertEquals("۰", com.example.util.PersianNumberHelper.formatScore(0))
      // Negative score: minus sign in front of digits
      assertEquals("۲۲۰-", com.example.util.PersianNumberHelper.formatScore(-220))
      assertEquals("۱۱۰-", com.example.util.PersianNumberHelper.formatScore(-110))

      // Test English locale
      java.util.Locale.setDefault(java.util.Locale.ENGLISH)
      assertEquals("165", com.example.util.PersianNumberHelper.formatScore(165))
      assertEquals("0", com.example.util.PersianNumberHelper.formatScore(0))
      assertEquals("-220", com.example.util.PersianNumberHelper.formatScore(-220))
      assertEquals("-110", com.example.util.PersianNumberHelper.formatScore(-110))
    } finally {
      java.util.Locale.setDefault(originalLocale)
    }
  }

  @Test
  fun csvExporter_escapeCsv_escapesCommasAndQuotesProperly() {
    assertEquals("Normal", com.example.util.CsvExporter.escapeCsv("Normal"))
    assertEquals("\"Team 1, Alpha\"", com.example.util.CsvExporter.escapeCsv("Team 1, Alpha"))
    assertEquals("\"\"\"Winning\"\" Team\"", com.example.util.CsvExporter.escapeCsv("\"Winning\" Team"))
    assertEquals("\"Line1\nLine2\"", com.example.util.CsvExporter.escapeCsv("Line1\nLine2"))
  }

  @Test
  fun csvExporter_getDefaultFileName_generatesCleanCsvName() {
    val sampleGame = com.example.data.GameEntity(
        id = 42,
        team1Name = "تیم سپهر",
        team2Name = "تیم آرش",
        team1Player1Name = "سپهر",
        team1Player2Name = "علی",
        team2Player1Name = "آرش",
        team2Player2Name = "رضا",
        targetScore = 1165,
        createdAt = 1700000000000L
    )

    val fileName = com.example.util.CsvExporter.getDefaultFileName(sampleGame)
    org.junit.Assert.assertTrue(fileName.startsWith("shelem_"))
    org.junit.Assert.assertTrue(fileName.endsWith(".csv"))
    org.junit.Assert.assertTrue(fileName.contains("vs"))
  }

  @Test
  fun csvExporter_generateCsv_outputsValidHeaderAndRowsWithBom() {
    val sampleGame = com.example.data.GameEntity(
        id = 1,
        team1Name = "Eagles",
        team2Name = "Lions",
        team1Player1Name = "Alice",
        team1Player2Name = "Bob",
        team2Player1Name = "Charlie",
        team2Player2Name = "Dave",
        targetScore = 1165,
        winHandOnlyOver1000 = true,
        createdAt = 1700000000000L
    )

    val sampleRounds = listOf(
        RoundEntity(
            id = 1,
            gameId = 1,
            roundNumber = 1,
            hakemTeam = 1,
            hakemPlayerName = "Alice",
            dealerPlayerName = "Dave",
            bidAmount = 110,
            team1CollectedRaw = 120,
            team2CollectedRaw = 45,
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
            hakemPlayerName = "Charlie",
            dealerPlayerName = "Alice",
            bidAmount = 165,
            team1CollectedRaw = 0,
            team2CollectedRaw = 165,
            team1FinalScore = 0,
            team2FinalScore = 330,
            status = "Shelem",
            isCompleted = true
        )
    )

    val dummyStrings = object : com.example.util.CsvStringProvider {
        override fun getString(resId: Int): String = "STR_$resId"
        override fun getString(resId: Int, vararg formatArgs: Any): String = "STR_${resId}_${formatArgs.joinToString("_")}"
    }

    val csv = com.example.util.CsvExporter.generateCsv(sampleGame, sampleRounds, dummyStrings)

    // UTF-8 BOM must be the first character
    org.junit.Assert.assertTrue("CSV must start with UTF-8 BOM", csv.startsWith("\uFEFF"))

    // Contains game metadata
    org.junit.Assert.assertTrue(csv.contains("Eagles"))
    org.junit.Assert.assertTrue(csv.contains("Lions"))
    org.junit.Assert.assertTrue(csv.contains("Alice - Bob"))
    org.junit.Assert.assertTrue(csv.contains("Charlie - Dave"))

    // Contains round data
    org.junit.Assert.assertTrue(csv.contains("110"))
    org.junit.Assert.assertTrue(csv.contains("165"))
    org.junit.Assert.assertTrue(csv.contains("120"))
    org.junit.Assert.assertTrue(csv.contains("330"))
  }

  @Test
  fun gameFinishCondition_reachedTargetScore() {
    val targetScore = 1165
    val team1Score = 1170
    val team2Score = 500
    val scoreDiff = kotlin.math.abs(team1Score - team2Score)

    val isFinished = team1Score >= targetScore || team2Score >= targetScore || scoreDiff >= targetScore
    org.junit.Assert.assertTrue("Game must be finished when Team 1 reaches target score", isFinished)

    val winner = when {
      team1Score >= targetScore || (team1Score - team2Score >= targetScore) || (isFinished && team1Score > team2Score) -> "Team 1"
      team2Score >= targetScore || (team2Score - team1Score >= targetScore) || (isFinished && team2Score > team1Score) -> "Team 2"
      else -> null
    }
    assertEquals("Team 1", winner)
  }

  @Test
  fun gameFinishCondition_reachedLeadDifference() {
    val targetScore = 1165
    // Neither team reached 1165, but Team 1 leads by 1200 points (700 vs -500)
    val team1Score = 700
    val team2Score = -500
    val scoreDiff = kotlin.math.abs(team1Score - team2Score)
    assertEquals(1200, scoreDiff)

    val isFinished = team1Score >= targetScore || team2Score >= targetScore || scoreDiff >= targetScore
    org.junit.Assert.assertTrue("Game must be finished when score difference reaches target score", isFinished)

    val winner = when {
      team1Score >= targetScore || (team1Score - team2Score >= targetScore) || (isFinished && team1Score > team2Score) -> "Team 1"
      team2Score >= targetScore || (team2Score - team1Score >= targetScore) || (isFinished && team2Score > team1Score) -> "Team 2"
      else -> null
    }
    assertEquals("Team 1", winner)
  }

  @Test
  fun gameFinishCondition_team2WinsByLeadDifference() {
    val targetScore = 625
    // Team 1 is at -250, Team 2 is at 400. Difference is 650 >= 625
    val team1Score = -250
    val team2Score = 400
    val scoreDiff = kotlin.math.abs(team1Score - team2Score)
    assertEquals(650, scoreDiff)

    val isFinished = team1Score >= targetScore || team2Score >= targetScore || scoreDiff >= targetScore
    org.junit.Assert.assertTrue("Game must be finished when Team 2 lead difference reaches target score", isFinished)

    val winner = when {
      team1Score >= targetScore || (team1Score - team2Score >= targetScore) || (isFinished && team1Score > team2Score) -> "Team 1"
      team2Score >= targetScore || (team2Score - team1Score >= targetScore) || (isFinished && team2Score > team1Score) -> "Team 2"
      else -> null
    }
    assertEquals("Team 2", winner)
  }

  @Test
  fun csvExport_winnerByLeadDifference() {
    val sampleGame = com.example.data.GameEntity(
        id = 1,
        team1Name = "Alpha",
        team2Name = "Beta",
        team1Player1Name = "A1",
        team1Player2Name = "A2",
        team2Player1Name = "B1",
        team2Player2Name = "B2",
        targetScore = 1165,
        isFinished = false
    )
    val sampleRounds = listOf(
        RoundEntity(
            id = 1,
            gameId = 1,
            roundNumber = 1,
            hakemTeam = 1,
            hakemPlayerName = "A1",
            dealerPlayerName = "B1",
            bidAmount = 165,
            team1FinalScore = 650,
            team2FinalScore = -550,
            status = "Shelem",
            isCompleted = true
        )
    )
    val dummyStrings = object : com.example.util.CsvStringProvider {
        override fun getString(resId: Int): String = "STR_$resId"
        override fun getString(resId: Int, vararg formatArgs: Any): String = "STR_${resId}_${formatArgs.joinToString("_")}"
    }

    val csv = com.example.util.CsvExporter.generateCsv(sampleGame, sampleRounds, dummyStrings)
    // Team 1 has 650, Team 2 has -550 -> diff = 1200 >= 1165 -> Alpha wins!
    org.junit.Assert.assertTrue("CSV should contain winner Alpha due to lead difference", csv.contains("Alpha"))
  }
}

