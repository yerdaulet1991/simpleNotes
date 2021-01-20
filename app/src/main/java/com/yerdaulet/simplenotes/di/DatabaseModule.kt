package com.yerdaulet.simplenotes.di

import android.content.Context
import com.yerdaulet.simplenotes.database.NoteDao
import com.yerdaulet.simplenotes.database.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule{
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): NotesDatabase {
        return NotesDatabase.getDatabase(context)
    }

    @Provides
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao()
    }
}