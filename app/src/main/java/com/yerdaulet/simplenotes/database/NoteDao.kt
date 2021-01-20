package com.yerdaulet.simplenotes.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
NoteDao Interface with helper methods
*/

@Dao
interface NoteDao {
    /**
     * Observe list of notes
     *
     * @return all notes
     */
    @Query("select * from database_note order by date desc")
    fun getAllNotes(): LiveData<List<DatabaseNote>>

    /**
     * Retrieve the latest inserted note from the database
     *
     * @return the latest inserted note
     */
    @Query("select * from database_note order by date desc limit 1")
    fun getNote(): DatabaseNote

    /**
     * Delete the Note with the given id from the database
     *
     * @param noteId the id of the note to be deleted
     */
    @Query("delete from database_note where id = :noteId")
    fun deteleNote(noteId:Int)

    /**
     * Delete the Notes with the given ids from the database
     *
     * @param idList list of ids of notes to be deleted
     */
    @Query("delete from database_note where id in (:idList)")
    fun deleteSomeNotes(idList: List<Int>)

    /**
     * Insert a single Note into the database
     *
     * @param note the note to be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: DatabaseNote?)

    /**
     * Insert a list of notes into the database
     *
     * @param notes list of notes to be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotesList(notes: List<DatabaseNote>)

    /**
     * Update contents of a Note in the database
     *
     * @param note note to be updated
     */
    @Update
    fun update(note: DatabaseNote)

    /**
     * Retrieve a single note with the specified id from the database
     *
     * @param noteId id of the note to be retrieved
     * @return the note withe the specified id
     */
    @Query("select * from database_note where id = :noteId")
    fun get(noteId: Int): DatabaseNote
}