package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CUSTOMFIELD_V1")
data class CustomFieldEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FIELDID")
    val fieldId: Int = 0,
    @ColumnInfo(name = "REFTYPE")
    val refType: String,
    @ColumnInfo(name = "DESCRIPTION")
    val description: String?,
    @ColumnInfo(name = "TYPE")
    val type: String,
    @ColumnInfo(name = "PROPERTIES")
    val properties: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
