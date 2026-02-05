package com.michelepiccotti.workweek

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object CsvUtils {

    fun exportCsv(context: Context, records: List<WorkRecordWithType>): File {
        val sb = StringBuilder()
        sb.append("date,type,hours,note\n")
        records.forEach {
            sb.append("${it.record.date},${it.workType.name},${it.record.hours},${it.record.note ?: ""}\n")
        }

        val file = File(context.getExternalFilesDir(null), "workweek_export.csv")
        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
        return file
    }
}
