package com.taskmate.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.taskmate.app.data.local.Task
import com.taskmate.app.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        // Започни синхронизација со облакот за тековниот корисник.
        repository.startSync(userId)
    }

    val tasks = repository.observeTasks(userId).asLiveData()
    val openCount = repository.observeOpenCount(userId).asLiveData()

    fun saveTask(title: String, description: String, priority: Int, dueDate: Long?, existing: Task?) {
        viewModelScope.launch {
            val task = existing?.copy(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            ) ?: Task(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                ownerId = userId
            )
            repository.saveTask(task)
        }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch { repository.toggleCompleted(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    suspend fun getTask(id: String): Task? = repository.getById(id)

    override fun onCleared() {
        super.onCleared()
        repository.stopSync()
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
