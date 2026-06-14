package com.money.manager.ex.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.money.manager.ex.domain.model.Transaction
import com.money.manager.ex.domain.model.TransactionCode
import com.money.manager.ex.presentation.theme.MmexTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun RecentActivityList(
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
            TransactionRow(transaction = transaction)
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
    modifier: Modifier = Modifier
) {
    val amountColor = when (transaction.transCode) {
        TransactionCode.DEPOSIT -> Color(0xFF10B981) // Emerald Green
        TransactionCode.WITHDRAWAL -> Color(0xFFBA1A1A) // Error Red
        TransactionCode.TRANSFER -> Color(0xFF3B82F6) // Blue for transfers
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

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
                text = "${transaction.transDate.format(DateTimeFormatter.ofPattern("MMM dd"))} • ${transaction.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount
        Text(
            text = formatAmount(transaction.transAmount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
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
            transAmount = -45.60,
            transDate = LocalDate.now(),
            payee = "Supermarket",
            category = "Food",
            notes = "Spesa settimanale"
        ),
        Transaction(
            id = 2,
            accountId = 1,
            payeeId = 2,
            transCode = TransactionCode.DEPOSIT,
            transAmount = 2500.0,
            transDate = LocalDate.now().minusDays(1),
            payee = "Salary",
            category = "Job"
        )
    )
    MmexTheme {
        RecentActivityList(transactions = mockTransactions, onViewAllClick = {})
    }
}
