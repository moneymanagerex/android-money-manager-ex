package com.money.manager.ex.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.money.manager.ex.domain.model.*
import com.money.manager.ex.presentation.theme.MmexTheme
import java.text.NumberFormat
import java.util.*

@Composable
fun AccountCard(
    account: Account,
    forecastSummary: PeriodSummary? = null,
    modifier: Modifier = Modifier
) {
    val forestGreen = Color(0xFF064E3B)
    val emeraldGreen = Color(0xFF10B981)
    
    val gradient = Brush.linearGradient(
        colors = listOf(emeraldGreen, forestGreen)
    )

    // EOM Forecast calculation: Account.Balance + currentForecastSummary.income - currentForecastSummary.expense
    val eomForecast = if (forecastSummary != null && forecastSummary.periodModel == PeriodModel.FORECAST) {
        account.balance + (forecastSummary.values.income - forecastSummary.values.expense)
    } else {
        account.balance
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getAccountIcon(account.type),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (account.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatCurrency(account.balance, account.currencySymbol, account.currencyPrefix),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceInfo(
                    label = "Ledger Balance",
                    amount = account.ledgerBalance,
                    symbol = account.currencySymbol,
                    prefix = account.currencyPrefix
                )
                BalanceInfo(
                    label = "EO-${forecastSummary?.periodType?.label ?: "Month"} Forecast",
                    amount = eomForecast,
                    symbol = account.currencySymbol,
                    prefix = account.currencyPrefix,
                    horizontalAlignment = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun BalanceInfo(
    label: String,
    amount: Double,
    symbol: String,
    prefix: String?,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = formatCurrency(amount, symbol, prefix),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getAccountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Payments
        AccountType.CHECKING -> Icons.Default.AccountBalance
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        else -> Icons.Default.AccountBalance
    }
}

private fun formatCurrency(amount: Double, symbol: String, prefix: String?): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val formatted = numberFormat.format(amount)
    return if (prefix != null) "$prefix $formatted" else "$formatted $symbol"
}

@Preview
@Composable
fun AccountCardPreview() {
    val mockAccount = Account(
        id = 1,
        name = "Main Bank",
        type = AccountType.CHECKING,
        status = "Open",
        isFavorite = true,
        currencySymbol = "€",
        currencyPrefix = null,
        balance = 12500.50,
        ledgerBalance = 12000.00
    )
    MmexTheme {
        AccountCard(account = mockAccount, modifier = Modifier.padding(16.dp))
    }
}
