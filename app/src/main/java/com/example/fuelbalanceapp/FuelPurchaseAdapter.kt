package com.example.fuelbalanceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

class FuelPurchaseAdapter(private val fuelPurchases: MutableList<FuelPurchase>, private val context: Context) :
    RecyclerView.Adapter<FuelPurchaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelPurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_purchase, parent, false)
        return FuelPurchaseViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FuelPurchaseViewHolder, position: Int) {
        val fuelPurchase = fuelPurchases[position]

        holder.amountTextView.text = fuelPurchase.amount
        holder.dateTextView.text = fuelPurchase.date
    }

    override fun getItemCount(): Int {
        return fuelPurchases.size
    }

    fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteItem(position)
                    true
                }
                // Add more menu items if needed.
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteItem(position: Int) {
        fuelPurchases.removeAt(position)
        notifyItemRemoved(position)

        updateEmptyViewVisibility()
    }

    private fun updateEmptyViewVisibility() {
        (context as AddFuelPurchase).updateEmptyViewVisibility()
    }
}