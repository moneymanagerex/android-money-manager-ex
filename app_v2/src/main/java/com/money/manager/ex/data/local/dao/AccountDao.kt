package com.money.manager.ex.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.money.manager.ex.data.local.pojo.AccountWithCurrency
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM ACCOUNTLIST_V1 WHERE STATUS = 'Open'")
    fun getOpenAccountsWithCurrency(): Flow<List<AccountWithCurrency>>
}
