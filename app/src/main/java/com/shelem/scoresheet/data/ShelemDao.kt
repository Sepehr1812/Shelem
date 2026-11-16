package com.shelem.scoresheet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelemDao {

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Transaction
    @Query("SELECT * FROM games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getOngoingGamesWithRounds(): Flow<List<GameWithRounds>>

    @Transaction
    @Query("SELECT * FROM games WHERE isFinished = 1 ORDER BY createdAt DESC")
    fun getFinishedGamesWithRounds(): Flow<List<GameWithRounds>>

    @Query("SELECT * FROM games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getOngoingGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFinished = 1 ORDER BY createdAt DESC")
    fun getFinishedGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameById(gameId: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameByIdOnce(gameId: Long): GameEntity?

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber DESC")
    fun getRoundsForGame(gameId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGameOnce(gameId: Long): List<RoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Delete
    suspend fun deleteRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE id = :roundId")
    suspend fun deleteRoundById(roundId: Long)
}
