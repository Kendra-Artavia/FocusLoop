package com.example.focusloop.activities

import android.os.Bundle
import android.widget.*
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import com.example.focusloop.R

class PomodoroActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var timerRunning = false
    private var workMinutes = 25
    private var breakMinutes = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        // Configuración del Spinner para seleccionar música
        val musicOptions = arrayOf("Ambient Music", "Relaxing Music", "Rain Sounds")
        val musicSpinner = findViewById<Spinner>(R.id.musicSpinner)
        val musicAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, musicOptions)
        musicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        musicSpinner.adapter = musicAdapter

        // Botón para iniciar la sesión Pomodoro
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            if (!timerRunning) {
                startPomodoroSession()
                timerRunning = true
            } else {
                stopPomodoroSession()
                timerRunning = false
            }
        }

        // Botón de volver
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Configuración de botones de sesión
        findViewById<Button>(R.id.baseButton).setOnClickListener {
            workMinutes = 25
            breakMinutes = 5
            updateTimer()
        }

        findViewById<Button>(R.id.easyButton).setOnClickListener {
            workMinutes = 10
            breakMinutes = 5
            updateTimer()
        }

        findViewById<Button>(R.id.customButton).setOnClickListener {
            // Lógica para establecer una sesión personalizada
            showCustomTimeDialog()
        }
    }

    private fun updateTimer() {
        val pomodoroTime = findViewById<TextView>(R.id.pomodoroTime)
        pomodoroTime.text = "$workMinutes:00"
    }

    private fun startPomodoroSession() {
        // Lógica para comenzar el temporizador y la música
        val selectedMusic = findViewById<Spinner>(R.id.musicSpinner).selectedItem.toString()
        playMusic(selectedMusic)
    }

    private fun stopPomodoroSession() {
        // Detener música y detener el temporizador
        mediaPlayer.stop()
        val pomodoroTime = findViewById<TextView>(R.id.pomodoroTime)
        pomodoroTime.text = "Pomodoro Stopped"
    }

    private fun playMusic(musicType: String) {
        // Inicializar y reproducir música según la opción seleccionada
        when (musicType) {
            "Ambient Music" -> mediaPlayer = MediaPlayer.create(this, R.raw.ambient_music)
            "Relaxing Music" -> mediaPlayer = MediaPlayer.create(this, R.raw.relaxing_music)
            "Rain Sounds" -> mediaPlayer = MediaPlayer.create(this, R.raw.rain_sounds)
        }
        mediaPlayer.start()
    }

    private fun showCustomTimeDialog() {
        val input = EditText(this)
        input.hint = "Enter minutes"
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set Custom Time")
            .setView(input)
            .setPositiveButton("Set") { d, _ ->
                val time = input.text.toString().toIntOrNull() ?: 25
                workMinutes = time
                breakMinutes = 5
                updateTimer()
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}
