package com.michelepiccotti.workweek

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SummaryAdapter : ListAdapter<HoursByType, SummaryAdapter.SummaryViewHolder>(object : DiffUtil.ItemCallback<HoursByType>() {

    override fun areItemsTheSame(oldItem: HoursByType, newItem: HoursByType) = false
    override fun areContentsTheSame(oldItem: HoursByType, newItem: HoursByType) = false
})
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType = itemView.findViewById<TextView>(R.id.tvType)
        private val tvHours = itemView.findViewById<TextView>(R.id.tvHours)

        fun bind(item: HoursByType) {
            tvType.text = item.typeName
            tvHours.text = "${item.totalHours} ore"
            try {
                tvType.setTextColor(Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                tvType.setTextColor(Color.BLACK)
            }
            println("DEBUG: SummaryAdapter new item ${item.typeName}")
        }
    }
}
