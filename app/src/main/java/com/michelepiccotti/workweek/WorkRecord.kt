package com.michelepiccotti.workweek

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkType::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["typeId"])]
)
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val hours: Float,
    val typeId: Int,
    val overtime: Float = 0f,
    val note: String? = null
)
