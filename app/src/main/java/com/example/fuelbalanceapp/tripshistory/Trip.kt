package com.example.fuelbalanceapp.tripshistory

import org.threeten.bp.LocalDate

data class Trip(val date: LocalDate, val distance: Double, var id: Long = 0)
