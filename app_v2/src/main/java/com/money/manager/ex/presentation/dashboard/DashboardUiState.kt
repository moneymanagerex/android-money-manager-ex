package com.money.manager.ex.presentation.dashboard

import com.money.manager.ex.domain.model.Account
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.model.PeriodType
import com.money.manager.ex.domain.model.Transaction

data class SummaryData(
    val current: PeriodSummary,
    val trend: Double // Percentage change
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Int? = null,
    val selectedPeriodType: PeriodType = PeriodType.MONTH,
    val isPeriodMenuVisible: Boolean = false,
    val currentActualSummary: PeriodSummary? = null,
    val currentForecastSummary: PeriodSummary? = null,
    val previousActualSummary: PeriodSummary? = null,
    val recentActivity: List<Transaction> = emptyList(),
    val databaseName: String = "No database selected",
    val error: String? = null
)
