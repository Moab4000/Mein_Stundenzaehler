package com.example.meinstundenzhler.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MonthlyList): Long

    @Update
    suspend fun update(entity: MonthlyList)          // <-- NEU

    @Query("SELECT * FROM monthly_lists ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MonthlyList>>

    @Query("SELECT * FROM monthly_lists WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<MonthlyList?>

    @Query("DELETE FROM monthly_lists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
