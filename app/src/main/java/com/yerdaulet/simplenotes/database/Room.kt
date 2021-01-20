package com.yerdaulet.simplenotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
Database for the offline cache
 */

@Database(entities = [DatabaseNote::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao() : NoteDao

    companion object {
        private lateinit var INSTANCE: NotesDatabase

        fun getDatabase(context: Context): NotesDatabase {
            synchronized(NotesDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java,
                        "notes"
                    ).build()
                }
            }
            return INSTANCE
        }
    }

}
