package com.michelepiccotti.workweek

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_types")
data class WorkType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false
)

