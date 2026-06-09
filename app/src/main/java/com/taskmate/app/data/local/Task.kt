package com.taskmate.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Task — ентитет што се чува ЛОКАЛНО преку Room и истовремено
 * се синхронизира во облак преку Firestore.
 *
 * Истиот [id] (UUID String) се користи и како Room primary key и
 * како Firestore document id, за да биде синхронизацијата едноставна.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    /** 0 = ниско, 1 = средно, 2 = високо */
    val priority: Int = 1,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** UID на корисникот сопственик (од Firebase Auth) */
    val ownerId: String = "",
    /** Дали записот е успешно синхронизиран во Firestore */
    val synced: Boolean = false
) {
    /** Конвертирање во Map за Firestore запис. */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "isCompleted" to isCompleted,
        "priority" to priority,
        "dueDate" to dueDate,
        "createdAt" to createdAt,
        "ownerId" to ownerId
    )

    companion object {
        /** Креирање на Task од Firestore документ (Map). */
        fun fromMap(map: Map<String, Any?>): Task = Task(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            isCompleted = map["isCompleted"] as? Boolean ?: false,
            priority = (map["priority"] as? Long)?.toInt() ?: 1,
            dueDate = map["dueDate"] as? Long,
            createdAt = (map["createdAt"] as? Long) ?: System.currentTimeMillis(),
            ownerId = map["ownerId"] as? String ?: "",
            synced = true
        )
    }
}
