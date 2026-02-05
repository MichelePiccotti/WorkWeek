package com.michelepiccotti.workweek

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityAddRecord : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedTypeId: Int = -1
    private var selectedDate: Long = System.currentTimeMillis()
    private val today = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etHours = findViewById<TextInputEditText>(R.id.etHours)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val typeDropdown = findViewById<AutoCompleteTextView>(R.id.typeDropdown)

        db = AppDatabase.getDatabase(this)

        etDate.setText(sdf.format(Date(selectedDate)))
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    etDate.setText(sdf.format(calendar.time))

                    if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    ) {
                        etDate.setTypeface(null, Typeface.BOLD)
                    } else {
                        etDate.setTypeface(null, Typeface.NORMAL)
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etHours.setText("8")

        lifecycleScope.launch {
            val typesFromDb = withContext(Dispatchers.IO) {
                db.workDao().getAllTypes()
            }

            if (typesFromDb.isNotEmpty()) {
                val adapter = android.widget.ArrayAdapter(
                    this@ActivityAddRecord,
                    android.R.layout.simple_dropdown_item_1line,
                    typesFromDb.map { it.name }
                )
                typeDropdown.setAdapter(adapter)

                val defaultType = typesFromDb.firstOrNull { it.isDefault }
                if (defaultType != null) {
                    selectedTypeId = defaultType.id
                    typeDropdown.setText(defaultType.name, false)
                }

                typeDropdown.setOnItemClickListener { _, _, position, _ ->
                    selectedTypeId = typesFromDb[position].id
                }
            }
        }

        btnSave.setOnClickListener {
            val hoursStr = etHours.text?.toString()?.trim().orEmpty()

            if (hoursStr.isEmpty() || selectedTypeId == -1) {
                Toast.makeText(this, "Inserisci ore e seleziona un tipo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val newRecord = WorkRecord(
                    date = selectedDate,
                    hours = hoursStr.toFloat(),
                    typeId = selectedTypeId
                )
                db.workDao().insertRecord(newRecord)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ActivityAddRecord, "Salvato!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}
