package com.example.meinstundenzhler.data

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun addNote(note: Note) {
        noteDao.insert(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getById(id)
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    fun getAllNotes() = noteDao.getAll()
}
