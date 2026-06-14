package com.money.manager.ex.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.money.manager.ex.domain.model.FinancialValues
import com.money.manager.ex.domain.model.PeriodModel
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.model.PeriodType
import com.money.manager.ex.presentation.theme.MmexTheme
import java.time.LocalDate
import java.util.*

@Composable
fun SummaryRow(
    currentActual: PeriodSummary?,
    currentForecast: PeriodSummary?,
    previousActual: PeriodSummary?,
    previousForecast: PeriodSummary? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income Card
        SummaryCard(
            title = "Income",
            actual = currentActual?.values?.income ?: 0.0,
            actualTrend = calculateTrend(currentActual?.values?.income ?: 0.0, previousActual?.values?.income ?: 0.0),
            forecast = (currentActual?.values?.income ?: 0.0) + (currentForecast?.values?.income ?: 0.0),
            forecastTrend = calculateTrend(
                (currentActual?.values?.income ?: 0.0) + (currentForecast?.values?.income ?: 0.0),
                (previousActual?.values?.income ?: 0.0) + (previousForecast?.values?.income ?: 0.0)
            ),
            isIncome = true,
            modifier = Modifier.weight(1f)
        )
        // Expense Card
        SummaryCard(
            title = "Expenses",
            actual = currentActual?.values?.expense ?: 0.0,
            actualTrend = calculateTrend(currentActual?.values?.expense ?: 0.0, previousActual?.values?.expense ?: 0.0),
            forecast = (currentActual?.values?.expense ?: 0.0) + (currentForecast?.values?.expense ?: 0.0),
            forecastTrend = calculateTrend(
                (currentActual?.values?.expense ?: 0.0) + (currentForecast?.values?.expense ?: 0.0),
                (previousActual?.values?.expense ?: 0.0) + (previousForecast?.values?.expense ?: 0.0)
            ),
            isIncome = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    actual: Double,
    actualTrend: Double,
    forecast: Double,
    forecastTrend: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val successColor = Color(0xFF10B981) // Emerald Green
    val errorColor = Color(0xFFBA1A1A)   // Error Red
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Titolo (Income o Expence)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isIncome) successColor else errorColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Valore Actual (grosso e bold) - Allineato a destra
            Text(
                text = formatValue(actual),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            // Badge variazione percentuale (actual su actual previous) - Allineato a destra
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TrendBadge(trend = actualTrend, isIncome = isIncome)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // "forecast" in più piccolo - Allineato a SINISTRA
            Text(
                text = "forecast",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )

            // Valore Forecast - Allineato a destra
            Text(
                text = formatValue(forecast),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            // Badge variazione (forecast actual su forecast previous) - Allineato a destra
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TrendBadge(trend = forecastTrend, isIncome = isIncome)
            }
        }
    }
}

@Composable
private fun TrendBadge(trend: Double, isIncome: Boolean) {
    val successColor = Color(0xFF10B981)
    val errorColor = Color(0xFFBA1A1A)
    
    val trendColor = if (isIncome) {
        if (trend >= 0) successColor else errorColor
    } else {
        if (trend <= 0) successColor else errorColor
    }

    Surface(
        color = trendColor.copy(alpha = 0.1f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${if (trend >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", trend)}%",
            style = MaterialTheme.typography.labelSmall,
            color = trendColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun calculateTrend(current: Double, previous: Double): Double {
    if (previous == 0.0) return 0.0
    return ((current - previous) / previous) * 100.0
}

private fun formatValue(value: Double): String {
    return String.format(Locale.getDefault(), "€%,.2f", value)
}

@Preview
@Composable
fun SummaryRowPreview() {
    val currentActual = PeriodSummary(
        values = FinancialValues(income = 2000.0, expense = 1500.0),
        periodModel = PeriodModel.ACTUAL,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val currentForecast = PeriodSummary(
        values = FinancialValues(income = 1500.0, expense = 300.0),
        periodModel = PeriodModel.FORECAST,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val previousActual = PeriodSummary(
        values = FinancialValues(income = 2800.0, expense = 1600.0),
        periodModel = PeriodModel.ACTUAL,
        periodType = PeriodType.MONTH,
        startDate = LocalDate.now().minusMonths(1),
        endDate = LocalDate.now().minusMonths(1)
    )

    MmexTheme {
        SummaryRow(
            currentActual = currentActual,
            currentForecast = currentForecast,
            previousActual = previousActual
        )
    }
}
