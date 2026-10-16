package com.example.data

import com.example.domain.ScoringEngine
import com.example.domain.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShelemRepository(private val dao: ShelemDao) {

    val ongoingGames: Flow<List<GameEntity>> = dao.getOngoingGames()
    val finishedGames: Flow<List<GameEntity>> = dao.getFinishedGames()

    val ongoingGamesWithRounds: Flow<List<GameWithRounds>> = dao.getOngoingGamesWithRounds()
    val finishedGamesWithRounds: Flow<List<GameWithRounds>> = dao.getFinishedGamesWithRounds()

    fun getGame(gameId: Long): Flow<GameEntity?> = dao.getGameById(gameId)

    fun getRounds(gameId: Long): Flow<List<RoundEntity>> = dao.getRoundsForGame(gameId)

    suspend fun createGame(
        team1Name: String,
        team2Name: String,
        team1Player1: String,
        team1Player2: String,
        team2Player1: String,
        team2Player2: String,
        targetScore: Int
    ): Long {
        val t1Name = team1Name.ifBlank { "$team1Player1 - $team1Player2" }.ifBlank { "تیم ۱" }
        val t2Name = team2Name.ifBlank { "$team2Player1 - $team2Player2" }.ifBlank { "تیم ۲" }

        val game = GameEntity(
            team1Name = t1Name,
            team2Name = t2Name,
            team1Player1Name = team1Player1.ifBlank { "بازیکن ۱" },
            team1Player2Name = team1Player2.ifBlank { "بازیکن ۲" },
            team2Player1Name = team2Player1.ifBlank { "بازیکن ۳" },
            team2Player2Name = team2Player2.ifBlank { "بازیکن ۴" },
            targetScore = targetScore
        )
        return dao.insertGame(game)
    }

    suspend fun createOngoingRound(
        gameId: Long,
        currentRoundCount: Int,
        hakemTeam: Team,
        hakemPlayerName: String,
        dealerPlayerName: String,
        bidAmount: Int,
        isNaras: Boolean
    ): Long {
        val round = RoundEntity(
            gameId = gameId,
            roundNumber = currentRoundCount + 1,
            hakemTeam = if (hakemTeam == Team.TEAM_1) 1 else 2,
            hakemPlayerName = hakemPlayerName,
            dealerPlayerName = dealerPlayerName,
            bidAmount = bidAmount,
            isNaras = isNaras,
            team1CollectedRaw = 0,
            team2CollectedRaw = 0,
            team1FinalScore = 0,
            team2FinalScore = 0,
            status = "Ongoing",
            isCompleted = false
        )
        return dao.insertRound(round)
    }

    suspend fun completeRound(
        round: RoundEntity,
        hakemCollectedPoints: Int
    ) {
        val hakemTeam = if (round.hakemTeam == 1) Team.TEAM_1 else Team.TEAM_2
        val calculated = ScoringEngine.computeTeamScores(hakemTeam, round.bidAmount, hakemCollectedPoints)
        val opponentPoints = 165 - hakemCollectedPoints

        val (team1Raw, team2Raw) = when (hakemTeam) {
            Team.TEAM_1 -> Pair(hakemCollectedPoints, opponentPoints)
            Team.TEAM_2 -> Pair(opponentPoints, hakemCollectedPoints)
        }

        val updatedRound = round.copy(
            team1CollectedRaw = team1Raw,
            team2CollectedRaw = team2Raw,
            team1FinalScore = calculated.team1FinalScore,
            team2FinalScore = calculated.team2FinalScore,
            status = calculated.result::class.java.simpleName,
            isCompleted = true
        )

        dao.updateRound(updatedRound)

        // Check if game should be finished
        val allRounds = dao.getRoundsForGameOnce(round.gameId)
        val game = dao.getGameByIdOnce(round.gameId)
        if (game != null) {
            val totalTeam1 = allRounds.filter { it.isCompleted }.sumOf { it.team1FinalScore }
            val totalTeam2 = allRounds.filter { it.isCompleted }.sumOf { it.team2FinalScore }
            if (totalTeam1 >= game.targetScore || totalTeam2 >= game.targetScore) {
                dao.updateGame(game.copy(isFinished = true))
            }
        }
    }

    suspend fun addRound(
        gameId: Long,
        currentRoundCount: Int,
        hakemTeam: Team,
        hakemPlayerName: String,
        dealerPlayerName: String,
        bidAmount: Int,
        isNaras: Boolean,
        hakemCollectedPoints: Int
    ): Long {
        val calculated = ScoringEngine.computeTeamScores(hakemTeam, bidAmount, hakemCollectedPoints)
        val opponentPoints = 165 - hakemCollectedPoints

        val (team1Raw, team2Raw) = when (hakemTeam) {
            Team.TEAM_1 -> Pair(hakemCollectedPoints, opponentPoints)
            Team.TEAM_2 -> Pair(opponentPoints, hakemCollectedPoints)
        }

        val round = RoundEntity(
            gameId = gameId,
            roundNumber = currentRoundCount + 1,
            hakemTeam = if (hakemTeam == Team.TEAM_1) 1 else 2,
            hakemPlayerName = hakemPlayerName,
            dealerPlayerName = dealerPlayerName,
            bidAmount = bidAmount,
            isNaras = isNaras,
            team1CollectedRaw = team1Raw,
            team2CollectedRaw = team2Raw,
            team1FinalScore = calculated.team1FinalScore,
            team2FinalScore = calculated.team2FinalScore,
            status = calculated.result::class.java.simpleName,
            isCompleted = true
        )

        val newRoundId = dao.insertRound(round)

        // Check if game should be finished
        val allRounds = dao.getRoundsForGameOnce(gameId)
        val game = dao.getGameByIdOnce(gameId)
        if (game != null) {
            val totalTeam1 = allRounds.filter { it.isCompleted }.sumOf { it.team1FinalScore }
            val totalTeam2 = allRounds.filter { it.isCompleted }.sumOf { it.team2FinalScore }
            if (totalTeam1 >= game.targetScore || totalTeam2 >= game.targetScore) {
                dao.updateGame(game.copy(isFinished = true))
            }
        }

        return newRoundId
    }

    suspend fun deleteRound(round: RoundEntity) {
        dao.deleteRound(round)
        // Recalculate game finished state
        val game = dao.getGameByIdOnce(round.gameId)
        if (game != null && game.isFinished) {
            val remainingRounds = dao.getRoundsForGameOnce(round.gameId)
            val totalTeam1 = remainingRounds.sumOf { it.team1FinalScore }
            val totalTeam2 = remainingRounds.sumOf { it.team2FinalScore }
            if (totalTeam1 < game.targetScore && totalTeam2 < game.targetScore) {
                dao.updateGame(game.copy(isFinished = false))
            }
        }
    }

    suspend fun deleteGame(gameId: Long) {
        dao.deleteGameById(gameId)
    }

    suspend fun toggleGameFinished(gameId: Long) {
        val game = dao.getGameByIdOnce(gameId)
        if (game != null) {
            dao.updateGame(game.copy(isFinished = !game.isFinished))
        }
    }
}
