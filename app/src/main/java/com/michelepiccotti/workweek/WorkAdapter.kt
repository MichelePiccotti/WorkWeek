package com.michelepiccotti.workweek

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class WorkAdapter(private var items: List<WorkRecordWithType>) :
    RecyclerView.Adapter<WorkAdapter.ViewHolder>() {

    var onItemLongClick: ((WorkRecordWithType) -> Unit)? = null

    // Questa classe descrive come sono fatti gli elementi grafici di una riga
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTypeName: TextView = view.findViewById(R.id.tvTypeName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvHours: TextView = view.findViewById(R.id.tvHours)
        val viewColor: View = view.findViewById(R.id.viewColorTag)

        init {
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(items[adapterPosition])
                true
            }
        }
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

        // Data leggibile
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateMillis = item.record.date
        holder.tvDate.text = try {
            sdf.format(Date(dateMillis))
        } catch (e: Exception) {
            "??/??/????"
        }

        // Qui impostiamo il colore che abbiamo salvato nel database
        try {
            holder.viewColor.background.setTint(android.graphics.Color.parseColor(item.workType.colorHex))
        } catch (e: Exception) {
            holder.viewColor.background.setTint(android.graphics.Color.GRAY)
        }
        println("DEBUG: WorkAdapterBinder ${item.workType.name} ${item.record.hours}h  date=${item.record.date}")
    }

    override fun getItemCount(): Int {
        println("DEBUG: WorkAdapterGetItemCount new items ${items.size}")
        return items.size
    }

    // Funzione fondamentale per aggiornare la lista quando aggiungi un record
    fun updateData(newItems: List<WorkRecordWithType>) {
        items = newItems
        notifyDataSetChanged()
        println("DEBUG: WorkAdapterUpdateData new items ${newItems.size}")
    }

}