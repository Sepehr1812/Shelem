package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ShelemTheme
import com.example.util.PersianNumberHelper.toPersianDigits

@Composable
fun GameCard(
    game: GameEntity,
    totalTeam1Score: Int = 0,
    totalTeam2Score: Int = 0,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("game_card_${game.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (game.isFinished) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (game.isFinished) Icons.Default.EmojiEvents else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (game.isFinished) GoldAccent else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (game.isFinished) stringResource(R.string.game_status_finished) else stringResource(R.string.game_status_ongoing),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (game.isFinished) GoldAccent else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.target_score_card, game.targetScore.toPersianDigits()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_game_${game.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_game),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teams and Scores Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = game.team1Name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${game.team1Player1Name} - ${game.team1Player2Name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.example.util.PersianNumberHelper.formatScore(totalTeam1Score),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            totalTeam1Score > totalTeam2Score -> EmeraldGreen
                            totalTeam1Score < 0 -> com.example.ui.theme.PersianRed
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.versus),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Team 2
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = game.team2Name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${game.team2Player1Name} - ${game.team2Player2Name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.example.util.PersianNumberHelper.formatScore(totalTeam2Score),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            totalTeam2Score > totalTeam1Score -> EmeraldGreen
                            totalTeam2Score < 0 -> com.example.ui.theme.PersianRed
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (game.isFinished) stringResource(R.string.view_game_results) else stringResource(R.string.continue_scoring),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(name = "GameCard Light", showBackground = true)
@Composable
fun GameCardLightPreview() {
    ShelemTheme(darkTheme = false) {
        GameCard(
            game = GameEntity(
                id = 1,
                team1Name = "تیم سپهر",
                team2Name = "تیم آرش",
                team1Player1Name = "سپهر",
                team1Player2Name = "علی",
                team2Player1Name = "آرش",
                team2Player2Name = "رضا",
                targetScore = 1165
            ),
            totalTeam1Score = 850,
            totalTeam2Score = 620,
            onClick = {},
            onDelete = {}
        )
    }
}

@Preview(name = "GameCard Dark", showBackground = true)
@Composable
fun GameCardDarkPreview() {
    ShelemTheme(darkTheme = true) {
        GameCard(
            game = GameEntity(
                id = 2,
                team1Name = "تیم شاهین",
                team2Name = "تیم شیران",
                team1Player1Name = "شاهین",
                team1Player2Name = "امید",
                team2Player1Name = "کامران",
                team2Player2Name = "نوید",
                targetScore = 1165,
                isFinished = true
            ),
            totalTeam1Score = 1210,
            totalTeam2Score = 940,
            onClick = {},
            onDelete = {}
        )
    }
}
