package com.yerdaulet.simplenotes.viewmodels

import android.content.Context
import android.text.format.DateUtils
import androidx.databinding.ObservableField
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.yerdaulet.simplenotes.database.asDomainModel
import com.yerdaulet.simplenotes.database.toDatabaseList
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.repository.NoteRepository
import com.yerdaulet.simplenotes.util.UIState
import com.yerdaulet.simplenotes.util.currentDate
import com.yerdaulet.simplenotes.work.cancelAlarm
import com.yerdaulet.simplenotes.work.createSchedule
import kotlinx.coroutines.launch

/**
 * ViewModel for [NoteListFragment]
 */


class NoteListViewModel @ViewModelInject internal constructor(
    private val context: Context,
    private val noteListRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState = ObservableField(UIState.LOADING)

    private var notes = noteListRepository.notes


    val filteredNotes: LiveData<List<Note>> = getSavedFilter().switchMap { filter ->
        when(filter) {
            TODAY -> Transformations.map(notes) {noteList->
                noteList.map{it}
                    .filter{it.reminder != null && DateUtils.isToday(it.reminder!!)}
            }
            UPCOMING -> Transformations.map(notes) {noteList ->
                noteList.map {it}
                    .filter {it.reminder != null && it.reminder!! < currentDate().timeInMillis}
            }
            COMPLETED -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && it.reminder!! < currentDate().timeInMillis }
            }
            else -> notes
        }
    }

    val notesToDelete: LiveData<List<Note>>
        get() = getNotesToDelete()

    /**
     * Delete the notes with given ids from the database and cancel
     * active reminders related to them
     */
    fun deleteNotes() {
        val idList = ArrayList<Int>()
        for (note in getNotesToDelete().value!!) {
            if (note.started && note.reminder!! > currentDate().timeInMillis) {
                cancelAlarm(context, note)
            }
            idList.add(note.id!!)
        }
        viewModelScope.launch {
            noteListRepository.deleteNotes(idList)
        }
    }

    /**
     * Delete a single note and cancel reminders
     */

    fun deleteNote() {
        val note = getNotesToDelete().value!![0]
        if (note.started && note.reminder!! > currentDate().timeInMillis){
            cancelAlarm(context, note)
        }
        viewModelScope.launch {
            noteListRepository.deleteNote(note.id!!)
        }
    }


    /**
     * insert notes and created associated reminders
     */
    fun insertNotes() {
        val notesToInsert = getNotesToDelete().value!!
        for (note in notesToInsert) {
            if (note.started && note.reminder!! > currentDate().timeInMillis){
                 createSchedule(context, note)
                }
        }
        viewModelScope.launch {
            noteListRepository.insertNotes(notesToInsert)
        }
    }

    /**
     * insert single note
     */
    fun insertNote() {
        val note = getNotesToDelete().value!![0]
        if (note.started && note.reminder!! > currentDate().timeInMillis){
            createSchedule(context, note)
        }
        viewModelScope.launch {
            noteListRepository.insertNote(note)
        }
    }

    fun setFilter(num: Int) {
        savedStateHandle.set(FILTER_SAVED_STATE_KEY, num)
    }
    private fun getSavedFilter(): MutableLiveData<Int> {
        return savedStateHandle.getLiveData(FILTER_SAVED_STATE_KEY, NO_FILTER)
    }
    fun setNotesToDelete(noteList: List<Note>){
        savedStateHandle.set(NOTES_TO_DELETE,
            noteList.toDatabaseList().asDomainModel())
    }
    private fun getNotesToDelete(): MutableLiveData<List<Note>> {
        return savedStateHandle.getLiveData(NOTES_TO_DELETE)
    }



    companion object {
        private const val TODAY = 1
        private const val UPCOMING = 2
        private const val COMPLETED = 3
        private const val NO_FILTER = 4
        private const val FILTER_SAVED_STATE_KEY = "FILTER_SAVED_STATE_KEY"
        private const val NOTES_TO_DELETE = "NOTES_TO_DELETE"

    }
}