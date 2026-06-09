package com.taskmate.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.taskmate.app.R
import com.taskmate.app.TaskMateApp
import com.taskmate.app.databinding.FragmentTaskListBinding
import com.taskmate.app.util.Constants

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory((requireActivity().application as TaskMateApp).repository)
    }

    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TaskAdapter(
            onClick = { task ->
                val bundle = Bundle().apply { putString(Constants.EXTRA_TASK_ID, task.id) }
                findNavController().navigate(R.id.action_taskList_to_addEdit, bundle)
            },
            onToggle = { task ->
                viewModel.toggleCompleted(task)
                if (!task.isCompleted) {
                    Firebase.analytics.logEvent(Constants.EVENT_TASK_COMPLETED) {
                        param("task_id", task.id)
                    }
                }
            }
        )
        binding.recyclerView.adapter = adapter

        // Набљудувај ги задачите од Room (преку ViewModel)
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        // FAB → нова задача
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_taskList_to_addEdit)
        }

        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.currentList[vh.adapterPosition]
                viewModel.deleteTask(task)
                Firebase.analytics.logEvent(Constants.EVENT_TASK_DELETED) {
                    param("task_id", task.id)
                }
                Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { viewModel.saveTask(
                        task.title, task.description, task.priority, task.dueDate, null
                    ) }
                    .show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
