package com.yerdaulet.simplenotes.adapters

import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.yerdaulet.simplenotes.R
import com.yerdaulet.simplenotes.databinding.NoteItemBinding
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.helper.DataBoundListAdapter
import com.yerdaulet.simplenotes.ui.notelist.NoteListFragment.Companion.ACTION_DELETE
import com.yerdaulet.simplenotes.ui.notelist.NoteListFragment.Companion.ACTION_FAB_HIDE
import com.yerdaulet.simplenotes.ui.notelist.NoteListFragment.Companion.ACTION_FAB_SHOW
import com.yerdaulet.simplenotes.ui.notelist.NoteListFragment.Companion.ACTION_SHARE
import com.yerdaulet.simplenotes.ui.notelist.NoteListFragmentDirections
import kotlinx.coroutines.*

class NotesAdapter(private val callback: ((List<Note>, action: String) -> Unit)?) :
        DataBoundListAdapter<Note, NoteItemBinding>(NoteDiffCallback()) {
        private val adapterScope = CoroutineScope(Dispatchers.Default)
        private val selectedItems = ArrayList<Note>()
        var actionMode: ActionMode? = null
        private var multiSelect = false
        private var allNotes = ArrayList<Note?>()
        private var cardList = ArrayList<ImageView>()
        private var showAddAllIcon = false

        override fun createBinding(parent: ViewGroup): NoteItemBinding {
                return DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.note_item, parent, false
                )
        }

        override fun bind(binding: NoteItemBinding, item: Note, position: Int) {
                binding.apply {
                        note = item
                        if (cardList.size > 0) cardList.clear()
                        repeat(allNotes.size) {
                                cardList.add(binding.checkedImage)
                        }
                        binding.checkedImage.isVisible = selectedItems.contains(item)
                        root.setOnClickListener {
                                binding.note?.let { note ->
                                        selectItem(note, binding.checkedImage, it)
                                }
                        }
                        root.setOnLongClickListener {
                                actionMode = it.startActionMode(ActionModeCallback())
                                binding.note?.let { note ->
                                        callback?.invoke(
                                                listOf(),
                                                ACTION_FAB_HIDE
                                        )
                                        selectItem(note, binding.checkedImage, it)
                                }
                                return@setOnLongClickListener true
                        }
                }
        }

        private fun selectItem(note: Note?, checkCircle: ImageView?, view: View?) {
                if (multiSelect) {
                        if (selectedItems.contains(note)) {
                                selectedItems.remove(note)
                                checkCircle?.isVisible = false
                                if (selectedItems.size == 0) {
                                        callback?.invoke(listOf(), ACTION_FAB_SHOW)
                                        checkCircle?.isVisible = false
                                        actionMode?.finish()
                                }
                        } else {
                                selectedItems.add(note!!)
                                checkCircle?.isVisible = true
                        }
                        if (selectedItems.size > 0) {
                                actionMode?.title = selectedItems.size.toString()
                        }
                        showAddAllIcon = allNotes.size != selectedItems.size
                        actionMode?.invalidate()
                } else {
                        navigateToNote(note?.id, view!!)
                }
        }

        private fun navigateToNote(id: Int?, view: View) {
                val direction =
                        NoteListFragmentDirections.actionNoteListFragmentToEditNoteFragment(id!!)
                view.findNavController().navigate(direction)
        }

        fun submitToList(list: List<Note>) {
                adapterScope.launch {
                        withContext(Dispatchers.Main) {
                                allNotes.clear()
                                allNotes.addAll(list)
                                submitList(list)
                        }
                }
        }

        inner class ActionModeCallback : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        multiSelect = true
                        mode?.menuInflater?.inflate(R.menu.menu_list_action_bar, menu)
                        return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        menu?.findItem(R.id.action_bar_add_all)?.isVisible = showAddAllIcon
                        menu?.findItem(R.id.action_bar_remove_all)?.isVisible = !showAddAllIcon
                        menu?.findItem(R.id.action_bar_share)?.isVisible = selectedItems.size == 1
                        return true
                }

                override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
                        when (menuItem?.itemId) {
                                R.id.action_bar_share -> {
                                        callback?.invoke(selectedItems, ACTION_SHARE)
                                        mode?.finish()
                                }
                                R.id.action_bar_delete -> {
                                        callback?.invoke(selectedItems, ACTION_DELETE)
                                }
                                R.id.action_bar_remove_all -> {
                                        mode?.finish()
                                }
                                R.id.action_bar_add_all -> {
                                        showAddAllIcon = false
                                        mode?.invalidate()
                                        if (allNotes.isNotEmpty()) {
                                                selectedItems.clear()
                                                allNotes.zip(cardList) { note, checkCircle ->
                                                        selectItem(note, checkCircle, null)
                                                }
                                                notifyDataSetChanged()
                                        }
                                }
                        }
                        return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                        callback?.invoke(
                                listOf(),
                                ACTION_FAB_SHOW
                        )
                        actionMode = null
                        multiSelect = false
                        selectedItems.clear()
                        showAddAllIcon = false
                        cardList.clear()
                        notifyDataSetChanged()
                }

        }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
        }
}