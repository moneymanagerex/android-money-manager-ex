package com.money.manager.ex.domain.model

/**
 * Enumeration of account types ported from legacy AccountTypes.java
 */
enum class AccountType(val title: String) {
    CASH("Cash"),
    CHECKING("Checking"),
    INVESTMENT("Investment"),
    TERM("Term"),
    CREDIT_CARD("Credit Card"),
    LOAN("Loan"),
    SHARES("Shares");

    companion object {
        fun fromTitle(title: String): AccountType? {
            return values().find { it.title.equals(title, ignoreCase = true) }
        }
    }
}
