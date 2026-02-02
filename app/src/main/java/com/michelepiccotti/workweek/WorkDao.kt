package com.michelepiccotti.workweek

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WorkDao {
    @Insert
    suspend fun insertRecord(record: WorkRecord)
    @Delete
    suspend fun delete(record: WorkRecord)

    // Inserisce un nuovo tipo (Lavoro, Ferie, etc.)
    @Insert
    suspend fun insertType(type: WorkType)
    @Query("SELECT * FROM work_types")
    suspend fun getAllTypes(): List<WorkType>

    // Recupera i record uniti ai loro tipi (per vedere il nome invece dell'ID)
    @Transaction
    @Query("SELECT * FROM work_records ORDER BY date DESC")
    fun getRecordsWithType(): LiveData<List<WorkRecordWithType>>

    @Transaction
    @Query("SELECT * FROM work_records WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getRecordsBetweenDates(start: Long, end: Long): List<WorkRecordWithType>

    @Query("""
        SELECT t.id as typeId, t.name as typeName, t.colorHex as colorHex, SUM(w.hours) as totalHours
        FROM work_records w
        JOIN work_types t ON w.typeId = t.id
        WHERE w.date BETWEEN :startDate AND :endDate
        GROUP BY t.id, t.name, t.colorHex
    """)
    suspend fun getHoursSumByTypeWithName(startDate: Long, endDate: Long): List<HoursByType>
    @Query("SELECT * FROM work_records ORDER BY date ASC")
    suspend fun getAllRecords(): List<WorkRecord>
    @Query("SELECT * FROM work_types WHERE id = :typeId LIMIT 1")
    suspend fun getTypeById(typeId: Int): WorkType?
    @Query("SELECT * FROM work_types")
    suspend fun getAllWorkTypes(): List<WorkType>
}