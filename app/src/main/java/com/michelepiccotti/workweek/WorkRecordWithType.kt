package com.michelepiccotti.workweek

import androidx.room.Embedded
import androidx.room.Relation

data class WorkRecordWithType(
    @Embedded
    val record: WorkRecord,
    @Relation(
        parentColumn = "typeId",
        entityColumn = "id"
    )
    val workType: WorkType
)
