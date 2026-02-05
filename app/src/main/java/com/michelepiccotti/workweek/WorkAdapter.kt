package com.michelepiccotti.workweek

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkAdapter(private var items: List<WorkRecordWithType>) :
    RecyclerView.Adapter<WorkAdapter.ViewHolder>() {

    var onItemLongClick: ((WorkRecordWithType) -> Unit)? = null

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTypeName.text = item.workType.name
        holder.tvHours.text = "${item.record.hours}h"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.record.date))

        try {
            holder.viewColor.setBackgroundColor(Color.parseColor(item.workType.colorHex))
        } catch (e: Exception) {
            holder.viewColor.setBackgroundColor(Color.GRAY)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WorkRecordWithType>) {
        items = newItems
        notifyDataSetChanged()
    }
}
