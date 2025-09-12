package com.example.meinstundenzhler.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MonthlyList::class, Shift::class], // Shift hinzugefügt
    version = 2,                                   // Version erhöht
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monthlyListDao(): MonthlyListDao
    abstract fun shiftDao(): ShiftDao                     // NEU

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stundenzaehler.db"
                )
                    .fallbackToDestructiveMigration() // in DEV: löscht DB bei Schema-Änderung
                    .build().also { INSTANCE = it }
            }
    }
}
