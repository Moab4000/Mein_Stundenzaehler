package com.example.meinstundenzhler.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shift: Shift): Long

    @Update
    suspend fun update(shift: Shift)

    @Query("SELECT * FROM shifts WHERE monthlyListId = :listId ORDER BY startEpochMillis ASC")
    fun getByMonthlyList(listId: Long): kotlinx.coroutines.flow.Flow<List<Shift>>

    @Query("DELETE FROM shifts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shifts WHERE monthlyListId = :listId")
    suspend fun deleteByMonthlyListId(listId: Long)
}
