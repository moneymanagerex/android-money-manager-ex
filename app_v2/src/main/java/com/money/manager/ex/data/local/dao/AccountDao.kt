package com.money.manager.ex.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.money.manager.ex.data.local.pojo.AccountWithBalancePojo
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("""
        SELECT
            ACCOUNTLIST_V1.*,
            (INITIALBAL + ifnull(T1.TOTAL, 0)) AS TOTAL,
            (INITIALBAL + ifnull(T1.reconciled, 0)) AS RECONCILED,
            (INITIALBAL + ifnull(T1.TOTAL, 0)) * ifnull(CURRENCYFORMATS_V1.BASECONVRATE, 1) AS TOTALBASECONVRATE,
            (INITIALBAL + ifnull(T1.reconciled, 0)) * ifnull(CURRENCYFORMATS_V1.BASECONVRATE, 1) AS RECONCILEDBASECONVRATE
        FROM ACCOUNTLIST_V1 
        LEFT JOIN ( 
            SELECT accountid, SUM(total) as total, SUM(reconciled) as reconciled
            FROM (
                -- Withdrawals
                SELECT accountid, transcode, sum(case when status in ('R', 'F', 'D', '') then -transamount else 0 end) as total,
                    sum(case when status = 'R' then -transamount else 0 end) as reconciled
                FROM checkingaccount_v1
                WHERE transcode in ('Withdrawal') and (deletedtime is null or deletedtime = '')
                GROUP BY accountid, transcode
        
                UNION ALL
        
                SELECT accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then transamount else 0 end) as total,
                    sum(case when status = 'R' then transamount else 0 end) as reconciled
                FROM checkingaccount_v1
                WHERE transcode in ('Deposit') and (deletedtime is null or deletedtime = '')
                GROUP BY accountid, transcode
        
                UNION ALL
        
                SELECT accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then -transamount else 0 end) as total,
                    sum(case when status = 'R' then -transamount else 0 end) as reconciled
                FROM checkingaccount_v1
                WHERE transcode in ('Transfer') and (deletedtime is null or deletedtime = '')
                GROUP BY accountid, transcode
        
                UNION ALL
        
                SELECT toaccountid AS accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then totransamount else 0 end) as total,
                    sum(case when status = 'R' then totransamount else 0 end) as reconciled
                FROM checkingaccount_v1
                WHERE transcode in ('Transfer') and toaccountid <> -1 and (deletedtime is null or deletedtime = '')
                GROUP BY toaccountid, transcode
        
                UNION ALL
        
                -- Investments
                SELECT HeldAt as accountid,
                    'Deposit' as transcode,
                    sum(NumShares * CurrentPrice) as total,
                    sum(NumShares * CurrentPrice) as reconciled
                FROM stock_v1
                GROUP BY accountid, transcode
            )  t
            GROUP BY accountid
        ) T1 ON ACCOUNTLIST_V1.ACCOUNTID = T1.ACCOUNTID 
        LEFT JOIN CURRENCYFORMATS_V1 ON ACCOUNTLIST_V1.CURRENCYID = CURRENCYFORMATS_V1.CURRENCYID
        WHERE ACCOUNTLIST_V1.STATUS = 'Open'
    """)
    fun getOpenAccountsWithBalance(): Flow<List<AccountWithBalancePojo>>
}
