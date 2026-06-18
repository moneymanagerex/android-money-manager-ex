package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "CUSTOMFIELDDATA_V1",
    indices = [
        Index(value = ["FIELDID", "REFID"], name = "IDX_CUSTOMFIELDDATA_REF")
    ]
)
data class CustomFieldDataEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FIELDATADID")
    val fieldDataId: Int = 0,
    @ColumnInfo(name = "FIELDID")
    val fieldId: Int,
    @ColumnInfo(name = "REFID")
    val refId: Int,
    @ColumnInfo(name = "CONTENT")
    val content: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
