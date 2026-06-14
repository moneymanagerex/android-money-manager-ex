package com.money.manager.ex.domain.model

import java.time.LocalDate

enum class PeriodType(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    FOUR_MONTH("4-month"),
    HALF_YEAR("Half Year"),
    YEAR("Year"),
    FISCAL_YEAR("Fiscal Year")
}

enum class PeriodModel {
    ACTUAL, FORECAST
}

data class FinancialValues(
    val income: Double = 0.0,
    val expense: Double = 0.0
)

data class PeriodSummary(
    val values: FinancialValues,
    val periodModel: PeriodModel,
    val periodType: PeriodType,
    val startDate: LocalDate,
    val endDate: LocalDate
)
