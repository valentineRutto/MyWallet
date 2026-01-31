package com.valentinerutto.mywallet.presentation.localtransactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.TransactionStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Status colours – match the mockup
private val COLOR_SYNCING  = Color(0xFF2196F3)   // blue
private val COLOR_SYNCED   = Color(0xFF4CAF50)   // green
private val COLOR_FAILED   = Color(0xFFF44336)   // red
private val COLOR_QUEUED   = Color(0xFF9E9E9E)   // grey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LocalTransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Local Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLOR_SYNCING)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = COLOR_SYNCING)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { viewModel.syncAll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor   = Color.White
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Sync all transactions", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Activity header ─────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              //  verticalAlignment     = Alignment.Baseline
            ) {
                Text(
                    "Activity",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Black
                )
                Text(
                    "${state.pendingCount} pending",
                    fontSize = 14.sp,
                    color    = Color.Gray
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Transaction list ────────────────────────────────────────
            if (state.transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No local transactions yet", color = Color.Gray)
                }
            } else {
                state.transactions.forEach { tx ->
                    TransactionItem(tx, onRetry = { viewModel.retry(tx) })
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(tx: Transaction, onRetry: () -> Unit) {
    val statusColor = when (tx.status) {
        TransactionStatus.SYNCING -> COLOR_SYNCING
        TransactionStatus.SYNCED  -> COLOR_SYNCED
        TransactionStatus.FAILED  -> COLOR_FAILED
        TransactionStatus.QUEUED  -> COLOR_QUEUED
    }

    val statusLabel = when (tx.status) {
        TransactionStatus.SYNCING -> "SYNCING"
        TransactionStatus.SYNCED  -> "SYNCED"
        TransactionStatus.FAILED  -> "FAILED"
        TransactionStatus.QUEUED  -> "QUEUED"
    }

    val dateStr = SimpleDateFormat("MMM dd • hh:mm a", Locale.US).format(Date(tx.createdAt))
    val amountStr = run {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        "-${fmt.format(tx.amount)}"
    }

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            // Left: dot + name + date
            Row(verticalAlignment = Alignment.Top) {
                // Status dot
                Spacer(Modifier.width(0.dp)) // anchor
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(y = 5.dp),
                        //.background(color = statusColor, shape = androidx.compose.ui.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) { }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(tx.accountTo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(dateStr, fontSize = 13.sp, color = Color.Gray)
                }
            }

            // Right: amount + status badge
            Column(horizontalAlignment = Alignment.End) {
                Text(amountStr, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }

        // Error banner (only for FAILED)
        if (tx.status == TransactionStatus.FAILED && tx.lastError != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp)   // indent past the dot
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(COLOR_FAILED)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(tx.lastError, fontSize = 13.sp, color = COLOR_FAILED)
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onRetry,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Retry", fontSize = 13.sp, color = COLOR_SYNCING, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
