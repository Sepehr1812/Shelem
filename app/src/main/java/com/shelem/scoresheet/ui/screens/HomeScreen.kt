package com.shelem.scoresheet.ui.screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shelem.scoresheet.R
import com.shelem.scoresheet.ui.components.GameCard
import com.shelem.scoresheet.ui.theme.FeltGreenDark
import com.shelem.scoresheet.ui.theme.FeltGreenPrimary
import com.shelem.scoresheet.ui.theme.GoldAccent
import com.shelem.scoresheet.ui.theme.TextPrimaryDark
import com.shelem.scoresheet.ui.viewmodel.ShelemViewModel
import com.shelem.scoresheet.util.LocaleHelper
import com.shelem.scoresheet.util.PersianNumberHelper.toPersianDigits
import kotlinx.coroutines.launch
import java.util.Locale

const val GITHUB_REPO_URL = "https://github.com/Sepehr1812/Shelem"
const val BUG_REPORT_RECIPIENT = "SepehrAkhoundi1812@gmail.com"

fun getAppVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }
}

fun buildBugReportEmailIntent(
    context: Context,
    isPersian: Boolean = LocaleHelper.isPersian(context)
): Intent {
    val targetLocale = if (isPersian) Locale("fa") else Locale.ENGLISH
    val localizedConfig = Configuration(context.resources.configuration).apply {
        setLocale(targetLocale)
    }
    val localizedContext = context.createConfigurationContext(localizedConfig)
    val rawVersion = getAppVersionName(context)
    val formattedVersion = if (isPersian) {
        rawVersion.map { ch ->
            when (ch) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> ch
            }
        }.joinToString("")
    } else {
        rawVersion
    }

    val subject = localizedContext.getString(R.string.bug_report_email_subject)
    val body = localizedContext.getString(R.string.bug_report_email_body, formattedVersion)

    val encodedSubject = Uri.encode(subject)
    val encodedBody = Uri.encode(body)
    val mailtoUri = Uri.parse("mailto:$BUG_REPORT_RECIPIENT?subject=$encodedSubject&body=$encodedBody")

    return Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(BUG_REPORT_RECIPIENT))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
}

fun openBugReportEmail(context: Context) {
    val emailIntent = buildBugReportEmailIntent(context)
    try {
        context.startActivity(emailIntent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, emailIntent.getStringArrayExtra(Intent.EXTRA_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, emailIntent.getStringExtra(Intent.EXTRA_SUBJECT))
                putExtra(Intent.EXTRA_TEXT, emailIntent.getStringExtra(Intent.EXTRA_TEXT))
            }
            context.startActivity(Intent.createChooser(fallbackIntent, null))
        } catch (_: Exception) {
        }
    }
}

fun openGitHubRepo(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL))
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

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

    val context = LocalContext.current
    val isPersian = LocaleHelper.isPersian(context)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var gameToDeleteId by remember { mutableStateOf<Long?>(null) }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

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

    val rawVersion = remember(context) { getAppVersionName(context) }
    val displayVersion = if (isPersian) rawVersion.toPersianDigits() else rawVersion

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .testTag("home_navigation_drawer"),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Sidebar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FeltGreenPrimary,
                                    FeltGreenDark
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "♠️",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                            Text(
                                text = "👑",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = stringResource(R.string.drawer_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryDark.copy(alpha = 0.92f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = stringResource(R.string.app_version_format, displayVersion),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Change Theme (Moved from AppBar)
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_toggle_theme),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(R.string.toggle_dark_mode),
                            tint = GoldAccent
                        )
                    },
                    badge = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = if (isDarkTheme) {
                                    stringResource(R.string.drawer_theme_dark)
                                } else {
                                    stringResource(R.string.drawer_theme_light)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        viewModel.toggleDarkTheme()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("theme_toggle_button")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Change Language (Moved from AppBar)
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_toggle_language),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.toggle_language),
                            tint = GoldAccent
                        )
                    },
                    badge = {
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        LocaleHelper.toggleLanguage(context)
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("language_toggle_button")
                )

                // Divider between the first 2 items and the remaining items
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .testTag("drawer_items_divider"),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )

                // 3. GitHub Repository
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_github_repo),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = stringResource(R.string.drawer_github_repo),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        openGitHubRepo(context)
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("github_repo_button")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Bug Report (Email with pre-filled subject & body containing app version)
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_report_bug),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = stringResource(R.string.drawer_report_bug),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        openBugReportEmail(context)
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("report_bug_button")
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.open_navigation_drawer),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
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
