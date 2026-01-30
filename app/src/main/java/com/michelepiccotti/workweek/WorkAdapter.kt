package com.michelepiccotti.workweek

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WorkAdapter(private var items: List<WorkRecordWithType>) :
    RecyclerView.Adapter<WorkAdapter.ViewHolder>() {

    // Questa classe descrive come sono fatti gli elementi grafici di una riga
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTypeName: TextView = view.findViewById(R.id.tvTypeName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvHours: TextView = view.findViewById(R.id.tvHours)
        val viewColor: View = view.findViewById(R.id.viewColorTag)
    }

    // Crea fisicamente la riga partendo dal file XML item_work_record
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_record, parent, false)
        return ViewHolder(view)
    }

    // Prende i dati dal database e li scrive nelle TextView
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTypeName.text = item.workType.name
        holder.tvHours.text = "${item.record.hours}h"

        // Trasforma il timestamp (Long) in una data leggibile (es. 28/01/2026)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.record.date))

        // Qui impostiamo il colore che abbiamo salvato nel database
        try {
            holder.viewColor.background.setTint(android.graphics.Color.parseColor(item.workType.colorHex))
        } catch (e: Exception) {
            holder.viewColor.background.setTint(android.graphics.Color.GRAY)
        }
    }

    override fun getItemCount() = items.size

    // Funzione fondamentale per aggiornare la lista quando aggiungi un record
    fun updateData(newItems: List<WorkRecordWithType>) {
        items = newItems
        notifyDataSetChanged()
    }
}