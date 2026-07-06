package com.money.manager.ex.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.money.manager.ex.domain.model.Transaction
import com.money.manager.ex.domain.model.TransactionCode
import com.money.manager.ex.domain.model.TransactionStatus
import com.money.manager.ex.presentation.theme.MmexTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun RecentActivityList(
    accountId: Int,
    transactions: List<Transaction>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        transactions.forEach { transaction ->
            TransactionRow(
                transaction = transaction,
                refAccountId = accountId
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    refAccountId: Int,
    modifier: Modifier = Modifier
) {
    val otherAccount =
        if (transaction.transCode == TransactionCode.TRANSFER) {
            "• " + if (transaction.accountId == refAccountId) "→ " + transaction.toAccountName else "← " + transaction.accountName
        } else ""

    val signedAmount = transaction.getSignedAmount(refAccountId)
    val amountColor = if (transaction.status == TransactionStatus.VOID) {
        Color.Gray
    } else {
        when (transaction.transCode) {
            TransactionCode.DEPOSIT -> Color(0xFF10B981) // Emerald Green
            TransactionCode.WITHDRAWAL -> Color(0xFFBA1A1A) // Error Red
            TransactionCode.TRANSFER -> Color(0xFF3B82F6) // Blue for transfers
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.payee,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            // Notes (if present)
            if (!transaction.notes.isNullOrBlank()) {
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${transaction.transDate.format(DateTimeFormatter.ofPattern("MMM dd"))} • ${transaction.category} $otherAccount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount and Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatAmount(signedAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            TransactionStatusIcon(status = transaction.status)
        }
    }
}

@Composable
fun TransactionStatusIcon(status: TransactionStatus, modifier: Modifier = Modifier) {
    val icon = if (status == TransactionStatus.VOID) Icons.Default.Done else Icons.Default.DoneAll
    val color = when (status) {
        TransactionStatus.NORMAL -> Color.Gray
        TransactionStatus.RECONCILED -> Color(0xFF3B82F6) // Blue
        TransactionStatus.FOLLOW_UP -> Color(0xFFFFB100) // Yellow/Amber
        TransactionStatus.VOID -> Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = modifier.size(16.dp)
    )
}

private fun formatAmount(amount: Double): String {
    val prefix = if (amount > 0) "+" else ""
    return "$prefix${String.format(Locale.getDefault(), "€%,.2f", amount)}"
}

@Preview
@Composable
fun RecentActivityListPreview() {
    val mockTransactions = listOf(
        Transaction(
            id = 1,
            accountId = 1,
            payeeId = 1,
            transCode = TransactionCode.WITHDRAWAL,
            transAmount = 45.60,
            status = TransactionStatus.NORMAL,
            transDate = LocalDate.now(),
            payee = "Supermarket",
            category = "Food",
            notes = "Weekly expense"
        ),
        Transaction(
            id = 2,
            accountId = 1,
            toAccountId = 2,
            payeeId = 2,
            transCode = TransactionCode.TRANSFER,
            transAmount = 100.0,
            status = TransactionStatus.RECONCILED,
            transDate = LocalDate.now().minusDays(1),
            payee = "",
            category = "Transfer from",
            accountName = "myself",
            toAccountName = "Other Account"
        ),
        Transaction(
            id = 3,
            accountId = 2,
            toAccountId = 1,
            payeeId = 2,
            transCode = TransactionCode.TRANSFER,
            transAmount = 100.0,
            status = TransactionStatus.FOLLOW_UP,
            transDate = LocalDate.now().minusDays(1),
            payee = "",
            category = "Transfer to",
            accountName = "Other Account",
            toAccountName = "myself"
        ),
        Transaction(
            id = 4,
            accountId = 1,
            payeeId = 2,
            transCode = TransactionCode.DEPOSIT,
            transAmount = 2500.0,
            transDate = LocalDate.now().minusDays(1),
            status = TransactionStatus.VOID,
            payee = "Salary",
            category = "Job"
        )
    )
    MmexTheme {
        RecentActivityList(
            accountId = 1,
            transactions = mockTransactions,
            onViewAllClick = {}
        )
    }
}
