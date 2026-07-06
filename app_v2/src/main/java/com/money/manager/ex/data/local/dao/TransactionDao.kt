package com.money.manager.ex.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.money.manager.ex.data.local.pojo.FinancialSummaryPojo
import com.money.manager.ex.data.local.pojo.TransactionPojo
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * Calcola il totale di entrate e uscite in un intervallo di date.
     * TRANSCODE: 'Deposit' = Entrata, 'Withdrawal' = Uscita.
     * non usiamo i trasferimenti WHEN TRANSCODE = 'Transfer' AND TOACCOUNTID = :accountId THEN TRANSAMOUNT
     *                            WHEN TRANSCODE = 'Transfer' AND ACCOUNTID = :accountId THEN TRANSAMOUNT
     *
     */
    @Query("""
        SELECT 
            SUM(CASE 
                WHEN TRANSCODE = 'Deposit' AND ACCOUNTID = :accountId THEN TRANSAMOUNT
                WHEN TRANSCODE = 'Transfer' AND TOACCOUNTID = :accountId THEN TRANSAMOUNT
                ELSE 0 END) as income,
            SUM(CASE 
                WHEN TRANSCODE = 'Withdrawal' AND ACCOUNTID = :accountId THEN TRANSAMOUNT
                WHEN TRANSCODE = 'Transfer' AND ACCOUNTID = :accountId THEN TRANSAMOUNT
                ELSE 0 END) as expense
        FROM CHECKINGACCOUNT_V1
        WHERE TRANSDATE BETWEEN :startDate AND :endDate
        AND (ACCOUNTID = :accountId OR TOACCOUNTID = :accountId)
        AND STATUS != 'V' 
    """)
    fun getFinancialSummary(startDate: String, endDate: String, accountId: Int): Flow<FinancialSummaryPojo?>

    @Query("""
        WITH RECURSIVE categories(categid, categname, parentid) AS
            (SELECT a.categid, a.categname, a.parentid FROM category_v1 a WHERE parentid = '-1'
                UNION ALL
             SELECT c.categid, r.categname || ':' || c.categname, c.parentid
             FROM categories r, category_v1 c
             WHERE r.categid = c.parentid
             )
        SELECT CHECKINGACCOUNT_V1.*,
               categories.categname,
               PAYEE_V1.PAYEENAME,
			   src.ACCOUNTNAME as ACCOUNTNAME,
			   dst.ACCOUNTNAME as TOACCOUNTNAME
        FROM CHECKINGACCOUNT_V1
        LEFT OUTER JOIN categories ON categories.categid = CHECKINGACCOUNT_V1.CATEGID
        LEFT OUTER JOIN PAYEE_V1 ON PAYEE_V1.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID
		LEFT OUTER JOIN ACCOUNTLIST_V1 AS src ON src.ACCOUNTID = CHECKINGACCOUNT_V1.ACCOUNTID
		LEFT OUTER JOIN ACCOUNTLIST_V1 AS dst ON dst.ACCOUNTID = CHECKINGACCOUNT_V1.TOACCOUNTID
        WHERE (CHECKINGACCOUNT_V1.ACCOUNTID = :accountId OR CHECKINGACCOUNT_V1.TOACCOUNTID = :accountId)
        AND CHECKINGACCOUNT_V1.STATUS != 'V'
        ORDER BY CHECKINGACCOUNT_V1.TRANSDATE DESC, CHECKINGACCOUNT_V1.TRANSID DESC
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int, accountId: Int): Flow<List<TransactionPojo>>
}
