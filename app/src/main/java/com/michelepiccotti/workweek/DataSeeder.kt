package com.michelepiccotti.workweek

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataSeeder {

    suspend fun seedWorkTypes(context: Context, db: AppDatabase) {
        withContext(Dispatchers.IO) {
            val workTypeCount = db.workDao().getAllTypes().size
            if (workTypeCount == 0) {
                val json = context.assets.open("work_types.json")
                    .bufferedReader()
                    .use { it.readText() }

                val type = object : TypeToken<List<WorkType>>() {}.type
                val workTypes: List<WorkType> = Gson().fromJson(json, type)

                workTypes.forEach { db.workDao().insertType(it) }
            }
        }
    }
}
