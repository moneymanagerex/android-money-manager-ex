package com.money.manager.ex.data.repository

import com.money.manager.ex.domain.model.PeriodElapsed
import com.money.manager.ex.domain.model.PeriodModel
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import com.money.manager.ex.di.Actual
import com.money.manager.ex.di.Forecast
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class PeriodSummaryRepositoryImpl @Inject constructor(
    @Actual private val actualRepository: PeriodSummaryRepository,
    @Forecast private val forecastRepository: PeriodSummaryRepository
) : PeriodSummaryRepository {

    override fun getSummary(
        startDate: LocalDate,
        endDate: LocalDate,
        periodElapsed: PeriodElapsed,
        periodModel: PeriodModel,
        accountId: Int
    ): Flow<PeriodSummary> {
        return when (periodModel) {
            PeriodModel.ACTUAL -> actualRepository.getSummary(startDate, endDate, periodElapsed, periodModel, accountId)
            PeriodModel.FORECAST -> forecastRepository.getSummary(startDate, endDate, periodElapsed, periodModel, accountId)
        }
    }
}
