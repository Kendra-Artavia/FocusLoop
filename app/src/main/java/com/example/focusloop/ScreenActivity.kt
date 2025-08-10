package com.example.focusloop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.focusloop.BaseActivity

class ScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)

        // Botón Nueva Tarea
        val newTaskButton: Button = findViewById(R.id.newTaskButton)
        newTaskButton.setOnClickListener {
            // Navegar a la pantalla de crear tarea
           // val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }

        // Botón Sesión Pomodoro
        val pomodoroButton: Button = findViewById(R.id.pomodoroButton)
        pomodoroButton.setOnClickListener {
            // Navegar a la pantalla de sesión Pomodoro
          //  val intent = Intent(this, PomodoroActivity::class.java)
            startActivity(intent)
        }

        // Botón Mis Tareas
        val myTasksButton: Button = findViewById(R.id.myTasksButton)
        myTasksButton.setOnClickListener {
            // Navegar a la pantalla de tareas pendientes
          //  val intent = Intent(this, MyTasksActivity::class.java)
            startActivity(intent)
        }
    }
}
