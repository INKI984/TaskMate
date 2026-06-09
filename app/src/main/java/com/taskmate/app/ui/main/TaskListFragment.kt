package com.taskmate.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
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
import com.taskmate.app.data.local.Task
import com.taskmate.app.databinding.FragmentTaskListBinding
import com.taskmate.app.util.Constants

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory((requireActivity().application as TaskMateApp).repository)
    }

    private lateinit var adapter: TaskAdapter

    private enum class SortMode { CREATED, PRIORITY, DUE_DATE }

    private var currentSort = SortMode.CREATED
    private var latestTasks: List<Task> = emptyList()
    private var currentQuery: String = ""

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

        setupSortMenu()

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            latestTasks = tasks
            resubmit()
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_taskList_to_addEdit)
        }

        setupSwipeToDelete()
    }

    private fun setupSortMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.task_list_menu, menu)

                val searchView = menu.findItem(R.id.action_search).actionView as SearchView
                searchView.queryHint = getString(R.string.search_hint)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        currentQuery = newText.orEmpty()
                        resubmit()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
                R.id.sort_priority -> { currentSort = SortMode.PRIORITY; resubmit(); true }
                R.id.sort_due -> { currentSort = SortMode.DUE_DATE; resubmit(); true }
                R.id.sort_created -> { currentSort = SortMode.CREATED; resubmit(); true }
                else -> false
            }
        }, viewLifecycleOwner)
    }

    private fun filtered(tasks: List<Task>): List<Task> =
        if (currentQuery.isBlank()) tasks
        else tasks.filter {
            it.title.contains(currentQuery, ignoreCase = true) ||
                    it.description.contains(currentQuery, ignoreCase = true)
        }

    private fun sortedList(tasks: List<Task>): List<Task> = when (currentSort) {
        SortMode.PRIORITY -> tasks.sortedByDescending { it.priority }
        SortMode.DUE_DATE -> tasks.sortedWith(compareBy(nullsLast<Long>()) { it.dueDate })
        SortMode.CREATED -> tasks.sortedByDescending { it.createdAt }
    }

    private fun resubmit() {
        val display = sortedList(filtered(latestTasks))
        adapter.submitList(display)
        binding.emptyView.visibility = if (display.isEmpty()) View.VISIBLE else View.GONE
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