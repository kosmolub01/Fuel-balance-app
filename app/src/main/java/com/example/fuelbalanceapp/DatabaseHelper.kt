package com.example.fuelbalanceapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fuel_balance_app_database"
        private const val DATABASE_VERSION = 1
        private const val TRIPS_TABLE_NAME = "trips_table"
        const val FUEL_PURCHASES_TABLE_NAME = "fuel_purchases_table"

        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_DISTANCE = "distance"
        const val COLUMN_FUEL_AMOUNT = "fuel_amount"

        private const val CREATE_TRIPS_TABLE =
            "CREATE TABLE $TRIPS_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_DATE TEXT," +
                    "$COLUMN_DISTANCE REAL)"

        private const val CREATE_FUEL_PURCHASES_TABLE =
            "CREATE TABLE $FUEL_PURCHASES_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_DATE TEXT," +
                    "$COLUMN_FUEL_AMOUNT REAL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "onCreate")
        db.execSQL(CREATE_TRIPS_TABLE)
        db.execSQL(CREATE_FUEL_PURCHASES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
    }
}
