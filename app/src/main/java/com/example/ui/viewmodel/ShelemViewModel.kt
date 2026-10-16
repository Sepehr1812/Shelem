package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.data.GameWithRounds
import com.example.data.RoundEntity
import com.example.data.ShelemRepository
import com.example.domain.Team
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val response: String) : AiState()
    data class Error(val message: String) : AiState()
}

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

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

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
                winHandOnlyOver1000 = winHandOnlyOver1000
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
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeRound(round, hakemCollectedPoints)
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
                hakemCollectedPoints = hakemCollectedPoints
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

    fun askAiStrategist(customPrompt: String? = null) {
        viewModelScope.launch {
            _aiState.value = AiState.Loading

            val game = activeGame.value
            val rounds = activeRounds.value

            val isPersian = java.util.Locale.getDefault().language.startsWith("fa", ignoreCase = true)
            val promptText = if (customPrompt.isNullOrBlank()) {
                if (game != null) {
                    val t1Total = rounds.sumOf { it.team1FinalScore }
                    val t2Total = rounds.sumOf { it.team2FinalScore }
                    val lastRound = rounds.firstOrNull()

                    if (isPersian) {
                        val lastRoundInfo = if (lastRound != null) {
                            "دست قبلی: حاکم=${lastRound.hakemPlayerName}، بید=${lastRound.bidAmount.toPersianDigits()}، امتیاز نهایی تیم ۱=${lastRound.team1FinalScore.toPersianDigits()}، تیم ۲=${lastRound.team2FinalScore.toPersianDigits()} (${lastRound.status})"
                        } else "هنوز دستی بازی نشده است."

                        """
                        تحلیل سریع بازی جاری شلم:
                        - نام تیم ۱: ${game.team1Name} (بازیکنان: ${game.team1Player1Name}, ${game.team1Player2Name}) - امتیاز کل: ${t1Total.toPersianDigits()}
                        - نام تیم ۲: ${game.team2Name} (بازیکنان: ${game.team2Player1Name}, ${game.team2Player2Name}) - امتیاز کل: ${t2Total.toPersianDigits()}
                        - امتیاز هدف بازی: ${game.targetScore.toPersianDigits()}
                        - تعداد دست‌های بازی شده: ${rounds.size.toPersianDigits()}
                        - $lastRoundInfo

                        لطفاً وضعیت فعلی بازی را تحلیل کنید، پیشنهاد بدهید کدام تیم نیاز به ریسک دارد و استراتژی پیشنهاد بیدینگ (خوانده) بعدی چیست.
                        """.trimIndent()
                    } else {
                        val lastRoundInfo = if (lastRound != null) {
                            "Previous round: Hakem=${lastRound.hakemPlayerName}, Bid=${lastRound.bidAmount}, Final score Team 1=${lastRound.team1FinalScore}, Team 2=${lastRound.team2FinalScore} (${lastRound.status})"
                        } else "No rounds played yet."

                        """
                        Quick Shelem match analysis:
                        - Team 1: ${game.team1Name} (Players: ${game.team1Player1Name}, ${game.team1Player2Name}) - Total Score: $t1Total
                        - Team 2: ${game.team2Name} (Players: ${game.team2Player1Name}, ${game.team2Player2Name}) - Total Score: $t2Total
                        - Target Score: ${game.targetScore}
                        - Completed Rounds: ${rounds.size}
                        - $lastRoundInfo

                        Please analyze the current match state, advise which team needs to take risks, and provide recommended bidding and trump strategy for the next round.
                        """.trimIndent()
                    }
                } else {
                    if (isPersian) {
                        "لطفاً استراتژی عمومی خواندن (بیدینگ)، انتخاب حکم، و بازی شلم را توضیح دهید."
                    } else {
                        "Please explain general Shelem strategies including bidding, trump suit selection, and high-level card play tactics."
                    }
                }
            } else {
                customPrompt
            }

            val result = GeminiClient.analyzeGameOrStrategy(promptText)
            _aiState.value = AiState.Success(result)
        }
    }

    fun resetAiState() {
        _aiState.value = AiState.Idle
    }
}
