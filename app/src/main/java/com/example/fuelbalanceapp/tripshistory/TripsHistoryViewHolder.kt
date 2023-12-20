package com.example.fuelbalanceapp.tripshistory

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelbalanceapp.R

class TripsHistoryViewHolder(itemView: View, private val adapter: TripsHistoryAdapter) : RecyclerView.ViewHolder(itemView) {
    val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

    init {
        itemView.setOnLongClickListener {
            showPopupMenu(it)
            true
        }
    }

    private fun showPopupMenu(view: View) {
        val position = adapterPosition
        adapter.showPopupMenu(view, position)
    }
}
