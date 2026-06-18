package com.money.manager.ex.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

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

enum class PeriodShift {
    PREVIOUS, CURRENT, NEXT
}

data class FinancialValues(
    val income: BigDecimal = BigDecimal.ZERO,
    val expense: BigDecimal = BigDecimal.ZERO
)

data class PeriodSummary(
    val values: FinancialValues,
    val model: PeriodModel,
    val type: PeriodType,
    val shift: PeriodShift,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        /**
         * Factory method per creare un PeriodSummary calcolando automaticamente
         * le date di inizio/fine e recuperando i valori finanziari.
         */
        fun create(
            model: PeriodModel,
            type: PeriodType,
            shift: PeriodShift,
            referenceDate: LocalDate = LocalDate.now()
        ): PeriodSummary {

            // 1. Calcola le date esatte in base alla data di riferimento, al tipo di periodo e allo shift
            val (startDate, endDate) = calculateDates(referenceDate, type, shift)

            // 2. Calcola o recupera i valori finanziari (income/expense)
            // Questa logica potrebbe poi essere spostata in un Use Case o Repository dedicato
            val values = FinancialValues(income = BigDecimal.ZERO, expense = BigDecimal.ZERO)

            // 3. Ritorna l'istanza finale della data class
            return PeriodSummary(
                values = values,
                model = model,
                type = type,
                shift = shift,
                startDate = startDate,
                endDate = endDate
            )
        }

        private fun calculateDates(
            referenceDate: LocalDate,
            type: PeriodType,
            shift: PeriodShift
        ): Pair<LocalDate, LocalDate> {
            // Esempio di implementazione per il calcolo.
            // Qui dovrai inserire la logica per spostare la `referenceDate`
            // nel passato o nel futuro in base a `shift` e `type`.

            var targetDate = referenceDate
            var start = targetDate
            var end = targetDate

            val mod = when (shift) {
                PeriodShift.PREVIOUS -> -1L
                PeriodShift.NEXT -> 1L
                PeriodShift.CURRENT -> 0L
            }

            when (type) {
                PeriodType.MONTH -> {
                    targetDate = targetDate.plusMonths(mod)
                    start = targetDate.withDayOfMonth(1)
                    end = targetDate.with(TemporalAdjusters.lastDayOfMonth())
                }
                PeriodType.WEEK -> {
                    targetDate = targetDate.plusWeeks(mod )
                    start = targetDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    end = targetDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                }
                PeriodType.QUARTER -> {
                    targetDate = targetDate.plusMonths(mod * 3)
                    val month = ((targetDate.monthValue - 1) / 3) * 3 + 1
                    start = targetDate.withMonth(month).withDayOfMonth(1)
                    end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth())
                }
                PeriodType.FOUR_MONTH -> {
                    targetDate = targetDate.plusMonths(mod * 4)
                    val month = ((targetDate.monthValue - 1) / 4) * 4 + 1
                    start = targetDate.withMonth(month).withDayOfMonth(1)
                    end = start.plusMonths(3).with(TemporalAdjusters.lastDayOfMonth())
                }
                PeriodType.HALF_YEAR -> {
                    targetDate = targetDate.plusMonths(mod * 6)
                    val month = ((targetDate.monthValue - 1) / 6) * 6 + 1
                    start = targetDate.withMonth(month).withDayOfMonth(1)
                    end = start.plusMonths(5).with(TemporalAdjusters.lastDayOfMonth())
                }
                PeriodType.YEAR -> {
                    targetDate = targetDate.plusYears(mod)
                    start = targetDate.withDayOfYear(1)
                    end = targetDate.with(TemporalAdjusters.lastDayOfYear())
                }
                PeriodType.FISCAL_YEAR -> {
                    // TODO: Recuperare financialYearStartDay/Month dai settings. 
                    // Per ora default 1 Gennaio (uguale a YEAR).
                    targetDate = targetDate.plusYears(mod)
                    start = targetDate.withDayOfYear(1)
                    end = targetDate.with(TemporalAdjusters.lastDayOfYear())
                }
            }

            return Pair(start, end)
        }
    }
}
