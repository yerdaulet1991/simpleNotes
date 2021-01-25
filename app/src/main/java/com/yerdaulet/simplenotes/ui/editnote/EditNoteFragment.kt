package com.yerdaulet.simplenotes.ui.editnote

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yerdaulet.simplenotes.R
import com.yerdaulet.simplenotes.databinding.EditNoteFragmentBinding
import com.yerdaulet.simplenotes.util.ReminderCompletion
import com.yerdaulet.simplenotes.util.ReminderState
import com.yerdaulet.simplenotes.util.currentDate
import com.yerdaulet.simplenotes.util.hideKeyboard
import com.yerdaulet.simplenotes.viewmodels.EditNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditNoteFragment : Fragment(), BottomSheetClickListener,
    DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    private var _binding: EditNoteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pickedDateTime: Calendar
    private lateinit var currentDateTime: Calendar
    private lateinit var uiScope: CoroutineScope
    private val args: EditNoteFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: EditNoteViewModel.AssistedFactory

    private val viewModel: EditNoteViewModel by viewModels {
        EditNoteViewModel.provideFactory(
            viewModelFactory,
            args.noteId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = EditNoteFragmentBinding.inflate(inflater,container, false)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            editviewmodel = viewModel
            reminderState = viewModel.reminderState
            reminderCompletion = viewModel.reminderCompletion
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        observeViewModel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiScope = CoroutineScope(Dispatchers.Default)

        binding.reminderCard.setOnClickListener {
            if (viewModel.reminderState.get() == ReminderState.NO_REMINDER) {
                pickDate()
            } else {
                childFragmentManager.let {
                    val bundle = Bundle().also {
                        it.putLong("reminder", viewModel.noteBeingModified.value?.reminder!!)
                    }
                    OptionsListDialogFragment.newInstance(bundle).apply {
                        show(it, tag)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                hideKeyboard(view, requireContext())
                saveNote()
                true
            }
            R.id.action_share -> {
                shareNote()
                true
            }
            R.id.action_color -> {
                viewModel.pickColor(requireActivity())
                true
            }
            R.id.action_delete -> {
                openDeleteDialog()
                true
            }
            android.R.id.home -> {
                onBackClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete).isVisible = viewModel.mIsEdit.value!!
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(view, requireContext())
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is OptionsListDialogFragment -> childFragment.mListener = this
        }
    }


    private fun openDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_note))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton("Delete") {_,_ ->
                deleteNote()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteNote() {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.deleteNote()
            }
        }
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard))
            .setMessage(getString(R.string.discard_changes))
            .setPositiveButton("Continue editing", null)
            .setNegativeButton(getString(R.string.discard_note)){ _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun shareNote() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "${viewModel.noteBeingModified.value?.title}\n\n${viewModel
                    .noteBeingModified.value?.text}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun saveNote() {
        when {
            viewModel.noteBeingModified.value!!.title.isBlank() or viewModel.noteBeingModified.value!!.text.isBlank() -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_be_blank),
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.isChanged.value!! -> {
                viewModel.saveNote()
                viewModel.scheduleReminder()
                Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            else -> {
                findNavController().popBackStack()

            }
        }
    }

    private fun pickDate() {
        currentDateTime = currentDate()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog =
            DatePickerDialog(requireContext(), this, startYear, startMonth, startDay)
        datePickerDialog.show()
    }

    private fun observeViewModel() {
        viewModel.apply {
            noteBeingModified.observe(viewLifecycleOwner, { note ->
                note?.let {
                    if (it.reminder != null) {
                        viewModel.reminderState.set(ReminderState.HAS_REMINDER)
                        if (currentDate().timeInMillis > note.reminder!!) {
                            viewModel.reminderCompletion.set(ReminderCompletion.COMPLETED)
                        } else {
                            viewModel.reminderCompletion.set(ReminderCompletion.ONGOING)
                        }
                    } else {
                        viewModel.reminderState.set(ReminderState.NO_REMINDER)
                    }
                    activity?.invalidateOptionsMenu()
                }
            })
        }
    }

    private fun onBackClicked() {
        if (viewModel.isChanged.value!!) {
            hideKeyboard(view, requireContext())
            openDeleteDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onItemClick(item: String) {
        when (item) {
            getString(R.string.modify) -> {
                pickDate()
            }
            getString(R.string.delete) -> {
                viewModel.cancelReminder()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        pickedDateTime = currentDate()
        pickedDateTime.set(year, month, dayOfMonth)
        currentDateTime = currentDate()
        val hourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = currentDateTime.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(requireContext(), this, hourOfDay, minuteOfDay, false)
        timePickerDialog.show()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        pickedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
        pickedDateTime.set(Calendar.MINUTE, minute)
        if (pickedDateTime.timeInMillis <= currentDate().timeInMillis) {
            pickedDateTime.run {
                set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
            }
        }
        viewModel.setDateTime(pickedDateTime.timeInMillis)
    }

}