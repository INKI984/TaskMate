package com.taskmate.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmate.app.R
import com.taskmate.app.data.local.Task
import com.taskmate.app.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onClick: (Task) -> Unit,
    private val onToggle: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDescription.text = task.description
            binding.tvDescription.visibility =
                if (task.description.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = task.isCompleted

            // Боја на индикаторот според приоритет
            val ctx = binding.root.context
            val color = when (task.priority) {
                2 -> ctx.getColor(R.color.priority_high)
                1 -> ctx.getColor(R.color.priority_medium)
                else -> ctx.getColor(R.color.priority_low)
            }
            binding.priorityIndicator.setBackgroundColor(color)

            // Рок (ако постои)
            if (task.dueDate != null) {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.tvDueDate.text = sdf.format(Date(task.dueDate))
                binding.tvDueDate.visibility = android.view.View.VISIBLE
            } else {
                binding.tvDueDate.visibility = android.view.View.GONE
            }

            // Прецртан текст за завршени задачи
            binding.tvTitle.paintFlags = if (task.isCompleted) {
                binding.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.checkBox.setOnCheckedChangeListener { _, _ -> onToggle(task) }
            binding.root.setOnClickListener { onClick(task) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
        }
    }
}
