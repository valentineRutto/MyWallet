package com.valentinerutto.mywallet.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSendMoney: () -> Unit,
    onNavigateToStatement: () -> Unit,
    onNavigateToLocalTransactions: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val user = homeState.user
    val showBalanceSheet by viewModel.showBalanceSheet.collectAsState()
    val balanceState by viewModel.balanceState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToStatement,
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Transactions") },
                    label = { Text("Transactions") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile ,
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Settings") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->

        if (homeState.isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Greeting
                Text(
                    text = "Hello, ${user.fullName?.split(" ")?.firstOrNull() ?: "User"}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatCurrency(user.balance?:0.0),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Menu Items
                MenuItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Check Balance",
                    onClick = {
                        viewModel.checkBalance(user.customerId)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItem(
                    icon = Icons.Default.Send,
                    title = "Send Money",
                    onClick = onNavigateToSendMoney
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItem(
                    icon = Icons.Default.Person,
                    title = "View Profile",
                    onClick = onNavigateToProfile
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItem(
                    icon = Icons.Default.Description,
                    title = "View Statement",
                    onClick = onNavigateToStatement
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "Local Activity",
                    onClick = onNavigateToLocalTransactions
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = { viewModel.logout(onLogout) }
                )
            }

            if (showBalanceSheet) {
                BalanceDialog(
                    balanceState = balanceState,
                    onDismiss = { viewModel.hideBalanceSheet() }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}
