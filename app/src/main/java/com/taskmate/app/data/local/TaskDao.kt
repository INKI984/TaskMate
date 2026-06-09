package com.taskmate.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /** Сите задачи на даден корисник, сортирани (недовршени прво, па по приоритет). */
    @Query("SELECT * FROM tasks WHERE ownerId = :ownerId ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getTasksForUser(ownerId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Task?

    @Query("SELECT COUNT(*) FROM tasks WHERE ownerId = :ownerId AND isCompleted = 0")
    fun countOpenTasks(ownerId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Бришење на сите локални задачи (на пр. при одјава). */
    @Query("DELETE FROM tasks WHERE ownerId = :ownerId")
    suspend fun clearForUser(ownerId: String)
}
