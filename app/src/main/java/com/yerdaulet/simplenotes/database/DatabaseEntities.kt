package com.yerdaulet.simplenotes.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yerdaulet.simplenotes.domain.Note


/**
 * Database entities go in this file. These are responsible for reading and writing from the
 * database. This class is immutable.
 */

/**
 * DatabaseNote represents a note entity in the database.
 *
 * @param id        id of the note
 * @param title     title of the note
 * @param text      content of the note
 * @param date      date the note was created/modified
 * @param reminder  date set for a reminder in the note
 * @param started   whether or note the reminder is active
 * @param color     color of the note
 */

@Entity(tableName = "database_note")
data class DatabaseNote(
        @PrimaryKey(autoGenerate = true)
        val id: Int?,
        val title: String,
        val text: String,
        val date: Long?,
        val reminder: Long?,
        val started: Boolean,
        val color: Int
) {
    companion object {
        /**
         * Map domain model to database entity
         *
         * @param note the note to be mapped
         * @return a database entity
         */
        fun toDatabaseEntry(note: Note): DatabaseNote {
            return DatabaseNote(note.id, note.title, note.text, note.date, note.reminder, note.started, note.color)
        }
    }

    /**
     * Map Database entity to domain model
     *
     * @return a domain model
     */
    fun asDomainModelEntry(): Note {
        return Note(id, title, text, date, reminder, started, color)
    }
}

/**
 * Map list of DatabaseNotes to list of domain entities
 *
 * @return a list of Notes
 */
fun List<DatabaseNote>.asDomainModel(): List<Note> {
    return map {
        Note(it.id, it.title, it.text, it.date, it.reminder, it.started, it.color)
    }
}

/**
 * Map list of domain notes to list of DatabaseNotes
 *
 * @return a list of DatabaseNotes
 */
fun List<Note>.toDatabaseList(): List<DatabaseNote> {
    return map {
        DatabaseNote(it.id, it.title, it.text, it.date, it.reminder, it.started, it.color)
    }
}


