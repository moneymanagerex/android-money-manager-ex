package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TAG_V1")
data class TagEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TAGID")
    val tagId: Int = 0,
    @ColumnInfo(name = "TAGNAME")
    val tagName: String,
    @ColumnInfo(name = "ACTIVE")
    val active: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
