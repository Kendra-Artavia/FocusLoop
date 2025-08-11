package com.example.focusloop.models


data class Task(
    val name: String,
    val description: String,
    val category: Category,
    val status: TaskStatus,
    val startDate: String?,
    val endDate: String?
)
