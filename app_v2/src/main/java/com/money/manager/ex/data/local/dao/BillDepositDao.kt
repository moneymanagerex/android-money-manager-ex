package com.money.manager.ex.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.money.manager.ex.data.local.entity.BillDepositEntityV1
import com.money.manager.ex.data.local.pojo.FinancialSummaryPojo
import com.money.manager.ex.domain.model.RepeatType
import com.money.manager.ex.domain.util.RecurrenceHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Dao
abstract class BillDepositDao {

    /**
     * Calcola il riepilogo finanziario (entrate/uscite) per un dato account in un intervallo di date.
     * Non può essere delegato al database perché BILLSDEPOSITS_V1 contiene solo la data successiva;
     * dobbiamo espandere le ricorrenze tramite getForecastTransactions.
     */
    fun getFinancialSummary(startDate: String, endDate: String, accountId: Int): Flow<FinancialSummaryPojo?> {
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val start = try { LocalDate.parse(startDate, dateFormatter) } catch (e: Exception) { null }
        val end = try { LocalDate.parse(endDate, dateFormatter) } catch (e: Exception) { null }
        
        if (start == null || end == null) return kotlinx.coroutines.flow.flowOf(null)

        // Usiamo un limite molto alto per assicurarci di prendere tutte le occorrenze nell'intervallo temporale
        return getForecastTransactions(limit = Int.MAX_VALUE, dateTo = end, accountId = accountId).map { expandedList ->
            var income = java.math.BigDecimal.ZERO
            var expense = java.math.BigDecimal.ZERO

            for (entity in expandedList) {
                val occurrenceDate = LocalDate.parse(entity.nextOccurrenceDate!!, dateFormatter)
                
                // Filtriamo solo quelle che rientrano nello startDate (l'endDate è già gestito da getForecastTransactions)
                if (!occurrenceDate.isBefore(start)) {
                    when (entity.transCode) {
                        "Deposit" -> income = income.add(entity.transAmount)
                        "Withdrawal" -> expense = expense.add(entity.transAmount)
                        "Transfer" -> {
                            // Se l'account cercato è quello di destinazione, è un'entrata (income)
                            if (entity.toAccountId == accountId) {
                                income = income.add(entity.toTransAmount)  // usiamo toTransamount in caso di valute diverse
                            } else {
                                // Altrimenti è un'uscita (expense) verso un altro account
                                expense = expense.add(entity.transAmount)
                            }
                        }
                    }
                }
            }

            FinancialSummaryPojo(income, expense)
        }
    }

    @Query("""
            SELECT * FROM BILLSDEPOSITS_V1
                     WHERE (ACCOUNTID = :accountId OR TOACCOUNTID = :accountId)
                       AND STATUS != 'V'        
    """)
    protected abstract fun getRawBillDeposits(accountId: Int): Flow<List<BillDepositEntityV1>>

    /**
     * Genera le transazioni previsionali (forecast) per un dato account.
     * Applica la logica di ricorrenza differenziata in base alla semantica di NUMOCCURRENCES.
     */
    fun getForecastTransactions(limit: Int, dateTo: LocalDate, accountId: Int): Flow<List<BillDepositEntityV1>> {
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        return getRawBillDeposits(accountId).map { rawList ->
            val resultList = mutableListOf<BillDepositEntityV1>()

            for (entity in rawList) {
                val nextDateStr = entity.nextOccurrenceDate ?: continue
                if (nextDateStr.isBlank()) continue
                
                var loopDate = try {
                    LocalDate.parse(nextDateStr, dateFormatter)
                } catch (e: Exception) {
                    continue
                }
                
                // 1. Aggiungiamo la prima occorrenza (quella registrata nel DB)
                if (!loopDate.isAfter(dateTo)) {
                    resultList.add(entity.copy(nextOccurrenceDate = loopDate.format(dateFormatter)))
                } else {
                    continue
                }

                // 2. Analisi ricorrenza
                val numOcc = entity.numOccurrences ?: 0
                val repeatInt = entity.repeats ?: 0
                val freq = repeatInt % 100
                val repeatType = RepeatType.fromInt(freq)

                if (repeatType == RepeatType.ONCE) continue

                // Determiniamo se NUMOCCURRENCE indica l'intervallo (infinito) o il conteggio residuo
                val isInfiniteType = repeatType == RepeatType.IN_X_DAYS || 
                                     repeatType == RepeatType.EVERY_X_DAYS ||
                                     repeatType == RepeatType.IN_X_MONTHS ||
                                     repeatType == RepeatType.EVERY_X_MONTHS

                if (isInfiniteType) {
                    // Semantica: INTERVALLO. Si assume senza scadenza, numOcc è il passo X.
                    val interval = if (numOcc <= 0) 1 else numOcc
                    
                    while (true) {
                        val nextDate = RecurrenceHelper.calculateNextDate(loopDate, repeatType, interval)
                        // Safety check: se la data non avanza, interrompiamo per evitare loop infiniti
                        if (!nextDate.isAfter(loopDate)) break
                        loopDate = nextDate

                        if (loopDate.isAfter(dateTo)) break
                        
                        resultList.add(entity.copy(nextOccurrenceDate = loopDate.format(dateFormatter)))
                        
                        // Safety break per evitare liste eccessive da una singola regola
                        if (resultList.size > limit * 5) break 
                    }
                } else {
                    // Semantica: CONTEGGIO. numOcc indica quante occorrenze mancano (inclusa la prima).
                    // -1 indica nessuna scadenza.
                    // Vincolo: se -1, 0 o 1 non generiamo occorrenze successive alla prima.
                    if (numOcc <= 1 && numOcc != -1) continue

                    var occurrencesLeft = numOcc - 1
                    while (numOcc == -1 || occurrencesLeft > 0) {
                        val nextDate = RecurrenceHelper.calculateNextDate(loopDate, repeatType, 1)
                        // Safety check: se la data non avanza, interrompiamo per evitare loop infiniti
                        if (!nextDate.isAfter(loopDate)) break
                        loopDate = nextDate

                        if (loopDate.isAfter(dateTo)) break
                        
                        resultList.add(entity.copy(nextOccurrenceDate = loopDate.format(dateFormatter)))
                        occurrencesLeft--
                    }
                }
            }

            // 3. Appiattimento, ordinamento e applicazione del limite totale
            resultList.sortedBy { it.nextOccurrenceDate }
                .take(limit)
        }
    }
}
