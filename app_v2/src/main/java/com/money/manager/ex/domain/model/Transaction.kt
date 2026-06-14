package com.money.manager.ex.domain.model

import java.time.LocalDate

enum class TransactionCode(val value: String) {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER("Transfer");

    companion object {
        fun fromValue(value: String?): TransactionCode {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: WITHDRAWAL
        }
    }
}

enum class TransactionStatus(val value: String) {
    NORMAL(""),
    RECONCILED("R"),
    VOID("V"),
    FOLLOW_UP("F");

    companion object {
        fun fromValue(value: String?): TransactionStatus {
            return entries.find { it.value == (value ?: "") } ?: NORMAL
        }
    }
}

data class Transaction(
    val id: Int,
    val accountId: Int,
    val toAccountId: Int? = null,
    val payeeId: Int,
    val transCode: TransactionCode,
    val transAmount: Double,
    val status: TransactionStatus = TransactionStatus.NORMAL,
    val transactionNumber: String? = null,
    val notes: String? = null,
    val categoryId: Int? = null,
    val transDate: LocalDate,
    val toTransAmount: Double? = null,
    
    // UI specific/helper fields (keeping some from previous version for convenience if needed)
    val payee: String = "",
    val category: String = ""
)
