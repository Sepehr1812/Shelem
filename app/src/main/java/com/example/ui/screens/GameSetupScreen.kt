package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.viewmodel.ShelemViewModel
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    viewModel: ShelemViewModel,
    onBack: () -> Unit,
    onGameCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultTeam1 = stringResource(R.string.team1_default_name)
    val defaultTeam2 = stringResource(R.string.team2_default_name)
    val defaultT1P1 = stringResource(R.string.team1_player1_default)
    val defaultT1P2 = stringResource(R.string.team1_player2_default)
    val defaultT2P1 = stringResource(R.string.team2_player1_default)
    val defaultT2P2 = stringResource(R.string.team2_player2_default)

    var team1Name by remember { mutableStateOf(defaultTeam1) }
    var team1Player1 by remember { mutableStateOf(defaultT1P1) }
    var team1Player2 by remember { mutableStateOf(defaultT1P2) }

    var team2Name by remember { mutableStateOf(defaultTeam2) }
    var team2Player1 by remember { mutableStateOf(defaultT2P1) }
    var team2Player2 by remember { mutableStateOf(defaultT2P2) }

    var targetScoreFloat by remember { mutableFloatStateOf(1165f) }
    val targetScore = targetScoreFloat.roundToInt()

    val presetScores = listOf(625, 1165, 1200, 1650)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_game),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("setup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Team 1 Card
            TeamSetupCard(
                title = stringResource(R.string.team1_label),
                teamName = team1Name,
                onTeamNameChange = { team1Name = it },
                player1 = team1Player1,
                onPlayer1Change = { team1Player1 = it },
                player2 = team1Player2,
                onPlayer2Change = { team1Player2 = it },
                tagPrefix = "team1"
            )

            // Team 2 Card
            TeamSetupCard(
                title = stringResource(R.string.team2_label),
                teamName = team2Name,
                onTeamNameChange = { team2Name = it },
                player1 = team2Player1,
                onPlayer1Change = { team2Player1 = it },
                player2 = team2Player2,
                onPlayer2Change = { team2Player2 = it },
                tagPrefix = "team2"
            )

            // Target Score Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.target_score),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.score_points_value, targetScore.toPersianDigits()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetScores.forEach { preset ->
                            FilterChip(
                                selected = targetScore == preset,
                                onClick = { targetScoreFloat = preset.toFloat() },
                                label = { Text(preset.toPersianDigits()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("preset_chip_$preset")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = targetScoreFloat,
                        onValueChange = { targetScoreFloat = it },
                        valueRange = 500f..2500f,
                        steps = 39, // steps of 50
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("target_score_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Start Game Button
            Button(
                onClick = {
                    viewModel.createGame(
                        team1Name = team1Name,
                        team2Name = team2Name,
                        team1Player1 = team1Player1,
                        team1Player2 = team1Player2,
                        team2Player1 = team2Player1,
                        team2Player2 = team2Player2,
                        targetScore = targetScore,
                        onCreated = onGameCreated
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_game_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.start_game),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TeamSetupCard(
    title: String,
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    player1: String,
    onPlayer1Change: (String) -> Unit,
    player2: String,
    onPlayer2Change: (String) -> Unit,
    tagPrefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = teamName,
                onValueChange = onTeamNameChange,
                label = { Text(stringResource(R.string.team_name_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${tagPrefix}_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = player1,
                    onValueChange = onPlayer1Change,
                    label = { Text(stringResource(R.string.player_1_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${tagPrefix}_player1_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = player2,
                    onValueChange = onPlayer2Change,
                    label = { Text(stringResource(R.string.player_2_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${tagPrefix}_player2_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
