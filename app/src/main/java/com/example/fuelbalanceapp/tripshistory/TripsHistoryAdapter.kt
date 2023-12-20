package com.example.fuelbalanceapp.tripshistory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelbalanceapp.R
import org.threeten.bp.format.DateTimeFormatter

class TripsHistoryAdapter(private val trips: MutableList<Trip>, private val context: Context) :
    RecyclerView.Adapter<TripsHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripsHistoryViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: TripsHistoryViewHolder, position: Int) {
        val trip = trips[position]

        holder.distanceTextView.text = trip.distance.toString() + " km"

        // Format the LocalDate to a String.
        val formattedDate: String = trip.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        holder.dateTextView.text = formattedDate
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteTrip(position)
                    true
                }
                // Add more menu items if needed.
                else -> false
            }
        }

        popupMenu.show()
    }

    fun addTrip(trip: Trip) {
        trips.add(0, trip) // Add at the beginning to show the latest trip first.
        notifyItemInserted(0)
    }

    private fun deleteTrip(position: Int) {
        deleteTripFromDb(trips[position].id)
        trips.removeAt(position)
        notifyItemRemoved(position)

        updateEmptyViewVisibility()
    }

    private fun updateEmptyViewVisibility() {
        (context as ViewTripsHistory).updateEmptyViewVisibility()
    }

    private fun deleteTripFromDb(id: Long) {
        (context as ViewTripsHistory).deleteTripFromDb(id)
    }
}