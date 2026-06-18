package com.money.manager.ex.data.mapper

import com.money.manager.ex.data.local.entity.CheckingAccountEntityV1
import com.money.manager.ex.domain.model.Transaction
import com.money.manager.ex.domain.model.TransactionCode
import com.money.manager.ex.domain.model.TransactionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun CheckingAccountEntityV1.toDomain(): Transaction {
    return Transaction(
        id = transId ?: 0,
        accountId = accountId,
        toAccountId = toAccountId,
        payeeId = payeeId,
        transCode = TransactionCode.fromValue(transCode),
        transAmount = transAmount.toDouble(),
        status = TransactionStatus.fromValue(status),
        transactionNumber = transactionNumber,
        notes = notes,
        categoryId = categId,
        transDate = transDate?.let {
            try {
                LocalDate.parse(it.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) {
                LocalDate.now()
            }
        } ?: LocalDate.now(),
        toTransAmount = toTransAmount?.toDouble(),
        payee = "", // TODO: Fetch from Payee table if needed
        category = "" // TODO: Fetch from Category table if needed
    )
}
