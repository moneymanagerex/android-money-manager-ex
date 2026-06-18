package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "ACCOUNTLIST_V1",
    indices = [
        Index(value = ["ACCOUNTTYPE"], name = "IDX_ACCOUNTLIST_ACCOUNTTYPE")
    ]
)
data class AccountEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ACCOUNTID")
    val accountId: Int? = null,
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
    @ColumnInfo(name = "INITIALBAL", typeAffinity = ColumnInfo.REAL)
    val initialBal: BigDecimal?,
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
    @ColumnInfo(name = "MINIMUMBALANCE", typeAffinity = ColumnInfo.REAL)
    val minimumBalance: BigDecimal?,
    @ColumnInfo(name = "CREDITLIMIT", typeAffinity = ColumnInfo.REAL)
    val creditLimit: BigDecimal?,
    @ColumnInfo(name = "INTERESTRATE", typeAffinity = ColumnInfo.REAL)
    val interestRate: BigDecimal?,
    @ColumnInfo(name = "PAYMENTDUEDATE")
    val paymentDueDate: String?,
    @ColumnInfo(name = "MINIMUMPAYMENT", typeAffinity = ColumnInfo.REAL)
    val minimumPayment: BigDecimal?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
