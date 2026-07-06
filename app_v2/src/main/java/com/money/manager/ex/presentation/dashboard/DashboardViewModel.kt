package com.money.manager.ex.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.money.manager.ex.domain.model.*
import com.money.manager.ex.domain.repository.AccountRepository
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import com.money.manager.ex.domain.repository.SettingsRepository
import com.money.manager.ex.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val periodSummaryRepository: PeriodSummaryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Int?>(null)
    private val _selectedPeriodElapsed = MutableStateFlow(PeriodElapsed.MONTH)
    private val _isPeriodMenuVisible = MutableStateFlow(false)

    init {
        loadDashboardData()
    }

    fun onAccountSelected(accountId: Int?) {
        _selectedAccountId.value = accountId
    }

    fun onPeriodElapsedSelected(periodElapsed: PeriodElapsed) {
        _selectedPeriodElapsed.value = periodElapsed
        _isPeriodMenuVisible.value = false
    }

    fun onRefresh() {
        loadDashboardData(isRefresh = true)
    }

    fun togglePeriodMenu() {
        _isPeriodMenuVisible.value = !_isPeriodMenuVisible.value
    }

    fun onDatabaseSelected(path: String, name: String) {
        viewModelScope.launch {
            settingsRepository.setDatabasePath(path)
            settingsRepository.setDatabaseName(name)
        }
    }

    private fun loadDashboardData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            combine(
                accountRepository.getOpenAccounts(),
                _selectedAccountId,
                _selectedPeriodElapsed,
                _isPeriodMenuVisible,
                settingsRepository.getDatabaseName()
            ) { accounts, selectedId, periodElapsed, menuVisible, dbName ->
                DataParams(accounts, selectedId, periodElapsed, menuVisible, dbName ?: "No database selected")
            }.flatMapLatest { params ->
                val accountId = params.accountId
                if (accountId == null) {
                    return@flatMapLatest flowOf(
                        DashboardUiState(
                            isLoading = false,
                            isRefreshing = false,
                            accounts = params.accounts,
                            selectedAccountId = null,
                            selectedPeriodElapsed = params.periodElapsed,
                            isPeriodMenuVisible = params.isMenuVisible,
                            databaseName = params.dbName
                        )
                    )
                }

                val dates = calculateDates(params.periodElapsed)
                
                combine(
                    transactionRepository.getRecentTransactions(10, accountId),
                    periodSummaryRepository.getSummary(dates.currentStart, dates.currentEnd, params.periodElapsed, PeriodModel.ACTUAL, accountId),
                    periodSummaryRepository.getSummary(dates.currentStart, dates.currentEnd, params.periodElapsed, PeriodModel.FORECAST, accountId),
                    periodSummaryRepository.getSummary(dates.prevStart, dates.prevEnd, params.periodElapsed, PeriodModel.ACTUAL, accountId)
                ) { transactions, currentActual, currentForecast, previousActual ->
                    DashboardUiState(
                        isLoading = false,
                        isRefreshing = false,
                        accounts = params.accounts,
                        selectedAccountId = accountId,
                        selectedPeriodElapsed = params.periodElapsed,
                        isPeriodMenuVisible = params.isMenuVisible,
                        recentActivity = transactions,
                        currentActualSummary = currentActual,
                        currentForecastSummary = currentForecast,
                        previousActualSummary = previousActual,
                        databaseName = params.dbName
                    )
                }
            }.collect { state ->
                if (_selectedAccountId.value == null && state.accounts.isNotEmpty()) {
                    _selectedAccountId.value = state.accounts.first().id
                }
                _uiState.value = state
            }
        }
    }

    private data class DataParams(
        val accounts: List<Account>,
        val accountId: Int?,
        val periodElapsed: PeriodElapsed,
        val isMenuVisible: Boolean,
        val dbName: String
    )

    private data class DateRange(
        val currentStart: LocalDate,
        val currentEnd: LocalDate,
        val prevStart: LocalDate,
        val prevEnd: LocalDate
    )

    private fun calculateDates(periodElapsed: PeriodElapsed): DateRange {
        val now = LocalDate.now()
        return when (periodElapsed) {
            PeriodElapsed.WEEK -> {
                val start = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                DateRange(start, start.plusDays(6), start.minusWeeks(1), start.minusDays(1))
            }
            PeriodElapsed.MONTH -> {
                val start = now.withDayOfMonth(1)
                DateRange(start, start.withDayOfMonth(start.lengthOfMonth()), start.minusMonths(1), start.withDayOfMonth(1).minusDays(1))
            }
            PeriodElapsed.QUARTER -> {
                val quarter = (now.monthValue - 1) / 3
                val start = LocalDate.of(now.year, quarter * 3 + 1, 1)
                DateRange(start, start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth()), start.minusMonths(3), start.minusDays(1))
            }
            PeriodElapsed.FOUR_MONTH -> {
                val start = LocalDate.of(now.year, ((now.monthValue - 1) / 4) * 4 + 1, 1)
                DateRange(start, start.plusMonths(3).withDayOfMonth(start.plusMonths(3).lengthOfMonth()), start.minusMonths(4), start.minusDays(1))
            }
            PeriodElapsed.HALF_YEAR -> {
                val start = LocalDate.of(now.year, if (now.monthValue <= 6) 1 else 7, 1)
                DateRange(start, start.plusMonths(5).withDayOfMonth(start.plusMonths(5).lengthOfMonth()), start.minusMonths(6), start.minusDays(1))
            }
            PeriodElapsed.YEAR, PeriodElapsed.FISCAL_YEAR -> {
                val start = now.withDayOfYear(1)
                DateRange(start, start.withDayOfYear(start.lengthOfYear()), start.minusYears(1), start.minusDays(1))
            }
        }
    }
}
