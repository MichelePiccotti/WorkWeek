package com.michelepiccotti.workweek

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WorkDao {
    // Inserisce un nuovo record di ore
    @Insert
    suspend fun insertRecord(record: WorkRecord)

    // Inserisce un nuovo tipo (Lavoro, Ferie, etc.)
    @Insert
    suspend fun insertType(type: WorkType)

    // Recupera tutti i tipi creati
    @Query("SELECT * FROM work_types")
    suspend fun getAllTypes(): List<WorkType>

    // Recupera i record uniti ai loro tipi (per vedere il nome invece dell'ID)
    @Transaction
    @Query("SELECT * FROM work_records ORDER BY date DESC")
    fun getRecordsWithType(): LiveData<List<WorkRecordWithType>>
}