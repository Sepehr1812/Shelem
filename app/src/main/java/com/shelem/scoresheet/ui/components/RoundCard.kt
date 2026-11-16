package com.shelem.scoresheet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shelem.scoresheet.R
import com.shelem.scoresheet.data.RoundEntity
import com.shelem.scoresheet.ui.theme.EmeraldGreen
import com.shelem.scoresheet.ui.theme.EmeraldGreenContainer
import com.shelem.scoresheet.ui.theme.GoldAccent
import com.shelem.scoresheet.ui.theme.NarasPurple
import com.shelem.scoresheet.ui.theme.NarasPurpleContainer
import com.shelem.scoresheet.ui.theme.PersianRed
import com.shelem.scoresheet.ui.theme.PersianRedContainer
import com.shelem.scoresheet.ui.theme.ShelemTheme
import com.shelem.scoresheet.util.PersianNumberHelper.formatScore
import com.shelem.scoresheet.util.PersianNumberHelper.toPersianDigits
import kotlinx.coroutines.launch

@Composable
fun RoundCard(
    round: RoundEntity,
    team1Name: String,
    team2Name: String,
    onDeleteRound: () -> Unit,
    onDeclareScore: (RoundEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isOngoing = !round.isCompleted || round.status == "Ongoing"

    val density = LocalDensity.current
    val initialOffsetY = with(density) { 36.dp.toPx() }

    val offsetY = remember { Animatable(initialOffsetY) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.95f) }

    LaunchedEffect(round.id) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Smooth animated highlight for ongoing round
    val borderStroke = if (isOngoing) {
        val infiniteTransition = rememberInfiniteTransition(label = "ongoing_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
        BorderStroke(2.dp, GoldAccent.copy(alpha = glowAlpha))
    } else {
        null
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .fillMaxWidth()
            .testTag("round_card_${round.id}"),
        shape = RoundedCornerShape(16.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOngoing) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Round Number, Status Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isOngoing) GoldAccent else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.round_number, round.roundNumber.toPersianDigits()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOngoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    StatusPill(status = round.status, isOngoing = isOngoing)

                    if (round.isNaras) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = NarasPurpleContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NarasPurple.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = stringResource(R.string.naras_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NarasPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (round.hasAceInGround) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GoldAccent.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("ace_badge_${round.id}")
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_spade),
                                contentDescription = stringResource(R.string.track_ace_in_ground_short_badge),
                                tint = GoldAccent,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteRound,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_round_${round.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_round),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Round Details: Hakem & Dealer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.hakem_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = round.hakemPlayerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.dealer_prefix),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = round.dealerPlayerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            append(stringResource(R.string.bid_prefix).trimEnd())
                            append(" ")
                        }
                        withStyle(
                            MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append(round.bidAmount.toPersianDigits())
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isOngoing) {
                // Ongoing Round Action Button
                Button(
                    onClick = { onDeclareScore(round) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("declare_scores_button_${round.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.declare_round_score),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Completed Round Scores Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team 1
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = team1Name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatScore(round.team1FinalScore),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    round.team1FinalScore > 0 -> EmeraldGreen
                                    round.team1FinalScore < 0 -> PersianRed
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )
                            Text(
                                text = " (${round.team1CollectedRaw.toPersianDigits()})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Team 2
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = team2Name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = " (${round.team2CollectedRaw.toPersianDigits()})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatScore(round.team2FinalScore),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    round.team2FinalScore > 0 -> EmeraldGreen
                                    round.team2FinalScore < 0 -> PersianRed
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String, isOngoing: Boolean) {
    val ongoingText = stringResource(R.string.status_ongoing)
    val shelemText = stringResource(R.string.status_shelem_card)
    val negativeShelemText = stringResource(R.string.status_negative_shelem_card)
    val bidAccomplishedText = stringResource(R.string.status_bid_accomplished)
    val bidFailedText = stringResource(R.string.status_bid_failed)

    val (text, bgColor, textColor) = when {
        isOngoing || status == "Ongoing" -> Triple(ongoingText, GoldAccent.copy(alpha = 0.2f), GoldAccent)
        status == "Shelem" -> Triple(shelemText, EmeraldGreenContainer, EmeraldGreen)
        status == "NegativeShelem" -> Triple(negativeShelemText, PersianRedContainer, PersianRed)
        status == "BidAccomplished" -> Triple(bidAccomplishedText, EmeraldGreenContainer, EmeraldGreen)
        status == "BidFailed" -> Triple(bidFailedText, PersianRedContainer, PersianRed)
        else -> Triple(status, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Preview(name = "RoundCard Light", showBackground = true)
@Composable
fun RoundCardLightPreview() {
    ShelemTheme(darkTheme = false) {
        RoundCard(
            round = RoundEntity(
                id = 1,
                gameId = 1,
                roundNumber = 3,
                hakemTeam = 1,
                hakemPlayerName = "سپهر",
                dealerPlayerName = "آرش",
                bidAmount = 140,
                isNaras = false,
                team1CollectedRaw = 145,
                team2CollectedRaw = 20,
                team1FinalScore = 145,
                team2FinalScore = 20,
                status = "BidAccomplished",
                isCompleted = true
            ),
            team1Name = "تیم سپهر",
            team2Name = "تیم آرش",
            onDeleteRound = {}
        )
    }
}
