package com.valentinerutto.mywallet.presentation.statement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.valentinerutto.mywallet.presentation.home.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatementViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel=hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val userProfile by homeViewModel.homeState.collectAsState()
val customerId = userProfile.user?.customerId ?: ""

    LaunchedEffect(customerId) {
        viewModel.loadTransactions(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Month header
            Text(
                "Last 100 Transactions",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f)) {

                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    state.error != null -> Text("Error: ${state.error}", Modifier.align(Alignment.CenterHorizontally))
                    state.entries.isEmpty() -> {
                        Text(
                            text = "No transactions found",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    else -> {
                        LazyColumn {
                            items(state.entries) {entry->
                                StatementRow(
                                    accountNo = entry.accountNo?:"", creditORdebit = entry.debitOrCredit?:"", amount = entry.amount?:0.0
                                )

                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                    }
                }

            }

            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier      = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL",
                    fontSize = 13.sp,
                    color    = Color.Gray,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text       = formatAmount(state.total),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Black
                )
            }



            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatementRow(accountNo: String, creditORdebit: String, amount: Double) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column {
            Text(accountNo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(creditORdebit, fontSize = 14.sp, color = Color.Gray)
        }
        Text(
            text       = formatAmount(amount),
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
    }
}

private fun formatAmount(amount: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    return if (amount >= 0) "+${fmt.format(amount)}" else fmt.format(amount)
}
