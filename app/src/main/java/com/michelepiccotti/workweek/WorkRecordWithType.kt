package com.michelepiccotti.workweek

import androidx.room.Embedded
import androidx.room.Relation

data class WorkRecordWithType(
    @Embedded
    val record: WorkRecord,

    @Relation(
        parentColumn = "typeId",    // Il nome della colonna in WorkRecord
        entityColumn = "typeId"     // Il nome della colonna in WorkType
    )
    val workType: WorkType
)