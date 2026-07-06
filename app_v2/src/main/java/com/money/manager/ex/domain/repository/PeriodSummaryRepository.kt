package com.money.manager.ex.domain.repository

import com.money.manager.ex.domain.model.PeriodElapsed
import com.money.manager.ex.domain.model.PeriodModel
import com.money.manager.ex.domain.model.PeriodSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PeriodSummaryRepository {
    fun getSummary(
        startDate: LocalDate,
        endDate: LocalDate,
        periodElapsed: PeriodElapsed,
        periodModel: PeriodModel,
        accountId: Int
    ): Flow<PeriodSummary>
}
