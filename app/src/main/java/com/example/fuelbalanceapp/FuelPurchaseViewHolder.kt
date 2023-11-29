package com.example.fuelbalanceapp

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FuelPurchaseViewHolder(itemView: View, private val adapter: FuelPurchaseAdapter) : RecyclerView.ViewHolder(itemView) {
    val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
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
