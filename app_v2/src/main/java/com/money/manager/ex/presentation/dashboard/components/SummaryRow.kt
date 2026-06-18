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
import com.money.manager.ex.domain.model.PeriodShift
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.model.PeriodType
import com.money.manager.ex.presentation.theme.MmexTheme
import java.math.BigDecimal
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
        val currentIncome = currentActual?.values?.income ?: BigDecimal.ZERO
        val currentForecastIncome = currentForecast?.values?.income ?: BigDecimal.ZERO
        val previousIncome = previousActual?.values?.income ?: BigDecimal.ZERO
        val previousForecastIncome = previousForecast?.values?.income ?: BigDecimal.ZERO

        val currentExpense = currentActual?.values?.expense ?: BigDecimal.ZERO
        val currentForecastExpense = currentForecast?.values?.expense ?: BigDecimal.ZERO
        val previousExpense = previousActual?.values?.expense ?: BigDecimal.ZERO
        val previousForecastExpense = previousForecast?.values?.expense ?: BigDecimal.ZERO

        // Income Card
        SummaryCard(
            title = "Income",
            actual = currentIncome,
            actualTrend = calculateTrend(currentIncome, previousIncome),
            forecast = currentIncome + currentForecastIncome,
            forecastTrend = calculateTrend(
                currentIncome + currentForecastIncome,
                previousIncome + previousForecastIncome
            ),
            isIncome = true,
            modifier = Modifier.weight(1f)
        )
        // Expense Card
        SummaryCard(
            title = "Expenses",
            actual = currentExpense,
            actualTrend = calculateTrend(currentExpense, previousExpense),
            forecast = currentExpense + currentForecastExpense,
            forecastTrend = calculateTrend(
                currentExpense + currentForecastExpense,
                previousExpense + previousForecastExpense
            ),
            isIncome = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    actual: BigDecimal,
    actualTrend: Double,
    forecast: BigDecimal,
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

private fun calculateTrend(current: BigDecimal, previous: BigDecimal): Double {
    if (previous.compareTo(BigDecimal.ZERO) == 0) return 0.0
    return ((current - previous).toDouble() / previous.toDouble()) * 100.0
}

private fun formatValue(value: BigDecimal): String {
    return String.format(Locale.getDefault(), "€%,.2f", value.toDouble())
}

@Preview
@Composable
fun SummaryRowPreview() {
    val currentActual = PeriodSummary(
        values = FinancialValues(income = BigDecimal.valueOf(2000.0), expense = BigDecimal.valueOf(1500.0)),
        model = PeriodModel.ACTUAL,
        type = PeriodType.MONTH,
        shift = PeriodShift.CURRENT,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val currentForecast = PeriodSummary(
        values = FinancialValues(income = BigDecimal.valueOf(1500.0), expense = BigDecimal.valueOf(300.0)),
        model = PeriodModel.FORECAST,
        type = PeriodType.MONTH,
        shift = PeriodShift.CURRENT,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()
    )
    val previousActual = PeriodSummary(
        values = FinancialValues(income = BigDecimal.valueOf(2800.0), expense = BigDecimal.valueOf(1600.0)),
        model = PeriodModel.ACTUAL,
        type = PeriodType.MONTH,
        shift = PeriodShift.PREVIOUS,
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
