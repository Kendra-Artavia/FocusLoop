package com.example.focusloop.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.work.*
import com.example.focusloop.R
import com.example.focusloop.workers.TaskDueWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)

        // Botón Nueva Tarea
        val newTaskButton: Button = findViewById(R.id.newTaskButton)
        newTaskButton.setOnClickListener {
            // Navegar a la pantalla de crear tarea
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        // Botón Sesión Pomodoro
        val pomodoroButton: Button = findViewById(R.id.pomodoroButton)
        pomodoroButton.setOnClickListener {
            // Navegar a la pantalla de sesión Pomodoro
            val intent = Intent(this, PomodoroActivity::class.java)
            startActivity(intent)
        }

        // Botón Mis Tareas
        val myTasksButton: Button = findViewById(R.id.myTasksButton)
        myTasksButton.setOnClickListener {
            // Navegar a la pantalla de tareas pendientes
            val intent = Intent(this, MyTasksActivity::class.java)
            startActivity(intent)
        }

        // Configuración del botón de notificaciones
        val notificationConfigButton: ImageButton = findViewById(R.id.notificationConfigButton)
        notificationConfigButton.setOnClickListener {
            showNotificationConfigDialog()
        }

        // Reprogramar Worker de notificación diaria al abrir la pantalla
        val prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("daily_task_notifications", true)
        val hour = prefs.getInt("notification_hour", 8)
        val minute = prefs.getInt("notification_minute", 0)
        scheduleOrCancelTaskDueWorker(enabled, hour, minute)
    }

    private fun showNotificationConfigDialog() {
        val prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("daily_task_notifications", true)
        val hour = prefs.getInt("notification_hour", 8)
        val minute = prefs.getInt("notification_minute", 0)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_settings, null)
        val switch = dialogView.findViewById<Switch>(R.id.switchEnableNotifications)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePickerNotification)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        switch.isChecked = enabled
        timePicker.setIs24HourView(true)
        timePicker.hour = hour
        timePicker.minute = minute
        timePicker.isEnabled = enabled

        switch.setOnCheckedChangeListener { _, isChecked ->
            timePicker.isEnabled = isChecked
        }

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        btnOk.setOnClickListener {
            prefs.edit()
                .putBoolean("daily_task_notifications", switch.isChecked)
                .putInt("notification_hour", timePicker.hour)
                .putInt("notification_minute", timePicker.minute)
                .apply()
            val msg = if (switch.isChecked) getString(R.string.notifications_enabled) else getString(R.string.notifications_disabled)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
            scheduleOrCancelTaskDueWorker(switch.isChecked, timePicker.hour, timePicker.minute)
        }
        alertDialog.show()
    }

    private fun scheduleOrCancelTaskDueWorker(enabled: Boolean, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelUniqueWork("TaskDueWorker")
        if (!enabled) return
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis
        val workRequest = PeriodicWorkRequestBuilder<TaskDueWorker>(5, TimeUnit.MINUTES)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "TaskDueWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}
