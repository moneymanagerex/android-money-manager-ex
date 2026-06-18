package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "BILLSDEPOSITS_V1",
    indices = [
        Index(value = ["ACCOUNTID", "TOACCOUNTID"], name = "IDX_BILLSDEPOSITS_ACCOUNT")
    ]
)
data class BillDepositEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BDID")
    val bdId: Int? = null,
    @ColumnInfo(name = "ACCOUNTID")
    val accountId: Int,
    @ColumnInfo(name = "TOACCOUNTID")
    val toAccountId: Int?,
    @ColumnInfo(name = "PAYEEID")
    val payeeId: Int,
    @ColumnInfo(name = "TRANSCODE")
    val transCode: String,
    @ColumnInfo(name = "TRANSAMOUNT", typeAffinity = ColumnInfo.REAL)
    val transAmount: BigDecimal,
    @ColumnInfo(name = "STATUS")
    val status: String?,
    @ColumnInfo(name = "TRANSACTIONNUMBER")
    val transactionNumber: String?,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "CATEGID")
    val categId: Int?,
    @ColumnInfo(name = "TRANSDATE")
    val transDate: String?,
    @ColumnInfo(name = "FOLLOWUPID")
    val followUpId: Int?,
    @ColumnInfo(name = "TOTRANSAMOUNT", typeAffinity = ColumnInfo.REAL)
    val toTransAmount: BigDecimal?,
    @ColumnInfo(name = "REPEATS")
    val repeats: Int?,
    @ColumnInfo(name = "NEXTOCCURRENCEDATE")
    val nextOccurrenceDate: String?,
    @ColumnInfo(name = "NUMOCCURRENCES")
    val numOccurrences: Int?,
    @ColumnInfo(name = "COLOR")
    val color: Int? = -1,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
