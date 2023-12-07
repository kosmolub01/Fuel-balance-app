package com.example.fuelbalanceapp.fuelpurchase

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelbalanceapp.R
import org.threeten.bp.format.DateTimeFormatter

class FuelPurchaseAdapter(private val fuelPurchases: MutableList<FuelPurchase>, private val context: Context) :
    RecyclerView.Adapter<FuelPurchaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelPurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_purchase, parent, false)
        return FuelPurchaseViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: FuelPurchaseViewHolder, position: Int) {
        val fuelPurchase = fuelPurchases[position]

        holder.amountTextView.text = fuelPurchase.amount.toString() + " l"

        // Format the LocalDate to a String.
        val formattedDate: String = fuelPurchase.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        holder.dateTextView.text = formattedDate
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
                    deleteFuelPurchase(position)
                    true
                }
                // Add more menu items if needed.
                else -> false
            }
        }

        popupMenu.show()
    }

    fun addFuelPurchase(fuelPurchase: FuelPurchase) {
        fuelPurchases.add(0, fuelPurchase) // Add at the beginning to show the latest purchase first.
        notifyItemInserted(0)
    }

    private fun deleteFuelPurchase(position: Int) {
        deleteFuelPurchaseFromDb(fuelPurchases[position].id)
        fuelPurchases.removeAt(position)
        notifyItemRemoved(position)

        updateEmptyViewVisibility()
    }

    private fun updateEmptyViewVisibility() {
        (context as AddFuelPurchase).updateEmptyViewVisibility()
    }

    private fun deleteFuelPurchaseFromDb(id: Long) {
        (context as AddFuelPurchase).deleteFuelPurchaseFromDb(id)
    }
}