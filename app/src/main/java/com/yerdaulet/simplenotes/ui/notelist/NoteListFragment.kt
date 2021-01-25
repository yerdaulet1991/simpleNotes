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
import com.yerdaulet.simplenotes.R
import com.yerdaulet.simplenotes.adapters.NotesAdapter
import com.yerdaulet.simplenotes.databinding.NoteListFragmentBinding
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
        return inflater.inflate(R.layout.note_list_fragment, container, false)
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

}