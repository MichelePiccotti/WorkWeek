package com.michelepiccotti.workweek

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var db: AppDatabase

    private var records: List<WorkRecord> = emptyList()
    private var workTypes: Map<Int, WorkType> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        db = AppDatabase.getDatabase(this)

        // carica dati dal DB
        lifecycleScope.launch {
            records = withContext(Dispatchers.IO) {
                db.workDao().getAllRecords()
            }
            workTypes = withContext(Dispatchers.IO) {
                db.workDao().getAllWorkTypes().associateBy { it.id }
            }

            setupCalendar()
        }
    }

    private fun setupCalendar() {
        val zoneId = ZoneId.systemDefault()

        // calcola ore totali per ogni giorno
        val dayTotals: Map<LocalDate, Double> = records.groupBy {
            Instant.ofEpochMilli(it.date)
                .atZone(zoneId)
                .toLocalDate()
        }.mapValues { it.value.sumOf { rec -> rec.hours.toDouble() } }

        // imposta intervallo di mesi
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(6)
        val endMonth = currentMonth.plusMonths(6)

        calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY)
        calendarView.scrollToMonth(currentMonth)

        // binder dei giorni
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {

            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                if (day.position != DayPosition.MonthDate) {
                    container.textView.text = ""
                    container.view.setBackgroundColor(Color.TRANSPARENT)
                    container.view.setOnClickListener(null)
                    return
                }

                container.textView.text = day.date.dayOfMonth.toString()

                val total = dayTotals[day.date] ?: 0.0
                val color = when {
                    total == 0.0 -> Color.TRANSPARENT
                    total <= 4 -> Color.parseColor("#FFCDD2") // rosso chiaro
                    total <= 8 -> Color.parseColor("#FFF9C4") // giallo
                    else -> Color.parseColor("#C8E6C9") // verde chiaro
                }

                container.view.setBackgroundColor(color)

                container.view.setOnClickListener {
                    showDayDetails(day.date)
                }
            }
        }
    }

    private fun showDayDetails(date: LocalDate) {
        val zoneId = ZoneId.systemDefault()
        val dayRecords = records.filter {
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() == date
        }

        if (dayRecords.isEmpty()) return

        val msg = dayRecords.joinToString("\n") { rec ->
            val type = workTypes[rec.typeId]?.name ?: "Sconosciuto"
            "$type: ${rec.hours} ore"
        }

        AlertDialog.Builder(this)
            .setTitle("Ore del ${date.dayOfMonth}/${date.monthValue}/${date.year}")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    // contenitore per la view del giorno
    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.tvDay)
    }
}

