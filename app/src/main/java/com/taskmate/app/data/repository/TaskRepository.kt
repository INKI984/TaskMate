package com.taskmate.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.taskmate.app.data.local.Task
import com.taskmate.app.data.local.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Единствен извор на вистина за задачите.
 *
 * - [Room] (преку [dao]) е ЛОКАЛНАТА база и изворот од кој UI чита.
 * - [Firestore] е облак-слојот: пишувањата одат таму, а snapshot listener
 *   ги враќа промените назад во Room (вклучително и промени од друг уред).
 */
class TaskRepository(
    private val dao: TaskDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var listener: ListenerRegistration? = null

    /** Реактивен поток на задачи за UI — секогаш чита од Room. */
    fun observeTasks(ownerId: String): Flow<List<Task>> = dao.getTasksForUser(ownerId)

    fun observeOpenCount(ownerId: String): Flow<Int> = dao.countOpenTasks(ownerId)

    private fun userCollection(ownerId: String) =
        firestore.collection("users").document(ownerId).collection("tasks")

    /**
     * Започнува слушање на Firestore промени за дадениот корисник и
     * ги пресликува во локалната Room база.
     */
    fun startSync(ownerId: String) {
        if (ownerId.isBlank()) return
        listener?.remove()
        listener = userCollection(ownerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Грешка при слушање на Firestore", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            scope.launch {
                for (change in snapshot.documentChanges) {
                    val data = change.document.data
                    when (change.type.name) {
                        "REMOVED" -> dao.deleteById(change.document.id)
                        else -> dao.upsert(Task.fromMap(data))
                    }
                }
            }
        }
    }

    fun stopSync() {
        listener?.remove()
        listener = null
    }

    /** Додавање/изменување: прво локално (offline), па во облак. */
    suspend fun saveTask(task: Task) {
        dao.upsert(task.copy(synced = false))
        try {
            userCollection(task.ownerId).document(task.id).set(task.toMap()).await()
            dao.upsert(task.copy(synced = true))
        } catch (e: Exception) {
            Log.w(TAG, "Записот не е синхронизиран, ќе се обиде подоцна", e)
        }
    }

    suspend fun toggleCompleted(task: Task) {
        saveTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(task: Task) {
        dao.delete(task)
        try {
            userCollection(task.ownerId).document(task.id).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Бришењето не е синхронизирано", e)
        }
    }

    suspend fun getById(id: String): Task? = dao.getById(id)

    /** Чисти ги локалните податоци (на пр. при одјава). */
    suspend fun clearLocal(ownerId: String) {
        stopSync()
        dao.clearForUser(ownerId)
    }

    companion object {
        private const val TAG = "TaskRepository"
    }
}
