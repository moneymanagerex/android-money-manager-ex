package com.money.manager.ex.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TAGLINK_V1",
    foreignKeys = [
        ForeignKey(
            entity = TagEntityV1::class,
            parentColumns = ["TAGID"],
            childColumns = ["TAGID"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["REFTYPE", "REFID", "TAGID"], name = "IDX_TAGLINK")
    ]
)
data class TagLinkEntityV1(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TAGLINKID")
    val tagLinkId: Int? = null,
    @ColumnInfo(name = "REFTYPE")
    val refType: String,
    @ColumnInfo(name = "REFID")
    val refId: Int,
    @ColumnInfo(name = "TAGID")
    val tagId: Int,
    @ColumnInfo(name = "pb_id")
    val pbId: String? = null,
    @ColumnInfo(name = "pb_updated_at")
    val pbUpdatedAt: String? = null,
    @ColumnInfo(name = "pb_is_dirty", defaultValue = "0")
    val pbIsDirty: Int? = 0
)
