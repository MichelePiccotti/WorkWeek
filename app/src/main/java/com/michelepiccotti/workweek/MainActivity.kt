package com.michelepiccotti.workweek

import android.app.DatePickerDialog
import android.widget.TextView
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var rvWorkRecords: RecyclerView
    private lateinit var fabAddRecord: FloatingActionButton
    private lateinit var btCalendarView : Button
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var adapter: WorkAdapter
    private var startDateCalendar: Calendar? = null
    private var endDateCalendar: Calendar? = null
    private lateinit var tvTotalHours: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvWorkRecords = findViewById(R.id.rvWorkRecords)
        fabAddRecord = findViewById(R.id.fabAddRecord)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        tvTotalHours = findViewById(R.id.tvTotalHours)
        btCalendarView = findViewById(R.id.btCalendarView)

        // RecyclerView
        adapter = WorkAdapter(emptyList())
        rvWorkRecords.layoutManager = LinearLayoutManager(this)
        rvWorkRecords.adapter = adapter
        adapter.onItemLongClick = { item ->
            // Mostra un dialog con opzioni Modifica / Cancella
            val options = arrayOf("Modifica", "Cancella")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seleziona azione")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> editRecord(item)    // Modifica
                        1 -> deleteRecord(item)  // Cancella
                    }
                }
                .show()
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            DataSeeder.seedWorkTypes(this@MainActivity, db)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val sevenDaysAgo = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_MONTH, -7)
        }
        // Imposta date di default nei TextInputEditText
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etStartDate.setText(sdf.format(sevenDaysAgo.time))
        startDateCalendar = sevenDaysAgo
        etEndDate.setText(sdf.format(today.time))
        endDateCalendar = today

        etStartDate.setOnClickListener {
            showDatePicker(null, maxDate = today) { date, calendar ->
                etStartDate.setText(date)
                startDateCalendar = calendar
                loadRecords()
            }
        }

        etEndDate.setOnClickListener {
            showDatePicker(minDate = startDateCalendar, maxDate = today) { date, calendar ->
                etEndDate.setText(date)
                endDateCalendar = calendar
            }
            loadRecords()
        }

        // FAB per aggiungere nuovo record
        fabAddRecord.setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java)
            intent.putExtra("todayMillis", today.timeInMillis)
            addRecordLauncher.launch(intent)
        }

        tvTotalHours.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java).apply {
                putExtra("startDate", startDateCalendar?.timeInMillis ?: 0L)
                putExtra("endDate", endDateCalendar?.timeInMillis ?: 0L)
            }
            startActivity(intent)
        }

        btCalendarView.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private val addRecordLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                loadRecords()
            }
        }
    private fun loadRecords() {
        val db = AppDatabase.getDatabase(this)

        // Calcola intervallo date
        val start = startDateCalendar?.timeInMillis ?: 0L
        val end = endDateCalendar?.timeInMillis  ?: 0L

        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.workDao().getRecordsBetweenDates(start, end)
            }
            println("DEBUG: Caricati ${records.size} record da $start a $end")

            adapter.updateData(records) // ora ogni item ha record + workType

            // CALCOLO DEL TOTALE ORE
            val totalHours = records.sumOf { it.record.hours.toDouble() }
            tvTotalHours.text = "Totale ore: %.2f".format(totalHours)
        }
    }

    public fun showDatePicker(
        minDate: Calendar? = null,
        maxDate: Calendar? = null,
        onDateSelected: (String, Calendar) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val formatted = "%02d/%02d/%04d".format(d, m + 1, y)
                onDateSelected(formatted, selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        minDate?.let {
            datePicker.datePicker.minDate = it.timeInMillis
        }
        maxDate?.let {
            datePicker.datePicker.maxDate = it.timeInMillis
        }

        datePicker.show()
    }
    private fun editRecord(item: WorkRecordWithType) {
        val intent = Intent(this, AddRecordActivity::class.java)
        intent.putExtra("recordId", item.record.id)
        addRecordLauncher.launch(intent)
    }

    private fun deleteRecord(item: WorkRecordWithType) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.workDao().delete(item.record)
            // ricarica lista sul thread principale
            withContext(Dispatchers.Main) {
                loadRecords()
            }
        }
    }
}
