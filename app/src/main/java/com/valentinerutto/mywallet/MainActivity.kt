package com.valentinerutto.mywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.presentation.home.HomeScreen
import com.valentinerutto.mywallet.presentation.login.LoginScreen
import com.valentinerutto.mywallet.presentation.profile.ProfileScreen
import com.valentinerutto.mywallet.presentation.sendmoney.SendMoneyScreen
import com.valentinerutto.mywallet.presentation.statement.StatementScreen
import com.valentinerutto.mywallet.ui.theme.MyWalletTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: BankingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BankingApp(repository)
                }
            }
        }
    }
}

@Composable
fun BankingApp(repository: BankingRepository) {
    val navController = rememberNavController()
    val isLoggedIn by repository.isLoggedIn().collectAsState(initial = false)

    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSendMoney = {
                    navController.navigate(Screen.SendMoney.route)
                },
                onNavigateToStatement = {
                    navController.navigate(Screen.Statement.route)
                },
                onNavigateToLocalTransactions = {
                    navController.navigate(Screen.LocalTransactions.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Statement.route) {
            StatementScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
//
//        composable(Screen.LocalTransactions.route) {
//            LocalTransactionsScreen(
//                onNavigateBack = { navController.navigateUp() }
//            )
//        }
    }
}
