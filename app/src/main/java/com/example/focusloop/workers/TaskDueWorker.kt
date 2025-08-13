package com.example.focusloop.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.focusloop.R
import com.example.focusloop.activities.MyTasksActivity
import com.example.focusloop.data.PrefsRepo
import java.text.SimpleDateFormat
import java.util.*

class TaskDueWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    companion object {
        const val IS_TEST_MODE = true // Cambia a false para modo normal
    }

    override fun doWork(): Result {
        if (IS_TEST_MODE) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if (hour == 13 || hour == 23) {
                sendTestNotification(hour)
            }
            return Result.success()
        }
        // Leer hora y minuto configurados por el usuario
        val prefs = applicationContext.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val userHour = prefs.getInt("notification_hour", 8)
        val userMinute = prefs.getInt("notification_minute", 0)
        val calendar = Calendar.getInstance()
        val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMinute = calendar.get(Calendar.MINUTE)
        // Tolerancia de 7 minutos para asegurar coincidencia
        val isTimeToNotify = nowHour == userHour && Math.abs(nowMinute - userMinute) <= 7
        if (!isTimeToNotify) return Result.success()
        // Verificar si ya se envió notificación hoy
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastNotifiedDay = prefs.getString("last_notified_day", null)
        if (lastNotifiedDay == today) return Result.success()
        // Guardar que ya se envió notificación hoy SOLO si se envía notificación
        val prefsRepo = PrefsRepo(applicationContext)
        val taskList = prefsRepo.getTasks()
        val todayDisplay = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
        // Filtrar tareas que cumplen condiciones
        val dueTasks = taskList.filter { task ->
            !task.endDate.isNullOrBlank() &&
            task.endDate == todayDisplay &&
            task.status != com.example.focusloop.models.TaskStatus.COMPLETED
        }
        if (dueTasks.isEmpty()) return Result.success() // No hay tareas válidas, no notificar ni guardar fecha
        // Guardar que ya se envió notificación hoy
        prefs.edit().putString("last_notified_day", today).apply()
        if (dueTasks.size == 1) {
            sendSingleTaskNotification(dueTasks.first())
        } else {
            sendMultipleTasksNotification(dueTasks.size)
        }
        return Result.success()
    }

    private fun sendTaskDueTodayNotification(task: com.example.focusloop.models.Task) {
        val channelId = "task_due_today_channel"
        val notificationId = (task.name + task.endDate).hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Due Today"
            val descriptionText = "Notifies when a task is due today."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val progress = when (task.status) {
            com.example.focusloop.models.TaskStatus.NOT_STARTED -> "Not Started"
            com.example.focusloop.models.TaskStatus.IN_PROGRESS -> "In Progress"
            com.example.focusloop.models.TaskStatus.COMPLETED -> "Completed"
        }
        val intent = Intent(applicationContext, MyTasksActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Check notification permission before sending
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, do not send notification
                return
            }
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_3)
            .setContentTitle("Task due today: ${task.name}")
            .setContentText("Progress: $progress | Category: ${task.category.name}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }

    private fun sendSingleTaskNotification(task: com.example.focusloop.models.Task) {
        val channelId = "task_due_today_channel"
        val notificationId = (task.name + task.endDate).hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Due Today"
            val descriptionText = "Notifies when a task is due today."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val progress = when (task.status) {
            com.example.focusloop.models.TaskStatus.NOT_STARTED -> "Not Started"
            com.example.focusloop.models.TaskStatus.IN_PROGRESS -> "In Progress"
            com.example.focusloop.models.TaskStatus.COMPLETED -> "Completed"
        }
        val intent = Intent(applicationContext, MyTasksActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_3)
            .setContentTitle("You have one pending task")
            .setContentText("Task: ${task.name} | Progress: $progress | Category: ${task.category.name}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }

    private fun sendMultipleTasksNotification(taskCount: Int) {
        val channelId = "task_due_today_channel"
        val notificationId = ("multiple_tasks_due_today").hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Due Today"
            val descriptionText = "Notifies when tasks are due today."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, MyTasksActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_3)
            .setContentTitle("You have several pending tasks")
            .setContentText("You have $taskCount pending tasks today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }

    private fun sendTestNotification(hour: Int) {
        val channelId = "test_notification_channel"
        val notificationId = ("test_notification_$hour").hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Notification"
            val descriptionText = "Test notification at $hour:00."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, MyTasksActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.icon_3)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification for $hour:00.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }
}
