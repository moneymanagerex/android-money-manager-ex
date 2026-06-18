package com.money.manager.ex.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.money.manager.ex.data.local.entity.CheckingAccountEntityV1
import com.money.manager.ex.data.local.pojo.FinancialSummaryPojo
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * Calcola il totale di entrate e uscite in un intervallo di date.
     * TRANSCODE: 'Deposit' = Entrata, 'Withdrawal' = Uscita.
     * Se accountId è null, calcola per tutti i conti.
     */
    @Query("""
        SELECT 
            SUM(CASE WHEN TRANSCODE = 'Deposit' THEN TRANSAMOUNT ELSE 0 END) as income,
            SUM(CASE WHEN TRANSCODE = 'Withdrawal' THEN TRANSAMOUNT ELSE 0 END) as expense
        FROM CHECKINGACCOUNT_V1
        WHERE TRANSDATE BETWEEN :startDate AND :endDate
        AND (ACCOUNTID = :accountId)
        AND STATUS != 'V' 
    """)
    fun getFinancialSummary(startDate: String, endDate: String, accountId: Int?): Flow<FinancialSummaryPojo?>

    @Query("""
        SELECT * FROM CHECKINGACCOUNT_V1
        WHERE (ACCOUNTID = :accountId)
        AND STATUS != 'V'
        ORDER BY TRANSDATE DESC, TRANSID DESC
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int, accountId: Int?): Flow<List<CheckingAccountEntityV1>>
}
