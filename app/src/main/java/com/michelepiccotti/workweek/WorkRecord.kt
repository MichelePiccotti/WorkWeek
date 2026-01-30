package com.michelepiccotti.workweek

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_records",
    // Definiamo il legame con la tabella WorkType
    foreignKeys = [
        ForeignKey(
            entity = WorkType::class,           // Tabella a cui puntiamo
            parentColumns = ["typeId"],         // Colonna nella tabella WorkType
            childColumns = ["typeId"],          // Colonna in questa tabella (WorkRecord)
            onDelete = ForeignKey.CASCADE      // Se elimino un tipo, elimina i record collegati
        )
    ],
    // Gli indici servono per rendere l'app veloce quando cerca i dati
    indices = [Index(value = ["typeId"])]
)
data class WorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,           // Data salvata come timestamp
    val hours: Float,         // Ore lavorate
    val typeId: Int,          // ID del tipo (collegato a WorkType)
    val overtime: Float = 0f, // Straordinari
    val note: String? = null  // Note opzionali (può essere null)
)