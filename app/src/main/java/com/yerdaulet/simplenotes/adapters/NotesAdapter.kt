package com.yerdaulet.simplenotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.yerdaulet.simplenotes.R
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.helper.DataBoundListAdapter
import kotlinx.coroutines.*

class NotesAdapter(private val callback: ((List<Note>, action: String) -> Unit)?) :
        DataBoundListAdapter<Note, NoteItemBinding>(NoteDiffCallback()) {

        private val adapterScope = CoroutineScope(Dispatchers.Default)
        private val selectedItems = ArrayList<Note>()
        var actionMode: ActionMode? = null
        private var multiSelect = false

        override fun createBinding(parent: ViewGroup): NoteItemBinding {
                return DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.note_item, parent, false
                )
        }

        override fun bind(binding: NoteItemBinding, item: Note, position: Int) {
                binding.apply {
                        note = item
                        if(cardlist.size > 0) cardList.clear()
                }
        }


}