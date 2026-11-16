package com.shelem.scoresheet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shelem.scoresheet.data.AppDatabase
import com.shelem.scoresheet.data.GameEntity
import com.shelem.scoresheet.data.GameWithRounds
import com.shelem.scoresheet.data.RoundEntity
import com.shelem.scoresheet.data.ShelemRepository
import com.shelem.scoresheet.domain.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShelemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShelemRepository

    init {
        val dao = AppDatabase.getDatabase(application).shelemDao()
        repository = ShelemRepository(dao)
    }

    private val _darkThemeEnabled = MutableStateFlow(true)
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled.asStateFlow()

    fun toggleDarkTheme() {
        _darkThemeEnabled.value = !_darkThemeEnabled.value
    }

    val ongoingGames: StateFlow<List<GameEntity>> = repository.ongoingGames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val finishedGames: StateFlow<List<GameEntity>> = repository.finishedGames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val ongoingGamesWithRounds: StateFlow<List<GameWithRounds>> = repository.ongoingGamesWithRounds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val finishedGamesWithRounds: StateFlow<List<GameWithRounds>> = repository.finishedGamesWithRounds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeGameId = MutableStateFlow<Long?>(null)
    val activeGameId: StateFlow<Long?> = _activeGameId.asStateFlow()

    val activeGame: StateFlow<GameEntity?> = _activeGameId
        .flatMapLatest { id ->
            if (id != null) repository.getGame(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activeRounds: StateFlow<List<RoundEntity>> = _activeGameId
        .flatMapLatest { id ->
            if (id != null) repository.getRounds(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectActiveGame(gameId: Long) {
        _activeGameId.value = gameId
    }

    fun createGame(
        team1Name: String,
        team2Name: String,
        team1Player1: String,
        team1Player2: String,
        team2Player1: String,
        team2Player2: String,
        targetScore: Int,
        winHandOnlyOver1000: Boolean = true,
        trackAceInGround: Boolean = false,
        initialDealerSeat: Int = 0,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newGameId = repository.createGame(
                team1Name = team1Name,
                team2Name = team2Name,
                team1Player1 = team1Player1,
                team1Player2 = team1Player2,
                team2Player1 = team2Player1,
                team2Player2 = team2Player2,
                targetScore = targetScore,
                winHandOnlyOver1000 = winHandOnlyOver1000,
                trackAceInGround = trackAceInGround,
                initialDealerSeat = initialDealerSeat
            )
            _activeGameId.value = newGameId
            onCreated(newGameId)
        }
    }

    fun createOngoingRound(
        hakemTeam: Team,
        hakemPlayerName: String,
        dealerPlayerName: String,
        bidAmount: Int,
        isNaras: Boolean,
        onSuccess: () -> Unit
    ) {
        val gameId = _activeGameId.value ?: return
        val currentRoundCount = activeRounds.value.size
        viewModelScope.launch {
            repository.createOngoingRound(
                gameId = gameId,
                currentRoundCount = currentRoundCount,
                hakemTeam = hakemTeam,
                hakemPlayerName = hakemPlayerName,
                dealerPlayerName = dealerPlayerName,
                bidAmount = bidAmount,
                isNaras = isNaras
            )
            onSuccess()
        }
    }

    fun completeRound(
        round: RoundEntity,
        hakemCollectedPoints: Int,
        hasAceInGround: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeRound(round, hakemCollectedPoints, hasAceInGround)
            onSuccess()
        }
    }

    fun addRound(
        hakemTeam: Team,
        hakemPlayerName: String,
        dealerPlayerName: String,
        bidAmount: Int,
        isNaras: Boolean,
        hakemCollectedPoints: Int,
        hasAceInGround: Boolean = false,
        onSuccess: () -> Unit
    ) {
        val gameId = _activeGameId.value ?: return
        val currentRoundCount = activeRounds.value.size
        viewModelScope.launch {
            repository.addRound(
                gameId = gameId,
                currentRoundCount = currentRoundCount,
                hakemTeam = hakemTeam,
                hakemPlayerName = hakemPlayerName,
                dealerPlayerName = dealerPlayerName,
                bidAmount = bidAmount,
                isNaras = isNaras,
                hakemCollectedPoints = hakemCollectedPoints,
                hasAceInGround = hasAceInGround
            )
            onSuccess()
        }
    }

    fun deleteRound(round: RoundEntity) {
        viewModelScope.launch {
            repository.deleteRound(round)
        }
    }

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            if (_activeGameId.value == gameId) {
                _activeGameId.value = null
            }
            repository.deleteGame(gameId)
        }
    }

    fun toggleGameFinished(gameId: Long) {
        viewModelScope.launch {
            repository.toggleGameFinished(gameId)
        }
    }
}
