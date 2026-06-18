package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.*
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import java.math.BigDecimal
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
                if (isCurrentMonth) FinancialValues(
                    income = BigDecimal.valueOf(3200.0 * multiplier),
                    expense = BigDecimal.valueOf(1450.0 * multiplier)
                )
                else FinancialValues(
                    income = BigDecimal.valueOf(3000.0 * multiplier),
                    expense = BigDecimal.valueOf(1600.0 * multiplier)
                )
            }
            PeriodModel.FORECAST -> {
                if (isCurrentMonth) FinancialValues(
                    income = BigDecimal.valueOf(800.0 * multiplier),
                    expense = BigDecimal.valueOf(350.0 * multiplier)
                )
                else FinancialValues(income = BigDecimal.ZERO, expense = BigDecimal.ZERO)
            }
        }

        emit(
            PeriodSummary(
                values = values,
                model = periodModel,
                type = periodType,
                shift = PeriodShift.CURRENT,
                startDate = startDate,
                endDate = endDate
            )
        )
    }
}
