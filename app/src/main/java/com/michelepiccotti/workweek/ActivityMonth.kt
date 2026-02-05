package com.michelepiccotti.workweek

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import androidx.appcompat.app.AlertDialog


class ActivityMonth : AppCompatActivity() {

    private lateinit var rvWorkRecords: RecyclerView
    private lateinit var fabAddRecord: FloatingActionButton
    private lateinit var fabCalendarView: FloatingActionButton
    private lateinit var etMonthDate: TextInputEditText
    private lateinit var tvTotalHours: TextView
    private lateinit var adapter: WorkAdapter

    private var startDateCalendar: Calendar? = null
    private var endDateCalendar: Calendar? = null

    private var currentRecords: List<WorkRecordWithType> = emptyList()

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val addRecordLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                loadRecords()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        rvWorkRecords = findViewById(R.id.rvWorkRecords)
        fabAddRecord = findViewById(R.id.fabAddRecord)
        etMonthDate = findViewById(R.id.etMonthDate)
        tvTotalHours = findViewById(R.id.tvTotalHours)
        fabCalendarView = findViewById(R.id.fabCalendarView)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.rootLayout)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBars.top,      // status bar
                view.paddingRight,
                systemBars.bottom    // navigation bar
            )

            insets
        }


        adapter = WorkAdapter(emptyList())
        rvWorkRecords.layoutManager = LinearLayoutManager(this)
        rvWorkRecords.adapter = adapter

        adapter.onItemLongClick = { item ->
            val options = arrayOf("Modifica", "Cancella")
            AlertDialog.Builder(this)
                .setTitle("Seleziona azione")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> editRecord(item)
                        1 -> deleteRecord(item)
                    }
                }
                .show()
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ActivityMonth)
            DataSeeder.seedWorkTypes(this@ActivityMonth, db)
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

        etMonthDate.setText(sdf.format(sevenDaysAgo.time))
        startDateCalendar = sevenDaysAgo

        etMonthDate.setOnClickListener {
            showDatePicker(null, today) { date, calendar ->
                etMonthDate.setText(date)
                startDateCalendar = calendar
                loadRecords()
            }
        }

        fabAddRecord.setOnClickListener {
            val intent = Intent(this, ActivityAddRecord::class.java)
            intent.putExtra("todayMillis", today.timeInMillis)
            addRecordLauncher.launch(intent)
        }

        tvTotalHours.setOnClickListener {
            val intent = Intent(this, ActivitySummary::class.java).apply {
                putExtra("startDate", startDateCalendar?.timeInMillis ?: 0L)
                putExtra("endDate", endDateCalendar?.timeInMillis ?: 0L)
            }
            startActivity(intent)
        }

        fabCalendarView.setOnClickListener {
            startActivity(Intent(this, ActivityCalendar::class.java))
        }

        NotificationUtils.createNotificationChannel(this)

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        val db = AppDatabase.getDatabase(this)
        val start = startDateCalendar?.timeInMillis ?: 0L
        val end = endDateCalendar?.timeInMillis?.plus(172800000) ?: 0L

        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                db.workDao().getRecordsBetweenDates(start, end)
            }
            currentRecords = records
            adapter.updateData(records)

            val totalHours = records.sumOf { it.record.hours.toDouble() }
            tvTotalHours.text = "Totale ore: %.2f".format(totalHours)
        }
    }

    fun showDatePicker(
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

        minDate?.let { datePicker.datePicker.minDate = it.timeInMillis }
        maxDate?.let { datePicker.datePicker.maxDate = it.timeInMillis }

        datePicker.show()
    }

    private fun editRecord(item: WorkRecordWithType) {
        val intent = Intent(this, ActivityAddRecord::class.java)
        intent.putExtra("recordId", item.record.id)
        addRecordLauncher.launch(intent)
    }

    private fun deleteRecord(item: WorkRecordWithType) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.workDao().delete(item.record)
            withContext(Dispatchers.Main) {
                loadRecords()
            }
        }
    }

    // ---------- MENU ----------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_export_csv -> exportCsv()
            R.id.menu_export_pdf -> exportPdf()
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exportCsv() {
        if (currentRecords.isEmpty()) {
            AlertDialog.Builder(this)
                .setMessage("Nessun dato da esportare.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val file = CsvUtils.exportCsv(this, currentRecords)
        AlertDialog.Builder(this)
            .setTitle("Export CSV")
            .setMessage("File salvato in:\n${file.absolutePath}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportPdf() {
        if (currentRecords.isEmpty()) {
            AlertDialog.Builder(this)
                .setMessage("Nessun dato da esportare.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val sb = StringBuilder()
        sb.append("Riepilogo ore\n\n")
        currentRecords.forEach {
            val dateStr = sdf.format(it.record.date)
            sb.append("$dateStr - ${it.workType.name}: ${it.record.hours} ore\n")
        }

        val file = PdfExporter.exportSummary(this, sb.toString())
        AlertDialog.Builder(this)
            .setTitle("Export PDF")
            .setMessage("PDF salvato in:\n${file.absolutePath}")
            .setPositiveButton("OK", null)
            .show()
    }
}

