package com.example.meinstundenzhler.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_lists")
data class MonthlyList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val monthIndex: Int,        // 0..11 (Januar = 0)
    val hourlyWage: Double,     // €/h
    val monthlyIncome: Double?, // € (optional)
    val previousDebt: Double,   // Übertrag vom Vormonat (kann + oder - sein)
    val createdAt: Long = System.currentTimeMillis()
)
