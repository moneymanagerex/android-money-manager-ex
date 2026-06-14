package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CATEGORY_V1")
data class CategoryEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CATEGID")
    val categId: Int = 0,
    @ColumnInfo(name = "CATEGNAME")
    val categName: String,
    @ColumnInfo(name = "ACTIVE")
    val active: Int?,
    @ColumnInfo(name = "PARENTID")
    val parentId: Int?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
