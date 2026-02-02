package com.michelepiccotti.workweek

import android.graphics.Color
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CalendarAdapter(
    private val days: List<DayCell>,
    private val onClick: (DayCell) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = days[position]
        val cal = Calendar.getInstance().apply { timeInMillis = item.date }

        holder.tvDay.text = cal.get(Calendar.DAY_OF_MONTH).toString()

        val color = when {
            item.totalHours == 0.0 -> Color.TRANSPARENT
            item.totalHours <= 4 -> Color.parseColor("#FFCDD2")
            item.totalHours <= 8 -> Color.parseColor("#FFF9C4")
            else -> Color.parseColor("#C8E6C9")
        }

        holder.tvDay.background.setTint(color)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}
