package com.michelepiccotti.workweek

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
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
import java.util.*

class AddRecordActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var selectedTypeId: Int = -1
    private var selectedDate: Long = System.currentTimeMillis() // Default: oggi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        db = AppDatabase.getDatabase(this)

        val etHours = findViewById<TextInputEditText>(R.id.etHours)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val typeDropdown = findViewById<AutoCompleteTextView>(R.id.typeDropdown)

        // Formattatore per mostrare la data in modo leggibile
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // 1. Carichiamo i tipi dal Database per popolare il menu a tendina
        lifecycleScope.launch {
            val typesFromDb = withContext(Dispatchers.IO) {
                db.workDao().getAllTypes()
            }

            if (typesFromDb.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this@AddRecordActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    typesFromDb.map { it.name }
                )
                typeDropdown.setAdapter(adapter)

                typeDropdown.setOnItemClickListener { _, _, position, _ ->
                    selectedTypeId = typesFromDb[position].typeId
                }
            }
        }

        // 2. Gestione del salvataggio
        btnSave.setOnClickListener {
            val hoursStr = etHours.text.toString()

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
                    Toast.makeText(this@AddRecordActivity, "Salvato!", Toast.LENGTH_SHORT).show()
                    finish() // Torna alla Home
                }
            }
        }
    }
}