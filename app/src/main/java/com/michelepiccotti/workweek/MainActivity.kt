package com.michelepiccotti.workweek

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: WorkAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        val recyclerView = findViewById<RecyclerView>(R.id.rvWorkRecords)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddRecord)

        adapter = WorkAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Importa: import androidx.lifecycle.lifecycleScope e kotlinx.coroutines.launch
        lifecycleScope.launch {
            val dao = database.workDao()
            val types = dao.getAllTypes()

            // Se la tabella dei tipi è vuota, aggiungiamo quelli base
            if (types.isEmpty()) {
                dao.insertType(WorkType(name = "Lavoro", colorHex = "#2196F3")) // Blu
                dao.insertType(WorkType(name = "Ferie", colorHex = "#4CAF50"))  // Verde
                dao.insertType(WorkType(name = "Malattia", colorHex = "#F44336")) // Rosso
            }
        }
        // 3. Osserviamo il Database: ogni volta che i dati cambiano, l'UI si aggiorna!
        database.workDao().getRecordsWithType().observe(this, Observer { records ->
            adapter.updateData(records)
        })


        fab.setOnClickListener {
            // Questo comando dice ad Android: "Parti dalla MainActivity e apri la AddRecordActivity"
            val intent = android.content.Intent(this, AddRecordActivity::class.java)
            startActivity(intent)
        }
    }
}