package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ACCOUNTLIST_V1")
data class AccountEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ACCOUNTID")
    val accountId: Int = 0,
    @ColumnInfo(name = "ACCOUNTNAME")
    val accountName: String,
    @ColumnInfo(name = "ACCOUNTTYPE")
    val accountType: String,
    @ColumnInfo(name = "ACCOUNTNUM")
    val accountNum: String?,
    @ColumnInfo(name = "STATUS")
    val status: String,
    @ColumnInfo(name = "NOTES")
    val notes: String?,
    @ColumnInfo(name = "HELDAT")
    val heldAt: String?,
    @ColumnInfo(name = "WEBSITE")
    val website: String?,
    @ColumnInfo(name = "CONTACTINFO")
    val contactInfo: String?,
    @ColumnInfo(name = "ACCESSINFO")
    val accessInfo: String?,
    @ColumnInfo(name = "INITIALBAL")
    val initialBal: Double?,
    @ColumnInfo(name = "INITIALDATE")
    val initialDate: String?,
    @ColumnInfo(name = "FAVORITEACCT")
    val favoriteAcct: String,
    @ColumnInfo(name = "CURRENCYID")
    val currencyId: Int,
    @ColumnInfo(name = "STATEMENTLOCKED")
    val statementLocked: Int?,
    @ColumnInfo(name = "STATEMENTDATE")
    val statementDate: String?,
    @ColumnInfo(name = "MINIMUMBALANCE")
    val minimumBalance: Double?,
    @ColumnInfo(name = "CREDITLIMIT")
    val creditLimit: Double?,
    @ColumnInfo(name = "INTERESTRATE")
    val interestRate: Double?,
    @ColumnInfo(name = "PAYMENTDUEDATE")
    val paymentDueDate: String?,
    @ColumnInfo(name = "MINIMUMPAYMENT")
    val minimumPayment: Double?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
