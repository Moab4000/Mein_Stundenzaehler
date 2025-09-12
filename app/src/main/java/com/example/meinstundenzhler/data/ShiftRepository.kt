package com.example.meinstundenzhler.data

import kotlinx.coroutines.flow.Flow

class ShiftRepository(private val dao: ShiftDao) {
    suspend fun insert(shift: Shift) = dao.insert(shift)
    suspend fun update(shift: Shift) = dao.update(shift)
    fun getByMonthlyList(listId: Long): Flow<List<Shift>> = dao.getByMonthlyList(listId)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteAllInList(listId: Long) = dao.deleteByMonthlyListId(listId)
}
