package com.taskmate.app.ui.detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.taskmate.app.R
import com.taskmate.app.TaskMateApp
import com.taskmate.app.data.local.Task
import com.taskmate.app.databinding.FragmentAddEditBinding
import com.taskmate.app.ui.main.TaskViewModel
import com.taskmate.app.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory((requireActivity().application as TaskMateApp).repository)
    }

    private var existingTask: Task? = null
    private var selectedDueDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getString(Constants.EXTRA_TASK_ID)
        if (taskId != null) {
            lifecycleScope.launch {
                existingTask = viewModel.getTask(taskId)
                existingTask?.let { populate(it) }
            }
        }

        binding.etDueDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun populate(task: Task) {
        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.description)
        binding.chipGroupPriority.check(
            when (task.priority) {
                2 -> R.id.chipHigh
                1 -> R.id.chipMedium
                else -> R.id.chipLow
            }
        )
        selectedDueDate = task.dueDate
        task.dueDate?.let {
            binding.etDueDate.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)))
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDueDate?.let { cal.timeInMillis = it }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day, 0, 0, 0)
                selectedDueDate = c.timeInMillis
                binding.etDueDate.setText(
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(c.time)
                )
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_title_required)
            return
        }
        val priority = when (binding.chipGroupPriority.checkedChipId) {
            R.id.chipHigh -> 2
            R.id.chipLow -> 0
            else -> 1
        }
        viewModel.saveTask(
            title = title,
            description = binding.etDescription.text.toString().trim(),
            priority = priority,
            dueDate = selectedDueDate,
            existing = existingTask
        )
        if (existingTask == null) {
            Firebase.analytics.logEvent(Constants.EVENT_TASK_CREATED) {
                param("priority", priority.toLong())
            }
        }
        Toast.makeText(requireContext(), R.string.task_saved, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
