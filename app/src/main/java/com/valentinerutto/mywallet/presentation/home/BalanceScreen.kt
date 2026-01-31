package com.valentinerutto.mywallet.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BalanceDialog(
    balanceState: BalanceState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            when {
                balanceState.isLoading -> CircularProgressIndicator()
                balanceState.error != null -> Icon(Icons.Default.ErrorOutline, null)
                else -> Icon(Icons.Default.AccountBalance, null)
            }
        },
        title = {
            Text(
                text = when {
                    balanceState.isLoading -> "Checking Balance..."
                    balanceState.error != null -> "Error"
                    else -> "Current Balance"
                }
            )
        },
        text = {
            when {
                balanceState.error != null -> {
                    Text(balanceState.error)
                }
                balanceState.balance != null -> {
                    Column {
                        Text(
                            text = "$${String.format("%.2f", balanceState.balance)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Account: ${balanceState.accountNo ?: "N/A"}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}