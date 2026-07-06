package com.money.manager.ex.domain.model

enum class RepeatType(val value: Int) {
    ONCE(0),
    WEEKLY(1),
    BIWEEKLY(2),
    MONTHLY(3),
    BIMONTHLY(4),
    QUARTERLY(5),
    SEMIANNUALLY(6),
    ANNUALLY(7),
    FOUR_MONTHS(8),
    FOUR_WEEKS(9),
    DAILY(10),
    IN_X_DAYS(11),
    IN_X_MONTHS(12),
    EVERY_X_DAYS(13),
    EVERY_X_MONTHS(14),
    MONTHLY_LAST_DAY(15),
    MONTHLY_LAST_BUSINESS_DAY(16);

    companion object {
        fun fromInt(value: Int): RepeatType = entries.find { it.value == value } ?: ONCE
    }
}
