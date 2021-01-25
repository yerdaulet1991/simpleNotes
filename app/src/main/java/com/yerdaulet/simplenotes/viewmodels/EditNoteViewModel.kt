package com.yerdaulet.simplenotes.viewmodels

import android.app.Activity
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.yerdaulet.simplenotes.database.DatabaseNote.Companion.toDatabaseEntry
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.repository.NoteRepository
import com.yerdaulet.simplenotes.util.ReminderCompletion
import com.yerdaulet.simplenotes.util.ReminderState
import com.yerdaulet.simplenotes.util.currentDate
import com.yerdaulet.simplenotes.util.selectColor
import com.yerdaulet.simplenotes.work.cancelAlarm
import com.yerdaulet.simplenotes.work.createSchedule
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EditNoteViewModel @AssistedInject constructor(
    private val context: Context,
    private val noteRepository: NoteRepository,
    @Assisted private val selectedNoteId: Int?
) :
    ViewModel() {
    private lateinit var selectedNote: Note
    private lateinit var scheduledNote: Note

    val reminderState = ObservableField(ReminderState.NO_REMINDER)
    val reminderCompletion = ObservableField(ReminderCompletion.ONGOING)

    private var _noteBeingModified = MutableLiveData<Note?>()
    val noteBeingModified: LiveData<Note> get() = _noteBeingModified

    private var _mIsEdit = MutableLiveData<Boolean>()
    val mIsEdit: LiveData<Boolean> get() = _mIsEdit

    init {
        if (selectedNoteId == -1) {
            onNewNote()
            selectedNote = noteRepository.emptyNote
            _noteBeingModified.value = selectedNote
        } else {
            onNoteInserted()
            viewModelScope.launch {
                noteRepository.getNote(selectedNoteId!!).collect { note ->
                    _noteBeingModified.value = note
                    selectedNote = toDatabaseEntry(note!!).asDomainModelEntry()
                }
            }
        }
    }


    private val _isChanged: MutableLiveData<Boolean>
        get() = if (_mIsEdit.value!!) {
            MutableLiveData(_noteBeingModified.value != selectedNote)
        } else {
            MutableLiveData(_noteBeingModified.value != noteRepository.emptyNote
                .copy(color = _noteBeingModified.value?.color!!))
        }

    val isChanged: LiveData<Boolean> get() = _isChanged

    /**
     * set time and date for Note's reminder
     *
     * @param dateTime the date for reminder
     */
    fun setDateTime(dateTime: Long) {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = dateTime)
    }

    /**
     * set Note's color
     *
     * @param activity the note's containing activity
     */
    fun pickColor(activity: Activity){
        selectColor(activity, _noteBeingModified.value!!)
    }

    /**
     * Check if a note includes a reminder and create the reminder if the time has not elapsed.
     */
    fun scheduleReminder() {
        if (_noteBeingModified.value!!.reminder != null && _noteBeingModified.value!!.reminder!! > currentDate().timeInMillis) {
            if (_mIsEdit.value!!) {
                createSchedule(context, _noteBeingModified.value!!)
                updateNote(_noteBeingModified.value!!)
            } else {
                runBlocking {
                    noteRepository.getLatestNote().collect { note ->
                        scheduledNote = note!!
                    }
                }
                createSchedule(context, scheduledNote)
                updateNote(scheduledNote)
            }
            reminderCompletion.set(ReminderCompletion.ONGOING)
        }
    }
    /**
     * Delete a note from the database and cancel the active reminder
     */
    fun deleteNote() {
        if (_noteBeingModified.value!!.started) {
            cancelReminder()
        }
        viewModelScope.launch {
            noteRepository.deleteNote(_noteBeingModified.value!!.id!!)
        }
    }

    /**
     * Cancel an active reminder associated with a note.
     */
    fun cancelReminder() {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = null, started = false)
        cancelAlarm(context, _noteBeingModified.value!!)
    }

    /**
     * Check if a note is being edited and update it if true. Insert a new note
     * in the database if false
     */
    fun saveNote() {
        if (!_mIsEdit.value!!) {
            insertNote(_noteBeingModified.value!!)
        } else {
            updateNote(_noteBeingModified.value!!)
        }
    }

    private fun insertNote(note: Note) {
        val newNote = note.copy(date = currentDate().timeInMillis)
        runBlocking {
            noteRepository.insertNote(newNote)
        }
    }

    private fun updateNote(note: Note) {
        val updatedNote = note.copy(date = currentDate().timeInMillis)
        viewModelScope.launch {
            noteRepository.updateNote(updatedNote)
        }
    }

    private fun onNoteInserted() {
        TODO("Not yet implemented")
    }

    private fun onNewNote() {
        _mIsEdit.value = false
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(selectedNoteId: Int?): EditNoteViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            selectedNoteId: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(selectedNoteId) as T
            }
        }
    }


}