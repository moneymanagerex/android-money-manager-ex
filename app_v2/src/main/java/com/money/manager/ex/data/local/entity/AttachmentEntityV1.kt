package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ATTACHMENT_V1",
    indices = [
        Index(value = ["REFTYPE", "REFID"], name = "IDX_ATTACHMENT_REF")
    ]
)
data class AttachmentEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ATTACHMENTID")
    val attachmentId: Int = 0,
    @ColumnInfo(name = "REFTYPE")
    val refType: String,
    @ColumnInfo(name = "REFID")
    val refId: Int,
    @ColumnInfo(name = "DESCRIPTION")
    val description: String?,
    @ColumnInfo(name = "FILENAME")
    val fileName: String,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
