package com.example.focusloop.models


data class Task(
    val name: String,
    val description: String,
    val category: Category,
    var status: TaskStatus, // <-- Cambiado a var
    val startDate: String?,
    val endDate: String?
)
