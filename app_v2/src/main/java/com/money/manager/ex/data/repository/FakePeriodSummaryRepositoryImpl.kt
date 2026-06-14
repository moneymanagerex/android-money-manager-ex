package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.*
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class FakePeriodSummaryRepositoryImpl @Inject constructor() : PeriodSummaryRepository {
    override fun getSummary(
        startDate: LocalDate,
        endDate: LocalDate,
        periodType: PeriodType,
        periodModel: PeriodModel,
        accountId: Int?
    ): Flow<PeriodSummary> = flow {
        delay(300)

        val isCurrentMonth = startDate.month == LocalDate.now().month
        
        // Simulo dati diversi per account diversi
        val multiplier = when (accountId) {
            1 -> 1.0
            2 -> 0.4
            3 -> 0.2
            else -> 1.0 // Totale o altro
        }

        val values = when (periodModel) {
            PeriodModel.ACTUAL -> {
                if (isCurrentMonth) FinancialValues(income = 3200.0 * multiplier, expense = 1450.0 * multiplier)
                else FinancialValues(income = 3000.0 * multiplier, expense = 1600.0 * multiplier)
            }
            PeriodModel.FORECAST -> {
                if (isCurrentMonth) FinancialValues(income = 800.0 * multiplier, expense = 350.0 * multiplier)
                else FinancialValues(income = 0.0, expense = 0.0)
            }
        }

        emit(
            PeriodSummary(
                values = values,
                periodModel = periodModel,
                periodType = periodType,
                startDate = startDate,
                endDate = endDate
            )
        )
    }
}
