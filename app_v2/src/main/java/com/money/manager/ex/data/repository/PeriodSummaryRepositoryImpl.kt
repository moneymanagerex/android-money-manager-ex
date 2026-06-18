package com.money.manager.ex.data.repository

import com.money.manager.ex.data.local.DatabaseManager
import com.money.manager.ex.domain.model.FinancialValues
import com.money.manager.ex.domain.model.PeriodModel
import com.money.manager.ex.domain.model.PeriodShift
import com.money.manager.ex.domain.model.PeriodSummary
import com.money.manager.ex.domain.model.PeriodType
import com.money.manager.ex.domain.repository.PeriodSummaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PeriodSummaryRepositoryImpl @Inject constructor(
    private val databaseManager: DatabaseManager
) : PeriodSummaryRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSummary(
        startDate: LocalDate,
        endDate: LocalDate,
        periodType: PeriodType,
        periodModel: PeriodModel,
        accountId: Int?
    ): Flow<PeriodSummary> {
        return databaseManager.database.flatMapLatest { db ->
            if (db == null) {
                return@flatMapLatest flowOf(
                    PeriodSummary(
                        values = FinancialValues(),
                        model = periodModel,
                        type = periodType,
                        shift = PeriodShift.CURRENT,
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            }

            val startStr = startDate.format(dateFormatter)
            val endStr = endDate.format(dateFormatter)

            db.transactionDao().getFinancialSummary(startStr, endStr, accountId)
                .map { pojo ->
                    PeriodSummary(
                        values = FinancialValues(
                            income = pojo?.income ?: BigDecimal.ZERO,
                            expense = pojo?.expense ?: BigDecimal.ZERO
                        ),
                        model = periodModel,
                        type = periodType,
                        shift = PeriodShift.CURRENT, // Lo shift è implicito nelle date passate
                        startDate = startDate,
                        endDate = endDate
                    )
                }
        }
    }
}
