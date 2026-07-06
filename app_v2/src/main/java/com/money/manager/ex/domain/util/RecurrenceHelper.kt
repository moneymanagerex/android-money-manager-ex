package com.money.manager.ex.domain.util

import com.money.manager.ex.domain.model.RepeatType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object RecurrenceHelper {
    /**
     * Calcola la data successiva in base al tipo di ricorrenza.
     * @param currentDate La data di partenza (l'ultima occorrenza calcolata).
     * @param repeatType Il tipo di ricorrenza (es. WEEKLY, MONTHLY).
     * @param interval L'intervallo per i tipi "EVERY_X_...".
     */
    fun calculateNextDate(currentDate: LocalDate, repeatType: RepeatType, interval: Int = 1): LocalDate {
        return when (repeatType) {
            RepeatType.ONCE -> currentDate
            RepeatType.WEEKLY -> currentDate.plusWeeks(1)
            RepeatType.BIWEEKLY -> currentDate.plusWeeks(2)
            RepeatType.MONTHLY -> currentDate.plusMonths(1)
            RepeatType.BIMONTHLY -> currentDate.plusMonths(2)
            RepeatType.QUARTERLY -> currentDate.plusMonths(3)
            RepeatType.SEMIANNUALLY -> currentDate.plusMonths(6)
            RepeatType.ANNUALLY -> currentDate.plusYears(1)
            RepeatType.FOUR_MONTHS -> currentDate.plusMonths(4)
            RepeatType.FOUR_WEEKS -> currentDate.plusWeeks(4)
            RepeatType.DAILY -> currentDate.plusDays(1)
            RepeatType.IN_X_DAYS, RepeatType.EVERY_X_DAYS -> currentDate.plusDays(interval.toLong())
            RepeatType.IN_X_MONTHS, RepeatType.EVERY_X_MONTHS -> currentDate.plusMonths(interval.toLong())
            RepeatType.MONTHLY_LAST_DAY -> {
                val lastDayOfCurrent = currentDate.with(TemporalAdjusters.lastDayOfMonth())
                if (currentDate.isEqual(lastDayOfCurrent)) {
                    currentDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                } else {
                    lastDayOfCurrent
                }
            }
            RepeatType.MONTHLY_LAST_BUSINESS_DAY -> {
                var nextDate = currentDate.with(TemporalAdjusters.lastDayOfMonth())
                // Se la data corrente è già l'ultimo giorno lavorativo del mese, passiamo al mese successivo
                if (currentDate.isEqual(adjustToBusinessDay(nextDate))) {
                    nextDate = currentDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                }
                adjustToBusinessDay(nextDate)
            }
        }
    }

    private fun adjustToBusinessDay(date: LocalDate): LocalDate {
        var result = date
        while (result.dayOfWeek == DayOfWeek.SATURDAY || result.dayOfWeek == DayOfWeek.SUNDAY) {
            result = result.minusDays(1)
        }
        return result
    }
}
