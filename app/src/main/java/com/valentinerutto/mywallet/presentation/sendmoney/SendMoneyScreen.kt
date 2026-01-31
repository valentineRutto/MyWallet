package com.valentinerutto.mywallet.presentation.sendmoney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.banking.app.presentation.sendmoney.SendMoneyViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    onNavigateBack: () -> Unit,
    viewModel: SendMoneyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Once queued, show snackbar then go back
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.queued) {
        if (state.queued) {
            showSnackbar = true
            snackbarHostState.showSnackbar("Queued for sync", withDismissAction = true)
            onNavigateBack()
        }
    }

    var recipient by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val canSend = recipient.isNotBlank() && amountValue > 0.0 && amountValue <= state.balance

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Send",
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
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.sendMoney(recipient, amountValue, note) },
                    enabled = canSend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor   = Color.White,
                        disabledContainerColor = Color.Black.copy(alpha = 0.4f),
                        disabledContentColor   = Color.White.copy(alpha = 0.6f)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Send Money", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Spacer(Modifier.height(28.dp))

            // ── TO ──────────────────────────────────────────────────────
            Text(
                "TO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 0.8.sp
            )
            TextField(
                value = recipient,
                onValueChange = { recipient = it },
                placeholder = {
                    Text(
                        "Name or Account",
                        fontSize = 22.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Normal
                    )
                },
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(Modifier.height(28.dp))

            // ── AMOUNT ──────────────────────────────────────────────────
            Text(
                "AMOUNT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 0.8.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.width(4.dp))
                TextField(
                    value = amountText,
                    onValueChange = { raw ->
                        // Allow only valid decimal input
                        val filtered = raw.replace(Regex("[^0-9.]"), "")
                        if (filtered.count { it == '.' } <= 1) amountText = filtered
                    },
                    placeholder = {
                        Text(
                            "0.00",
                            fontSize = 48.sp,
                            color = Color(0xFFDDDDDD),
                            fontWeight = FontWeight.Normal
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions =
                        androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Available: ${formatMoney(state.balance)}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            // ── FOR ─────────────────────────────────────────────────────
            Text(
                "FOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 0.8.sp
            )
            TextField(
                value = note,
                onValueChange = { note = it },
                placeholder = {
                    Text(
                        "What is it for?",
                        fontSize = 18.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Normal
                    )
                },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

private fun formatMoney(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(amount)
