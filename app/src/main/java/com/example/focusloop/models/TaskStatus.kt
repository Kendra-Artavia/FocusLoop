// models/TaskStatus.kt
package com.example.focusloop.models

enum class TaskStatus(val displayName: String) {
    NOT_STARTED("not started"),
    IN_PROGRESS("in progress"),
    COMPLETED("completed");

    companion object {
        // Función para convertir un String en un TaskStatus
        fun fromString(value: String): TaskStatus {
            return values().firstOrNull { it.displayName == value }
                ?: NOT_STARTED // Valor por defecto si no se encuentra el estado
        }
    }
}
