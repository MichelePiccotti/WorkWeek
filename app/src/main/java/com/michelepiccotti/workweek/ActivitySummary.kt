package com.michelepiccotti.workweek

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitySummary : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        db = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewSummary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SummaryAdapter()
        recyclerView.adapter = adapter

        val startDate = intent.getLongExtra("startDate", 0L)
        val endDate = intent.getLongExtra("endDate", System.currentTimeMillis()).plus(172800000)

        lifecycleScope.launch {
            val summary = withContext(Dispatchers.IO) {
                db.workDao().getHoursSumByTypeWithName(startDate, endDate)
            }
            adapter.submitList(summary)
        }
    }
}
