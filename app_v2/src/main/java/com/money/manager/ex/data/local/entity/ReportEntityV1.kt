package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "REPORT_V1")
data class ReportEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "REPORTID")
    val reportId: Int = 0,
    @ColumnInfo(name = "REPORTNAME")
    val reportName: String,
    @ColumnInfo(name = "GROUPNAME")
    val groupName: String?,
    @ColumnInfo(name = "ACTIVE")
    val active: Int?,
    @ColumnInfo(name = "SQLCONTENT")
    val sqlContent: String?,
    @ColumnInfo(name = "LUACONTENT")
    val luaContent: String?,
    @ColumnInfo(name = "TEMPLATECONTENT")
    val templateContent: String?,
    @ColumnInfo(name = "DESCRIPTION")
    val description: String?,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty")
    val pbIsDirty: Int = 0
)
