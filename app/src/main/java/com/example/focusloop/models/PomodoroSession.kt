// models/PomodoroSession.kt
package com.example.focusloop.models

data class PomodoroSession(
    val workTime: Int,
    val breakTime: Int,
    val isCustom: Boolean = false
)
