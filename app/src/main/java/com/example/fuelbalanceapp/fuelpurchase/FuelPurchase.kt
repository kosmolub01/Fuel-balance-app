package com.example.fuelbalanceapp.fuelpurchase

import org.threeten.bp.LocalDate

data class FuelPurchase(val date: LocalDate, val amount: Double, var id: Long = 0)
