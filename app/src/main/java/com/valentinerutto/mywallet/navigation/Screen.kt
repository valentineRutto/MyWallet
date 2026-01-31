package com.valentinerutto.mywallet

sealed class Screen(val route: String) {
    object Login              : Screen("login")
    object Home               : Screen("home")
    object Profile            : Screen("profile")
    object SendMoney          : Screen("send_money")
    object Statement          : Screen("statement")
    object LocalTransactions  : Screen("local_transactions")
}
