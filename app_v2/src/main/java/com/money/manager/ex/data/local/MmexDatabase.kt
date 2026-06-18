package com.money.manager.ex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.money.manager.ex.data.local.converter.BigDecimalConverter
import com.money.manager.ex.data.local.dao.AccountDao
import com.money.manager.ex.data.local.dao.TransactionDao
import com.money.manager.ex.data.local.entity.*

@Database(
    entities = [
        AccountEntityV1::class,
        AssetEntityV1::class,
        BillDepositEntityV1::class,
        BudgetSplitTransactionEntityV1::class,
        BudgetTableEntityV1::class,
        BudgetYearEntityV1::class,
        CategoryEntityV1::class,
        CheckingAccountEntityV1::class,
        CurrencyHistoryEntityV1::class,
        CurrencyFormatEntityV1::class,
        InfoTableEntityV1::class,
        PayeeEntityV1::class,
        SplitTransactionEntityV1::class,
        StockEntityV1::class,
        StockHistoryEntityV1::class,
        ReportEntityV1::class,
        AttachmentEntityV1::class,
        CustomFieldEntityV1::class,
        CustomFieldDataEntityV1::class,
        TransLinkEntityV1::class,
        ShareInfoEntityV1::class,
        TagEntityV1::class,
        TagLinkEntityV1::class,
        DeletedRecordLogEntity::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class MmexDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}
