package com.valentinerutto.mywallet.presentation.statement

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                actions = {
                    IconButton(onClick = { /* share / export placeholder */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
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
                "OCTOBER 2023",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(8.dp))

            // Scrollable entry list – fills remaining space above the footer
            Column(modifier = Modifier.weight(1f)) {
                state.entries.forEach { entry ->
                    StatementRow(
                        date        = entry.date,
                        description = entry.description,
                        amount      = entry.amount
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── TOTAL row ───────────────────────────────────────────────
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

            // Download button
            Button(
                onClick = { /* download / export placeholder */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor   = Color.White
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("Download Statement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatementRow(date: String, description: String, amount: Double) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column {
            Text(date, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(description, fontSize = 14.sp, color = Color.Gray)
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
