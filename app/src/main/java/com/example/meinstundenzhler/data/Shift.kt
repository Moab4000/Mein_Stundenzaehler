package com.example.meinstundenzhler.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",
    indices = [Index("monthlyListId")]
)
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthlyListId: Long,        // FK: verweist auf MonthlyList.id
    val startEpochMillis: Long,     // Startzeit in Millisekunden (UTC)
    val endEpochMillis: Long,       // Endzeit in Millisekunden (UTC)
    val breakMinutes: Int = 0,      // Pausen in Minuten
    val note: String? = null,       // optional: Notiz
    val createdAt: Long = System.currentTimeMillis()
)
