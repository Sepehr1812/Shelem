package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.FeltGreenDark
import com.example.ui.theme.FeltGreenLight
import com.example.ui.theme.FeltGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.ShelemViewModel
import com.example.util.PersianNumberHelper.toPersianDigits
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    var team1Name by rememberSaveable { mutableStateOf(defaultTeam1) }
    var team1Player1 by rememberSaveable { mutableStateOf(defaultT1P1) } // Bottom seat ("شما")
    var team1Player2 by rememberSaveable { mutableStateOf(defaultT1P2) } // Top seat (Opposite teammate)

    var team2Name by rememberSaveable { mutableStateOf(defaultTeam2) }
    var team2Player1 by rememberSaveable { mutableStateOf(defaultT2P1) } // Right seat
    var team2Player2 by rememberSaveable { mutableStateOf(defaultT2P2) } // Left seat

    // Seat index in counter-clockwise order:
    // 0 = Bottom (team1Player1 - "شما")
    // 1 = Right (team2Player1)
    // 2 = Top (team1Player2)
    // 3 = Left (team2Player2)
    var initialDealerSeat by rememberSaveable { mutableIntStateOf(0) }

    var targetScoreFloat by rememberSaveable { mutableFloatStateOf(1165f) }
    val targetScore = targetScoreFloat.roundToInt()

    var winHandOnlyOver1000 by rememberSaveable { mutableStateOf(true) }
    var trackAceInGround by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = currentStep > 1) {
        currentStep -= 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentStep) {
                                1 -> stringResource(R.string.setup_step_1_title)
                                2 -> stringResource(R.string.setup_step_2_title)
                                else -> stringResource(R.string.setup_step_3_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                R.string.setup_step_indicator,
                                currentStep.toPersianDigits()
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep -= 1
                            } else {
                                onBack()
                            }
                        },
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
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (currentStep < 3) {
                        Button(
                            onClick = { currentStep += 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("next_step_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentStep == 1) {
                                        stringResource(R.string.next_step_table)
                                    } else {
                                        stringResource(R.string.next_step_rules)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                    } else {
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
                                    winHandOnlyOver1000 = winHandOnlyOver1000,
                                    trackAceInGround = trackAceInGround,
                                    initialDealerSeat = initialDealerSeat,
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
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Step Progress Bar
            SetupStepProgressHeader(
                currentStep = currentStep,
                onSelectStep1 = { currentStep = 1 },
                onSelectStep2 = { currentStep = 2 },
                onSelectStep3 = { currentStep = 3 }
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> width / 3 } + fadeOut())
                    }
                },
                label = "setup_step_transition"
            ) { step ->
                when (step) {
                    1 -> Step1TeamsAndPlayersContent(
                        team1Name = team1Name,
                        onTeam1NameChange = { team1Name = it },
                        team1Player1 = team1Player1,
                        onTeam1Player1Change = { team1Player1 = it },
                        team1Player2 = team1Player2,
                        onTeam1Player2Change = { team1Player2 = it },
                        team2Name = team2Name,
                        onTeam2NameChange = { team2Name = it },
                        team2Player1 = team2Player1,
                        onTeam2Player1Change = { team2Player1 = it },
                        team2Player2 = team2Player2,
                        onTeam2Player2Change = { team2Player2 = it }
                    )
                    2 -> Step2TableAndDealerContent(
                        team1Name = team1Name,
                        team1Player1 = team1Player1,
                        team1Player2 = team1Player2,
                        team2Name = team2Name,
                        team2Player1 = team2Player1,
                        team2Player2 = team2Player2,
                        initialDealerSeat = initialDealerSeat,
                        onSelectDealerSeat = { initialDealerSeat = it },
                        onSwapOpponents = {
                            val temp = team2Player1
                            team2Player1 = team2Player2
                            team2Player2 = temp
                            if (initialDealerSeat == 1) {
                                initialDealerSeat = 3
                            } else if (initialDealerSeat == 3) {
                                initialDealerSeat = 1
                            }
                        }
                    )
                    else -> Step3TargetAndRulesContent(
                        targetScore = targetScore,
                        targetScoreFloat = targetScoreFloat,
                        onTargetScoreChange = { targetScoreFloat = it },
                        winHandOnlyOver1000 = winHandOnlyOver1000,
                        onWinHandOnlyOver1000Change = { winHandOnlyOver1000 = it },
                        trackAceInGround = trackAceInGround,
                        onTrackAceInGroundChange = { trackAceInGround = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupStepProgressHeader(
    currentStep: Int,
    onSelectStep1: () -> Unit,
    onSelectStep2: () -> Unit,
    onSelectStep3: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepPill(
            stepNumber = 1,
            title = stringResource(R.string.setup_step_1_title),
            isActive = currentStep == 1,
            isCompleted = currentStep > 1,
            onClick = onSelectStep1,
            modifier = Modifier
                .weight(1f)
                .testTag("step_1_indicator")
        )
        StepPill(
            stepNumber = 2,
            title = stringResource(R.string.setup_step_2_title),
            isActive = currentStep == 2,
            isCompleted = currentStep > 2,
            onClick = onSelectStep2,
            modifier = Modifier
                .weight(1f)
                .testTag("step_2_indicator")
        )
        StepPill(
            stepNumber = 3,
            title = stringResource(R.string.setup_step_3_title),
            isActive = currentStep == 3,
            isCompleted = false,
            onClick = onSelectStep3,
            modifier = Modifier
                .weight(1f)
                .testTag("step_3_indicator")
        )
    }
}

@Composable
private fun StepPill(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isActive -> MaterialTheme.colorScheme.primaryContainer
        isCompleted -> EmeraldGreen.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val borderColor = when {
        isActive -> GoldAccent
        isCompleted -> EmeraldGreen
        else -> Color.Transparent
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = when {
                            isActive -> GoldAccent
                            isCompleted -> EmeraldGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toPersianDigits(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) FeltGreenDark else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Step1TeamsAndPlayersContent(
    team1Name: String,
    onTeam1NameChange: (String) -> Unit,
    team1Player1: String,
    onTeam1Player1Change: (String) -> Unit,
    team1Player2: String,
    onTeam1Player2Change: (String) -> Unit,
    team2Name: String,
    onTeam2NameChange: (String) -> Unit,
    team2Player1: String,
    onTeam2Player1Change: (String) -> Unit,
    team2Player2: String,
    onTeam2Player2Change: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Team 1 Card (You & Teammate)
        TeamSetupCard(
            title = stringResource(R.string.team1_label),
            teamName = team1Name,
            onTeamNameChange = onTeam1NameChange,
            player1 = team1Player1,
            onPlayer1Change = onTeam1Player1Change,
            player1Hint = stringResource(R.string.player_you_hint),
            player2 = team1Player2,
            onPlayer2Change = onTeam1Player2Change,
            player2Hint = stringResource(R.string.player_teammate_hint),
            tagPrefix = "team1"
        )

        // Team 2 Card (Right & Left Opponents)
        TeamSetupCard(
            title = stringResource(R.string.team2_label),
            teamName = team2Name,
            onTeamNameChange = onTeam2NameChange,
            player1 = team2Player1,
            onPlayer1Change = onTeam2Player1Change,
            player1Hint = stringResource(R.string.player_right_hint),
            player2 = team2Player2,
            onPlayer2Change = onTeam2Player2Change,
            player2Hint = stringResource(R.string.player_left_hint),
            tagPrefix = "team2"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2TableAndDealerContent(
    team1Name: String,
    team1Player1: String,
    team1Player2: String,
    team2Name: String,
    team2Player1: String,
    team2Player2: String,
    initialDealerSeat: Int,
    onSelectDealerSeat: (Int) -> Unit,
    onSwapOpponents: () -> Unit
) {
    val seatPlayers = listOf(
        team1Player1.ifBlank { stringResource(R.string.team1_player1_default) },
        team2Player1.ifBlank { stringResource(R.string.team2_player1_default) },
        team1Player2.ifBlank { stringResource(R.string.team1_player2_default) },
        team2Player2.ifBlank { stringResource(R.string.team2_player2_default) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overhead Table Visual Card
        OverheadTableCard(
            bottomPlayerName = seatPlayers[0],
            rightPlayerName = seatPlayers[1],
            topPlayerName = seatPlayers[2],
            leftPlayerName = seatPlayers[3],
            team1Name = team1Name.ifBlank { stringResource(R.string.team1_default_name) },
            team2Name = team2Name.ifBlank { stringResource(R.string.team2_default_name) },
            initialDealerSeat = initialDealerSeat,
            onSelectDealerSeat = onSelectDealerSeat,
            onSwapOpponents = onSwapOpponents
        )

        // First Dealer Quick Selection Card
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
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.select_first_dealer_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.select_first_dealer_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    seatPlayers.forEachIndexed { seatIndex, playerName ->
                        val isYou = seatIndex == 0
                        val chipLabel = if (isYou) {
                            "$playerName (${stringResource(R.string.you_short_label)})"
                        } else {
                            playerName
                        }
                        FilterChip(
                            selected = initialDealerSeat == seatIndex,
                            onClick = { onSelectDealerSeat(seatIndex) },
                            label = {
                                Text(
                                    text = "🃏 $chipLabel",
                                    fontWeight = if (initialDealerSeat == seatIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = FeltGreenDark
                            ),
                            modifier = Modifier.testTag("initial_dealer_chip_$seatIndex")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OverheadTableCard(
    bottomPlayerName: String,
    rightPlayerName: String,
    topPlayerName: String,
    leftPlayerName: String,
    team1Name: String,
    team2Name: String,
    initialDealerSeat: Int,
    onSelectDealerSeat: (Int) -> Unit,
    onSwapOpponents: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overhead_table_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.table_seating_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.table_seating_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onSwapOpponents,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("swap_opponents_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.swap_opponents_seats),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Force LTR inside the physical table container so Left/Right seats stay on physical Left/Right
            // regardless of Persian RTL or English LTR locale.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FeltGreenLight,
                                    FeltGreenPrimary,
                                    FeltGreenDark
                                )
                            )
                        )
                        .border(2.dp, GoldAccent.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        .padding(10.dp)
                ) {
                    // Decorative Table Felt Lines & Counter-Clockwise Rotation Arrows
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val rx = size.width * 0.24f
                        val ry = size.height * 0.22f

                        // Inner dashed ring
                        drawOval(
                            color = GoldAccent.copy(alpha = 0.28f),
                            topLeft = Offset(cx - rx * 1.25f, cy - ry * 1.25f),
                            size = Size(rx * 2.5f, ry * 2.5f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                            )
                        )

                        // Draw 4 counter-clockwise curved arrows between seats
                        // Screen coordinates: 0 deg = Right, 90 deg = Bottom, 180 deg = Left, 270 deg = Top
                        // Counter-clockwise on screen means decreasing angle:
                        // Bottom (90°) -> Right (0°) -> Top (270°) -> Left (180°) -> Bottom (90°)
                        val arcAngles = listOf(
                            Pair(65f, -40f),   // Bottom -> Right
                            Pair(335f, -40f),  // Right -> Top
                            Pair(245f, -40f),  // Top -> Left
                            Pair(155f, -40f)   // Left -> Bottom
                        )

                        arcAngles.forEach { (startDeg, sweepDeg) ->
                            drawArc(
                                color = GoldAccent.copy(alpha = 0.65f),
                                startAngle = startDeg,
                                sweepAngle = sweepDeg,
                                useCenter = false,
                                topLeft = Offset(cx - rx, cy - ry),
                                size = Size(rx * 2f, ry * 2f),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Arrowhead at the end of each arc
                            val endRad = Math.toRadians((startDeg + sweepDeg).toDouble())
                            val tipX = cx + rx * cos(endRad).toFloat()
                            val tipY = cy + ry * sin(endRad).toFloat()

                            // Tangent direction for decreasing angle
                            val tangentAngle = endRad - Math.PI / 2.0
                            val headLen = 7.dp.toPx()
                            val wing1 = tangentAngle + Math.toRadians(150.0)
                            val wing2 = tangentAngle - Math.toRadians(150.0)

                            val path = Path().apply {
                                moveTo(tipX, tipY)
                                lineTo(
                                    tipX + headLen * cos(wing1).toFloat(),
                                    tipY + headLen * sin(wing1).toFloat()
                                )
                                moveTo(tipX, tipY)
                                lineTo(
                                    tipX + headLen * cos(wing2).toFloat(),
                                    tipY + headLen * sin(wing2).toFloat()
                                )
                            }
                            drawPath(
                                path = path,
                                color = GoldAccent.copy(alpha = 0.85f),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center Emblem (Counter-Clockwise Suit Indicator)
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "♠️ ♥️",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "↺",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                        Text(
                            text = "♣️ ♦️",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // TOP SEAT: Seat 2 (Teammate - Team 1 Player 2)
                    TablePlayerSeatNode(
                        playerName = topPlayerName,
                        teamBadge = team1Name,
                        positionSubtitle = stringResource(R.string.teammate_label),
                        isYou = false,
                        isDealer = initialDealerSeat == 2,
                        teamColor = EmeraldGreen,
                        onClick = { onSelectDealerSeat(2) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .testTag("table_seat_top")
                    )

                    // LEFT SEAT: Seat 3 (Left Opponent - Team 2 Player 2)
                    TablePlayerSeatNode(
                        playerName = leftPlayerName,
                        teamBadge = team2Name,
                        positionSubtitle = stringResource(R.string.left_opponent_label),
                        isYou = false,
                        isDealer = initialDealerSeat == 3,
                        teamColor = GoldAccent,
                        onClick = { onSelectDealerSeat(3) },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("table_seat_left")
                    )

                    // RIGHT SEAT: Seat 1 (Right Opponent - Team 2 Player 1)
                    TablePlayerSeatNode(
                        playerName = rightPlayerName,
                        teamBadge = team2Name,
                        positionSubtitle = stringResource(R.string.right_opponent_label),
                        isYou = false,
                        isDealer = initialDealerSeat == 1,
                        teamColor = GoldAccent,
                        onClick = { onSelectDealerSeat(1) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .testTag("table_seat_right")
                    )

                    // BOTTOM SEAT: Seat 0 (Scorer / "شما" - Team 1 Player 1)
                    TablePlayerSeatNode(
                        playerName = bottomPlayerName,
                        teamBadge = team1Name,
                        positionSubtitle = stringResource(R.string.you_short_label),
                        isYou = true,
                        isDealer = initialDealerSeat == 0,
                        teamColor = EmeraldGreen,
                        onClick = { onSelectDealerSeat(0) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("table_seat_bottom")
                    )
                }
            }
        }
    }
}

@Composable
private fun TablePlayerSeatNode(
    playerName: String,
    teamBadge: String,
    positionSubtitle: String,
    isYou: Boolean,
    isDealer: Boolean,
    teamColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = 96.dp, max = 132.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDealer) FeltGreenDark.copy(alpha = 0.92f)
                else FeltGreenDark.copy(alpha = 0.78f)
            )
            .border(
                width = if (isDealer) 2.dp else 1.dp,
                color = if (isDealer) GoldAccent else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dealer Badge if selected
        if (isDealer) {
            Surface(
                color = GoldAccent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.first_dealer_badge),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = FeltGreenDark,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }

        // Player Avatar + Name Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(teamColor.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, teamColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = playerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Subtitle ("شما" badge under the bottom player, or role label for others)
        if (isYou) {
            Surface(
                color = EmeraldGreen,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.you_short_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 1.dp)
                        .testTag("bottom_player_you_label")
                )
            }
        } else {
            Text(
                text = "$positionSubtitle ($teamBadge)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Step3TargetAndRulesContent(
    targetScore: Int,
    targetScoreFloat: Float,
    onTargetScoreChange: (Float) -> Unit,
    winHandOnlyOver1000: Boolean,
    onWinHandOnlyOver1000Change: (Boolean) -> Unit,
    trackAceInGround: Boolean,
    onTrackAceInGroundChange: (Boolean) -> Unit
) {
    val presetScores = listOf(625, 1165, 1200, 1650)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.target_score),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.target_score_explanation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(
                            R.string.score_points_value,
                            targetScore.toPersianDigits()
                        ),
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
                            onClick = { onTargetScoreChange(preset.toFloat()) },
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
                    onValueChange = onTargetScoreChange,
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

        // Optional Rules Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.game_rules_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.rule_1000_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.rule_1000_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = winHandOnlyOver1000,
                        onCheckedChange = onWinHandOnlyOver1000Change,
                        modifier = Modifier.testTag("rule_1000_switch")
                    )
                }
            }
        }

        // Ace in Ground Tracking Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_spade),
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.track_ace_in_ground_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.track_ace_in_ground_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.track_ace_in_ground_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = trackAceInGround,
                        onCheckedChange = onTrackAceInGroundChange,
                        modifier = Modifier.testTag("track_ace_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TeamSetupCard(
    title: String,
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    player1: String,
    onPlayer1Change: (String) -> Unit,
    player1Hint: String,
    player2: String,
    onPlayer2Change: (String) -> Unit,
    player2Hint: String,
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
                    label = { Text(player1Hint) },
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
                    label = { Text(player2Hint) },
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
