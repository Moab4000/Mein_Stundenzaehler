package com.example.meinstundenzhler.data

import kotlinx.coroutines.flow.Flow

class MonthlyListRepository(private val dao: MonthlyListDao) {
    suspend fun insert(item: MonthlyList) = dao.insert(item)
    fun getAll(): Flow<List<MonthlyList>> = dao.getAll()
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun update(item: MonthlyList) = dao.update(item)

    fun getById(id: Long) = dao.getById(id)

}
