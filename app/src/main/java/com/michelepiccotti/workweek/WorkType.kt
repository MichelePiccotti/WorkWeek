package com.michelepiccotti.workweek

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_types")
data class WorkType(
    @PrimaryKey(autoGenerate = true)
    val typeId: Int = 0,
    val name: String,        // Esempio: "Lavoro", "Ferie", "Malattia"
    val colorHex: String,    // Esempio: "#4CAF50" (verde), "#F44336" (rosso)
    val isDefault: Boolean = false // Utile per proteggere i tipi predefiniti dall'eliminazione
)
