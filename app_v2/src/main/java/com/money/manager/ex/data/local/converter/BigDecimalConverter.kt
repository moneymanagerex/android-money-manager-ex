package com.money.manager.ex.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        return value?.let { BigDecimal.valueOf(it) }
    }

    @TypeConverter
    fun toDouble(value: BigDecimal?): Double? {
        return value?.toDouble()
    }
}
