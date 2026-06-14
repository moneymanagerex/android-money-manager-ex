package com.money.manager.ex.domain.repository

import com.money.manager.ex.domain.model.PeriodModel
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.model.PeriodType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PeriodSummaryRepository {
    fun getSummary(
        startDate: LocalDate,
        endDate: LocalDate,
        periodType: PeriodType,
        periodModel: PeriodModel,
        accountId: Int? = null
    ): Flow<PeriodSummary>
}
