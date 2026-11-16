package com.shelem.scoresheet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shelem.scoresheet.R
import com.shelem.scoresheet.data.GameEntity
import com.shelem.scoresheet.domain.Team
import com.shelem.scoresheet.ui.theme.GoldAccent
import com.shelem.scoresheet.util.PersianNumberHelper.toPersianDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRoundBottomSheet(
    game: GameEntity,
    previousDealerName: String? = null,
    onDismiss: () -> Unit,
    onConfirmBid: (
        hakemTeam: Team,
        hakemPlayerName: String,
        dealerPlayerName: String,
        bidAmount: Int,
        isNaras: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Counter-clockwise table seating rotation:
    // Seat 0 (Bottom / "شما"): Team1 P1
    // Seat 1 (Right): Team2 P1
    // Seat 2 (Top): Team1 P2
    // Seat 3 (Left): Team2 P2
    val players = remember(game) { game.getCounterClockwiseSeating() }

    // Dealer is automatically determined counter-clockwise based on table seating
    val currentDealerPair = remember(game, previousDealerName) {
        game.getCurrentDealer(previousDealerName)
    }
    val currentDealerPlayer = currentDealerPair.first
    val currentDealerTeamLabel = if (currentDealerPair.second == Team.TEAM_1) {
        game.team1Name
    } else {
        game.team2Name
    }

    var selectedHakemIndex by remember { mutableIntStateOf(0) }

    val selectedHakemPlayer = players[selectedHakemIndex].first
    val selectedHakemTeam = players[selectedHakemIndex].second

    var isShelemClaim by remember { mutableStateOf(false) }
    var isNaras by remember { mutableStateOf(false) }

    var bidAmountFloat by remember { mutableFloatStateOf(110f) }
    val bidAmount = if (isShelemClaim) 165 else bidAmountFloat.roundToInt()

    val quickBidPresets = listOf(110, 115, 120, 125, 130, 135, 140, 145)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.add_new_round),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // DEALER READ-ONLY DISPLAY (Auto-assigned counter-clockwise)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dealer_display_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🃏 ${stringResource(R.string.current_round_dealer_title)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("dealer_readonly_badge")
                        ) {
                            Text(
                                text = currentDealerPlayer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "($currentDealerTeamLabel)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // HAKEM SELECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "👑 ${stringResource(R.string.select_hakem_player)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            players.forEachIndexed { index, (name, team) ->
                                val teamLabel = if (team == Team.TEAM_1) game.team1Name else game.team2Name
                                FilterChip(
                                    selected = selectedHakemIndex == index,
                                    onClick = { selectedHakemIndex = index },
                                    label = { Text("$name ($teamLabel)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("hakem_chip_$index")
                                )
                            }
                        }
                    }
                }

                // BID AMOUNT SELECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.bid_amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isShelemClaim) stringResource(R.string.shelem_165) else bidAmount.toPersianDigits(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.shelem_claim), style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isShelemClaim,
                                onCheckedChange = {
                                    isShelemClaim = it
                                    if (!it) isNaras = false
                                },
                                modifier = Modifier.testTag("shelem_claim_switch")
                            )
                        }

                        AnimatedVisibility(visible = isShelemClaim) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.shelem_naras), style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = isNaras,
                                    onCheckedChange = { isNaras = it },
                                    modifier = Modifier.testTag("shelem_naras_switch")
                                )
                            }
                        }

                        if (!isShelemClaim) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickBidPresets.forEach { preset ->
                                    FilterChip(
                                        selected = bidAmount == preset,
                                        onClick = { bidAmountFloat = preset.toFloat() },
                                        label = { Text(preset.toPersianDigits()) },
                                        modifier = Modifier.testTag("bid_chip_$preset")
                                    )
                                }
                            }

                            Slider(
                                value = bidAmountFloat,
                                onValueChange = { bidAmountFloat = it },
                                valueRange = 100f..165f,
                                steps = 12,
                                modifier = Modifier.testTag("bid_slider")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            onConfirmBid(
                                selectedHakemTeam,
                                selectedHakemPlayer,
                                currentDealerPlayer,
                                bidAmount,
                                isNaras
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_round_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.start_round_with_bid),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
