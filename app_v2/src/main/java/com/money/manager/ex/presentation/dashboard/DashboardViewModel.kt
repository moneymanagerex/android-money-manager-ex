package com.money.manager.ex.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.money.manager.ex.domain.model.*
import com.money.manager.ex.domain.repository.AccountRepository
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
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
    private val periodSummaryRepository: PeriodSummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Int?>(null)
    private val _selectedPeriodType = MutableStateFlow(PeriodType.MONTH)
    private val _isPeriodMenuVisible = MutableStateFlow(false)

    init {
        loadDashboardData()
    }

    fun onAccountSelected(accountId: Int?) {
        _selectedAccountId.value = accountId
    }

    fun onPeriodTypeSelected(periodType: PeriodType) {
        _selectedPeriodType.value = periodType
        _isPeriodMenuVisible.value = false
    }

    fun togglePeriodMenu() {
        _isPeriodMenuVisible.value = !_isPeriodMenuVisible.value
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                accountRepository.getOpenAccounts(),
                _selectedAccountId,
                _selectedPeriodType,
                _isPeriodMenuVisible
            ) { accounts, selectedId, periodType, menuVisible ->
                DataParams(accounts, selectedId, periodType, menuVisible)
            }.flatMapLatest { params ->
                val dates = calculateDates(params.periodType)
                
                combine(
                    transactionRepository.getRecentTransactions(5, params.accountId),
                    periodSummaryRepository.getSummary(dates.currentStart, dates.currentEnd, params.periodType, PeriodModel.ACTUAL, params.accountId),
                    periodSummaryRepository.getSummary(dates.currentStart, dates.currentEnd, params.periodType, PeriodModel.FORECAST, params.accountId),
                    periodSummaryRepository.getSummary(dates.prevStart, dates.prevEnd, params.periodType, PeriodModel.ACTUAL, params.accountId)
                ) { transactions, currentActual, currentForecast, previousActual ->
                    DashboardUiState(
                        isLoading = false,
                        accounts = params.accounts,
                        selectedAccountId = params.accountId,
                        selectedPeriodType = params.periodType,
                        isPeriodMenuVisible = params.isMenuVisible,
                        recentActivity = transactions,
                        currentActualSummary = currentActual,
                        currentForecastSummary = currentForecast,
                        previousActualSummary = previousActual
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
        val periodType: PeriodType,
        val isMenuVisible: Boolean
    )

    private data class DateRange(
        val currentStart: LocalDate,
        val currentEnd: LocalDate,
        val prevStart: LocalDate,
        val prevEnd: LocalDate
    )

    private fun calculateDates(periodType: PeriodType): DateRange {
        val now = LocalDate.now()
        return when (periodType) {
            PeriodType.WEEK -> {
                val start = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                DateRange(start, start.plusDays(6), start.minusWeeks(1), start.minusDays(1))
            }
            PeriodType.MONTH -> {
                val start = now.withDayOfMonth(1)
                DateRange(start, start.withDayOfMonth(start.lengthOfMonth()), start.minusMonths(1), start.withDayOfMonth(1).minusDays(1))
            }
            PeriodType.QUARTER -> {
                val quarter = (now.monthValue - 1) / 3
                val start = LocalDate.of(now.year, quarter * 3 + 1, 1)
                DateRange(start, start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth()), start.minusMonths(3), start.minusDays(1))
            }
            PeriodType.FOUR_MONTH -> {
                val start = LocalDate.of(now.year, ((now.monthValue - 1) / 4) * 4 + 1, 1)
                DateRange(start, start.plusMonths(3).withDayOfMonth(start.plusMonths(3).lengthOfMonth()), start.minusMonths(4), start.minusDays(1))
            }
            PeriodType.HALF_YEAR -> {
                val start = LocalDate.of(now.year, if (now.monthValue <= 6) 1 else 7, 1)
                DateRange(start, start.plusMonths(5).withDayOfMonth(start.plusMonths(5).lengthOfMonth()), start.minusMonths(6), start.minusDays(1))
            }
            PeriodType.YEAR, PeriodType.FISCAL_YEAR -> {
                val start = now.withDayOfYear(1)
                DateRange(start, start.withDayOfYear(start.lengthOfYear()), start.minusYears(1), start.minusDays(1))
            }
        }
    }
}
