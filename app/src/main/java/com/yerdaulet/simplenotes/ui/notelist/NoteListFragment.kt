package com.yerdaulet.simplenotes.ui.notelist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.yerdaulet.simplenotes.R
import com.yerdaulet.simplenotes.adapters.NotesAdapter
import com.yerdaulet.simplenotes.databinding.NoteListFragmentBinding
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.util.UIState
import com.yerdaulet.simplenotes.util.calculateNoOfColumns
import com.yerdaulet.simplenotes.viewmodels.NoteListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

/**
 * A Fragment representing the list of user-created notes
 */

@AndroidEntryPoint
class NoteListFragment : Fragment(), AdapterView.OnItemSelectedListener{

    private val noteListViewModel: NoteListViewModel by viewModels()
    private lateinit var uiScope: CoroutineScope
    private var _binding: NoteListFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotesAdapter
    private var _layout: ConstraintLayout? = null
    private val layout get() = _layout!!


    private lateinit var viewModel: NoteListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            .setDrawerLockMode(LOCK_MODE_UNLOCKED)
        _binding = NoteListFragmentBinding.inflate(inflater, container, false)

        adapter = NotesAdapter {list, action ->
            when (action) {
                ACTION_FAB_HIDE -> {
                    binding.fab.hide()
                }
                ACTION_FAB_SHOW -> {
                    binding.fab.show()
                }
                ACTION_SHARE -> {
                    shareNote(list[0])
                }
                ACTION_DELETE -> {
                    noteListViewModel.setNotesToDelete(list)
                    openAlertDialog()
                }
            }
        }

        observeViewModel()

        binding.apply {
            lifecycleOwner = this@NoteListFragment
            uiState = noteListViewModel.uiState
            noteList.adapter = adapter
            noteList.layoutManager =
                GridLayoutManager(context, calculateNoOfColumns(requireContext(), 180))
        }

        return binding.root
    }

    private fun observeViewModel() {
        noteListViewModel.filteredNotes.observe(viewLifecycleOwner) { noteList ->
            noteList?.let {
                if (noteList.isNotEmpty()) {
                    noteListViewModel.uiState.set(UIState.HAS_DATA)
                } else {
                    noteListViewModel.uiState.set(UIState.EMPTY)
                }
                adapter.submitList(noteList)
                activity?.invalidateOptionsMenu()
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener{
            findNavController().navigate(R.id.)
        }
    }

    private fun shareNote(note: Note) {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NoteListViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    companion object {
        const val ACTION_DELETE = "delete"
        const val ACTION_SHARE = "share"
        const val ACTION_FAB_SHOW = "fabShow"
        const val ACTION_FAB_HIDE = "fabHide"
    }

}