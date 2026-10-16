package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.GameEntity
import com.example.data.RoundEntity
import com.example.ui.components.RoundCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PersianRed
import com.example.ui.viewmodel.ShelemViewModel
import com.example.util.PersianNumberHelper.formatScore
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoardScreen(
    viewModel: ShelemViewModel,
    onBack: () -> Unit,
    onOpenAddRoundSheet: () -> Unit,
    onOpenAiStrategist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val rounds by viewModel.activeRounds.collectAsStateWithLifecycle()

    val game = activeGame ?: return

    var activeRoundForScoreSheet by remember { mutableStateOf<RoundEntity?>(null) }
    var roundToDelete by remember { mutableStateOf<RoundEntity?>(null) }
    var showFinishGameConfirmation by remember { mutableStateOf(false) }
    var showResumeGameConfirmation by remember { mutableStateOf(false) }
    var showStatisticsSheet by remember { mutableStateOf(false) }

    val ongoingRound = rounds.find { !it.isCompleted || it.status == "Ongoing" }
    val listState = rememberLazyListState()

    LaunchedEffect(ongoingRound?.id, rounds.size) {
        if (rounds.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    val completedRounds = rounds.filter { it.isCompleted && it.status != "Ongoing" }

    val totalTeam1Score = completedRounds.sumOf { it.team1FinalScore }
    val totalTeam2Score = completedRounds.sumOf { it.team2FinalScore }

    val isFinished = game.isFinished || totalTeam1Score >= game.targetScore || totalTeam2Score >= game.targetScore
    val winnerName = when {
        totalTeam1Score >= game.targetScore || (isFinished && totalTeam1Score > totalTeam2Score) -> game.team1Name
        totalTeam2Score >= game.targetScore || (isFinished && totalTeam2Score > totalTeam1Score) -> game.team2Name
        else -> null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${game.team1Name} ${stringResource(R.string.versus)} ${game.team2Name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.target_score_label, game.targetScore.toPersianDigits()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("board_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showStatisticsSheet = true },
                        modifier = Modifier.testTag("game_statistics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.game_statistics_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isFinished) {
                                showResumeGameConfirmation = true
                            } else {
                                showFinishGameConfirmation = true
                            }
                        },
                        modifier = Modifier.testTag("toggle_game_finished_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = if (isFinished) stringResource(R.string.resume_game) else stringResource(R.string.end_game),
                            tint = if (isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!isFinished) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (ongoingRound != null) {
                            activeRoundForScoreSheet = ongoingRound
                        } else {
                            onOpenAddRoundSheet()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (ongoingRound != null) Icons.Default.AutoAwesome else Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = if (ongoingRound != null) stringResource(R.string.declare_round_score) else stringResource(R.string.add_new_round),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = if (ongoingRound != null) GoldAccent else MaterialTheme.colorScheme.primary,
                    contentColor = if (ongoingRound != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_round_fab")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sticky Score Header Card
            ScoreBoardHeader(
                game = game,
                totalTeam1Score = totalTeam1Score,
                totalTeam2Score = totalTeam2Score,
                winnerName = winnerName,
                isFinished = isFinished,
                onOpenStatistics = { showStatisticsSheet = true }
            )

            // Rounds List
            if (rounds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "♠️ ♥️ ♦️ ♣️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.no_rounds_recorded),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_rounds_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ongoing Round Section
                    if (ongoingRound != null) {
                        item(key = "ongoing_section") {
                            Column {
                                Text(
                                    text = stringResource(R.string.ongoing_round_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, end = 4.dp)
                                )
                                RoundCard(
                                    round = ongoingRound,
                                    team1Name = game.team1Name,
                                    team2Name = game.team2Name,
                                    onDeleteRound = { roundToDelete = ongoingRound },
                                    onDeclareScore = { round -> activeRoundForScoreSheet = round }
                                )
                            }
                        }
                    }

                    // Completed Rounds Section
                    if (completedRounds.isNotEmpty()) {
                        if (ongoingRound != null) {
                            item(key = "completed_header") {
                                Text(
                                    text = stringResource(R.string.completed_rounds_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                                )
                            }
                        }

                        items(
                            items = completedRounds,
                            key = { it.id }
                        ) { round ->
                            RoundCard(
                                round = round,
                                team1Name = game.team1Name,
                                team2Name = game.team2Name,
                                onDeleteRound = { roundToDelete = round }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Entering Scores of Ongoing Round
    if (activeRoundForScoreSheet != null) {
        DeclareScoreBottomSheet(
            round = activeRoundForScoreSheet!!,
            game = game,
            onDismiss = { activeRoundForScoreSheet = null },
            onConfirmScore = { roundToComplete, hakemPoints ->
                viewModel.completeRound(roundToComplete, hakemPoints) {
                    activeRoundForScoreSheet = null
                }
            }
        )
    }

    // Delete Round Confirmation Dialog
    if (roundToDelete != null) {
        AlertDialog(
            onDismissRequest = { roundToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_round),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.round_delete_confirm, roundToDelete!!.roundNumber.toPersianDigits())
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetRound = roundToDelete
                        roundToDelete = null
                        if (targetRound != null) {
                            viewModel.deleteRound(targetRound)
                        }
                    },
                    modifier = Modifier.testTag("confirm_delete_round_button")
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { roundToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_round_button")
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    // Game Statistics Bottom Sheet
    if (showStatisticsSheet) {
        GameStatisticsBottomSheet(
            game = game,
            rounds = rounds,
            onDismiss = { showStatisticsSheet = false }
        )
    }

    // Finish Game Confirmation Dialog
    if (showFinishGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishGameConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.finish_game),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.confirm_finish_game)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishGameConfirmation = false
                        viewModel.toggleGameFinished(game.id)
                    },
                    modifier = Modifier.testTag("confirm_finish_game_button")
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFinishGameConfirmation = false },
                    modifier = Modifier.testTag("cancel_finish_game_button")
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    // Resume Game Confirmation Dialog
    if (showResumeGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showResumeGameConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.resume_game),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.confirm_resume_game)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResumeGameConfirmation = false
                        viewModel.toggleGameFinished(game.id)
                    },
                    modifier = Modifier.testTag("confirm_resume_game_button")
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResumeGameConfirmation = false },
                    modifier = Modifier.testTag("cancel_resume_game_button")
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ScoreBoardHeader(
    game: GameEntity,
    totalTeam1Score: Int,
    totalTeam2Score: Int,
    winnerName: String?,
    isFinished: Boolean,
    onOpenStatistics: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isFinished && winnerName != null) {
                Surface(
                    color = GoldAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.winner_announcement, winnerName),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Teams Total Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 Total
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = game.team1Name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${game.team1Player1Name} & ${game.team1Player2Name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatScore(totalTeam1Score),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = when {
                            totalTeam1Score > totalTeam2Score -> EmeraldGreen
                            totalTeam1Score < 0 -> PersianRed
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }

                // Lead Margin Pill & Stats trigger
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenStatistics)
                        .testTag("header_statistics_badge")
                ) {
                    val margin = abs(totalTeam1Score - totalTeam2Score)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${stringResource(R.string.lead_difference_label)}\n${margin.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.game_statistics_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Team 2 Total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = game.team2Name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${game.team2Player1Name} & ${game.team2Player2Name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatScore(totalTeam2Score),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = when {
                            totalTeam2Score > totalTeam1Score -> EmeraldGreen
                            totalTeam2Score < 0 -> PersianRed
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress towards Target Score
            val progressTeam1 = (totalTeam1Score.coerceAtLeast(0).toFloat() / game.targetScore.toFloat()).coerceIn(0f, 1f)
            val progressTeam2 = (totalTeam2Score.coerceAtLeast(0).toFloat() / game.targetScore.toFloat()).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progressTeam1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { progressTeam2 },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}
