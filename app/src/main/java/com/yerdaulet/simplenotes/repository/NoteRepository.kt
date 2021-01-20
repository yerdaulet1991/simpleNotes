package com.yerdaulet.simplenotes.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.yerdaulet.simplenotes.database.NoteDao
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.database.DatabaseNote.Companion.toDatabaseEntry
import com.yerdaulet.simplenotes.database.asDomainModel
import com.yerdaulet.simplenotes.database.toDatabaseList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    val notes: LiveData<List<Note>> by lazy {
        Transformations.map(noteDao.getAllNotes()) {
            it.asDomainModel()
        }
    }

    val emptyNote: Note
        get() {
            return Note()
        }

    /**
     * Delete the Notes with the included ids from the database
     *
     * @param idList list of ids of notes to be deleted
     */
    suspend fun deleteNotes(idList: List<Int>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteSomeNotes(idList)
        }
    }

    /**
     * Insert a list of Notes into the database
     *
     * @param noteList list of notes to be inserted
     */
    suspend fun insertNotes(noteList: List<Note>) {
        withContext(Dispatchers.IO) {
            noteDao.insertNotesList(noteList.toDatabaseList())
        }
    }

    /**
     * Retrieve a single note with the specified id from the database
     *
     * @param noteId id of the note to be retrieved
     * @return a single note via a flow collector
     */
    suspend fun getNote(noteId: Int): Flow<Note?> {
        return flow {
            emit(noteDao.get(noteId).asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Retrieve the latest inserted note from the database
     *
     * @return the latest inserted note via a flow collector
     */
    suspend fun getLatestNote(): Flow<Note?> {
        return flow {
            emit(noteDao.getNote().asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Insert a single note into the database
     *
     * @param note the note to be inserted
     */
    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insert(toDatabaseEntry(note))
        }
    }

    /**
     * Update contents of a Note in the database
     *
     * @param note the note to be updated
     */
    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.update(toDatabaseEntry(note))
        }
    }

    /**
     * Delete the Note with the specified id from the database
     *
     * @param id id of the note to be deleted
     */
    suspend fun deleteNote(id: Int) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(id)
        }
    }
}