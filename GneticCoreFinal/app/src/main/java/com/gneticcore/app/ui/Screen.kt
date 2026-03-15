package com.gneticcore.app.ui

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Login       : Screen("login")
    object Register    : Screen("register")
    object UserHome    : Screen("user_home")
    object AdminHome   : Screen("admin_home")
    object AddGame     : Screen("add_game")
    object PendingList : Screen("pending_list")
    object Statistics  : Screen("statistics")
    object GameDetail  : Screen("game_detail/{gameId}") {
        fun createRoute(id: Int) = "game_detail/$id"
    }
    object EditGame    : Screen("edit_game/{gameId}") {
        fun createRoute(id: Int) = "edit_game/$id"
    }
    object ProposeEdit : Screen("propose_edit/{gameId}") {
        fun createRoute(id: Int) = "propose_edit/$id"
    }
}
