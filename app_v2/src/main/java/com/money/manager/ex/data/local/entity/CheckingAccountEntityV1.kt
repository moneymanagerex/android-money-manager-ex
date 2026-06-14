package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CHECKINGACCOUNT_V1")
data class CheckingAccountEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TRANSID")
    val transId: Int = 0,
    @ColumnInfo(name = "ACCOUNTID")
    val accountId: Int,
    @ColumnInfo(name = "TOACCOUNTID")
    val toAccountId: Int?,
    @ColumnInfo(name = "PAYEEID")
    val payeeId: Int,
    @ColumnInfo(name = "TRANSCODE")
    val transCode: String,
    @ColumnInfo(name = "TRANSAMOUNT")
    val transAmount: Double,
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
    @ColumnInfo(name = "LASTUPDATEDTIME")
    val lastUpdatedTime: String?,
    @ColumnInfo(name = "DELETEDTIME")
    val deletedTime: String?,
    @ColumnInfo(name = "FOLLOWUPID")
    val followUpId: Int?,
    @ColumnInfo(name = "TOTRANSAMOUNT")
    val toTransAmount: Double?,
    @ColumnInfo(name = "COLOR")
    val color: Int? = -1,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
