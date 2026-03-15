package com.gneticcore.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gneticcore.app.ui.screens.*
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.UserSession

@Composable
fun GneticCoreNavGraph(viewModel: GameViewModel) {
    val nav = rememberNavController()

    // Determinar el destino inicial según el estado de la sesión
    val start = when {
        UserSession.isLoggedIn && UserSession.isAdmin -> Screen.AdminHome.route
        UserSession.isLoggedIn                        -> Screen.UserHome.route
        else                                          -> Screen.Splash.route
    }

    NavHost(nav, startDestination = start) {

        composable(Screen.Splash.route) {
            SplashScreen {
                nav.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel    = viewModel,
                onSuccess    = { isAdmin ->
                    val dest = if (isAdmin) Screen.AdminHome.route else Screen.UserHome.route
                    nav.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onGoRegister = { nav.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onSuccess = { nav.navigate(Screen.UserHome.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onGoLogin = { nav.popBackStack() }
            )
        }

        composable(Screen.UserHome.route) {
            UserHomeScreen(
                viewModel     = viewModel,
                onGameClick   = { id -> nav.navigate(Screen.GameDetail.createRoute(id)) },
                onAddGame     = { nav.navigate(Screen.AddGame.route) },
                onProposeEdit = { id -> nav.navigate(Screen.ProposeEdit.createRoute(id)) },
                onLogout      = { viewModel.logout(); nav.navigate(Screen.Login.route) { popUpTo(Screen.UserHome.route) { inclusive = true } } }
            )
        }

        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                viewModel    = viewModel,
                onGameClick  = { id -> nav.navigate(Screen.GameDetail.createRoute(id)) },
                onEditGame   = { id -> nav.navigate(Screen.EditGame.createRoute(id)) },
                onPending    = { nav.navigate(Screen.PendingList.route) },
                onStatistics = { nav.navigate(Screen.Statistics.route) },
                onLogout     = { viewModel.logout(); nav.navigate(Screen.Login.route) { popUpTo(Screen.AdminHome.route) { inclusive = true } } }
            )
        }

        composable(Screen.AddGame.route) {
            GameFormScreen(viewModel = viewModel, gameId = null, isAdmin = false, onBack = { nav.popBackStack() })
        }

        composable(Screen.EditGame.route, arguments = listOf(navArgument("gameId") { type = NavType.IntType })) { back ->
            val id = back.arguments?.getInt("gameId") ?: -1
            GameFormScreen(viewModel = viewModel, gameId = if (id == -1) null else id, isAdmin = true, onBack = { nav.popBackStack() })
        }

        composable(Screen.ProposeEdit.route, arguments = listOf(navArgument("gameId") { type = NavType.IntType })) { back ->
            ProposeEditScreen(viewModel = viewModel, gameId = back.arguments?.getInt("gameId"), onBack = { nav.popBackStack() })
        }

        composable(Screen.GameDetail.route, arguments = listOf(navArgument("gameId") { type = NavType.IntType })) { back ->
            val gameId = back.arguments?.getInt("gameId") ?: return@composable
            GameDetailScreen(
                viewModel     = viewModel,
                gameId        = gameId,
                onBack        = { nav.popBackStack() },
                onEdit        = { nav.navigate(Screen.EditGame.createRoute(gameId)) },
                onProposeEdit = { nav.navigate(Screen.ProposeEdit.createRoute(gameId)) }
            )
        }

        composable(Screen.PendingList.route) {
            PendingListScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }
    }
}
