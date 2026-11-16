package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.GameEntity
import com.example.ui.components.GameCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.example.util.LocaleHelper
import com.example.ui.theme.FeltGreenDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ShelemTheme
import com.example.ui.viewmodel.ShelemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShelemViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToBoard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ongoingGamesWithRounds by viewModel.ongoingGamesWithRounds.collectAsStateWithLifecycle()
    val finishedGamesWithRounds by viewModel.finishedGamesWithRounds.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.darkThemeEnabled.collectAsStateWithLifecycle()

    var gameToDeleteId by remember { mutableStateOf<Long?>(null) }

    if (gameToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { gameToDeleteId = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_game),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.confirm_delete_game))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameToDeleteId?.let { id ->
                            viewModel.deleteGame(id)
                        }
                        gameToDeleteId = null
                    },
                    modifier = Modifier.testTag("confirm_delete_game_button")
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { gameToDeleteId = null },
                    modifier = Modifier.testTag("cancel_delete_game_button")
                ) {
                    Text(text = stringResource(R.string.no))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "♠️ ",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " 👑",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    val isPersian = LocaleHelper.isPersian(context)

                    IconButton(
                        onClick = {
                            LocaleHelper.toggleLanguage(context)
                        },
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = if (isPersian) "EN" else "فا",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(R.string.toggle_dark_mode)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Game Banner Button
            item {
                Button(
                    onClick = onNavigateToSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("new_game_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val iconBadgeBackground = if (isDarkTheme) FeltGreenDark else GoldAccent
                        val iconTint = if (isDarkTheme) GoldAccent else MaterialTheme.colorScheme.onPrimary

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(iconBadgeBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.new_game),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Ongoing Games Section
            item {
                Text(
                    text = stringResource(R.string.ongoing_games),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (ongoingGamesWithRounds.isEmpty()) {
                item {
                    EmptySectionCard(message = stringResource(R.string.no_ongoing_games))
                }
            } else {
                items(
                    items = ongoingGamesWithRounds,
                    key = { it.game.id }
                ) { gameWithRounds ->
                    GameCard(
                        game = gameWithRounds.game,
                        totalTeam1Score = gameWithRounds.team1TotalScore,
                        totalTeam2Score = gameWithRounds.team2TotalScore,
                        onClick = {
                            viewModel.selectActiveGame(gameWithRounds.game.id)
                            onNavigateToBoard(gameWithRounds.game.id)
                        },
                        onDelete = {
                            gameToDeleteId = gameWithRounds.game.id
                        }
                    )
                }
            }

            // Match History Section
            item {
                Text(
                    text = stringResource(R.string.match_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            if (finishedGamesWithRounds.isEmpty()) {
                item {
                    EmptySectionCard(message = stringResource(R.string.no_history_games))
                }
            } else {
                items(
                    items = finishedGamesWithRounds,
                    key = { it.game.id }
                ) { gameWithRounds ->
                    GameCard(
                        game = gameWithRounds.game,
                        totalTeam1Score = gameWithRounds.team1TotalScore,
                        totalTeam2Score = gameWithRounds.team2TotalScore,
                        onClick = {
                            viewModel.selectActiveGame(gameWithRounds.game.id)
                            onNavigateToBoard(gameWithRounds.game.id)
                        },
                        onDelete = {
                            gameToDeleteId = gameWithRounds.game.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
