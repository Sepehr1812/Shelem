package com.shelem.scoresheet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shelem.scoresheet.ui.screens.AddRoundBottomSheet
import com.shelem.scoresheet.ui.screens.GameBoardScreen
import com.shelem.scoresheet.ui.screens.GameSetupScreen
import com.shelem.scoresheet.ui.screens.HomeScreen
import com.shelem.scoresheet.ui.viewmodel.ShelemViewModel

object ShelemDestinations {
    const val HOME = "home"
    const val SETUP = "setup"
    const val BOARD = "board/{gameId}"
    fun createBoardRoute(gameId: Long) = "board/$gameId"
}

@Composable
fun ShelemNavGraph(
    navController: NavHostController,
    viewModel: ShelemViewModel,
    modifier: Modifier = Modifier
) {
    var showAddRoundSheet by remember { mutableStateOf(false) }
    val activeGameState by viewModel.activeGame.collectAsStateWithLifecycle()
    val activeRoundsState by viewModel.activeRounds.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = ShelemDestinations.HOME,
        modifier = modifier
    ) {
        composable(ShelemDestinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSetup = {
                    navController.navigate(ShelemDestinations.SETUP)
                },
                onNavigateToBoard = { gameId ->
                    navController.navigate(ShelemDestinations.createBoardRoute(gameId))
                }
            )
        }

        composable(ShelemDestinations.SETUP) {
            GameSetupScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onGameCreated = { gameId ->
                    navController.popBackStack()
                    navController.navigate(ShelemDestinations.createBoardRoute(gameId))
                }
            )
        }

        composable(
            route = ShelemDestinations.BOARD,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: return@composable
            viewModel.selectActiveGame(gameId)

            GameBoardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenAddRoundSheet = { showAddRoundSheet = true }
            )
        }
    }

    if (showAddRoundSheet) {
        val currentGame = activeGameState
        if (currentGame != null) {
            val previousDealer = activeRoundsState.maxByOrNull { it.roundNumber }?.dealerPlayerName
            AddRoundBottomSheet(
                game = currentGame,
                previousDealerName = previousDealer,
                onDismiss = { showAddRoundSheet = false },
                onConfirmBid = { hakemTeam, hakemPlayerName, dealerPlayerName, bidAmount, isNaras ->
                    viewModel.createOngoingRound(
                        hakemTeam = hakemTeam,
                        hakemPlayerName = hakemPlayerName,
                        dealerPlayerName = dealerPlayerName,
                        bidAmount = bidAmount,
                        isNaras = isNaras,
                        onSuccess = { showAddRoundSheet = false }
                    )
                }
            )
        }
    }
}
