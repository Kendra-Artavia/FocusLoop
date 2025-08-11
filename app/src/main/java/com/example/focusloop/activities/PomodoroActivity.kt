package com.example.focusloop.activities

import android.os.Bundle
import android.widget.*
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import com.example.focusloop.R
import android.os.CountDownTimer
class PomodoroActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var timerRunning = false
    private var workMinutes = 25
    private var breakMinutes = 5
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0L
    private var isPaused = false
    private var pauseTimeLeft: Long = 0L

    // NUEVO: Guardar el tipo de música seleccionada
    private var selectedMusicType: String = "Ambient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        // --- MUSIC BUTTONS ---
        val musicAmbientButton = findViewById<Button>(R.id.musicAmbientButton)
        val musicRelaxingButton = findViewById<Button>(R.id.musicRelaxingButton)
        val musicRainButton = findViewById<Button>(R.id.musicRainButton)

        // Inicializa el estado visual de los botones de música
        highlightSelectedMusicButton(musicAmbientButton, musicRelaxingButton, musicRainButton)

        musicAmbientButton.setOnClickListener {
            if (selectedMusicType != "Ambient") {
                selectedMusicType = "Ambient"
                playMusic(selectedMusicType)
                highlightSelectedMusicButton(musicAmbientButton, musicRelaxingButton, musicRainButton)
            }
        }
        musicRelaxingButton.setOnClickListener {
            if (selectedMusicType != "Relaxing") {
                selectedMusicType = "Relaxing"
                playMusic(selectedMusicType)
                highlightSelectedMusicButton(musicRelaxingButton, musicAmbientButton, musicRainButton)
            }
        }
        musicRainButton.setOnClickListener {
            if (selectedMusicType != "Rain") {
                selectedMusicType = "Rain"
                playMusic(selectedMusicType)
                highlightSelectedMusicButton(musicRainButton, musicAmbientButton, musicRelaxingButton)
            }
        }

        // Botón para iniciar la sesión Pomodoro
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        startButton.setOnClickListener {
            if (!timerRunning) {
                // Siempre usa el valor actual de workMinutes antes de iniciar
                timeLeftInMillis = workMinutes * 60 * 1000L
                startPomodoroSession()
                timerRunning = true
                isPaused = false
            }
        }

        stopButton.setOnClickListener {
            if (timerRunning || isPaused) {
                stopPomodoroSession()
                timerRunning = false
                isPaused = false
            }
        }

        // Botón de volver
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Referencias a los botones de sesión
        val baseButton = findViewById<Button>(R.id.baseButton)
        val easyButton = findViewById<Button>(R.id.easyButton)
        val customButton = findViewById<Button>(R.id.customButton)

        // Configuración de botones de sesión
        baseButton.setOnClickListener {
            stopIfRunningAndReset()
            workMinutes = 25
            breakMinutes = 5
            updateTimer()
            highlightSelectedSessionButton(baseButton, easyButton, customButton)
            Toast.makeText(this, "Base mode selected", Toast.LENGTH_SHORT).show()
        }

        easyButton.setOnClickListener {
            stopIfRunningAndReset()
            workMinutes = 10
            breakMinutes = 5
            updateTimer()
            highlightSelectedSessionButton(easyButton, baseButton, customButton)
            Toast.makeText(this, "Easy mode selected", Toast.LENGTH_SHORT).show()
        }

        customButton.setOnClickListener {
            stopIfRunningAndReset()
            showCustomTimeDialog {
                highlightSelectedSessionButton(customButton, baseButton, easyButton)
                Toast.makeText(this, "Custom mode selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Inicializa el temporizador con el valor por defecto y resalta el botón base
        updateTimer()
        highlightSelectedSessionButton(baseButton, easyButton, customButton)
    }

    // NUEVO: Función para resaltar el botón de sesión seleccionado
    private fun highlightSelectedSessionButton(selected: Button, other1: Button, other2: Button) {
        selected.setBackgroundResource(R.drawable.rounded_button_pink)
        other1.setBackgroundResource(R.drawable.rounded_button_blue)
        other2.setBackgroundResource(R.drawable.rounded_button_blue)
    }

    // Añade esta función para resaltar el botón de música seleccionado
    private fun highlightSelectedMusicButton(selected: Button, other1: Button, other2: Button) {
        selected.setBackgroundResource(R.drawable.rounded_button_pink)
        other1.setBackgroundResource(R.drawable.rounded_button_blue)
        other2.setBackgroundResource(R.drawable.rounded_button_blue)
    }

    private fun updateTimer() {
        val pomodoroTime = findViewById<TextView>(R.id.pomodoroTime)
        pomodoroTime.text = String.format("%02d:00", workMinutes)
        timeLeftInMillis = workMinutes * 60 * 1000L
    }

    private fun startPomodoroSession() {
        playMusic(selectedMusicType)
        // Usa el valor actual de timeLeftInMillis
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val pomodoroTime = findViewById<TextView>(R.id.pomodoroTime)
                pomodoroTime.text = String.format("%02d:%02d", minutes, seconds)
            }
            override fun onFinish() {
                timerRunning = false
                isPaused = false
                pauseTimeLeft = 0L
                val pomodoroTime = findViewById<TextView>(R.id.pomodoroTime)
                pomodoroTime.text = "00:00"
                if (::mediaPlayer.isInitialized) mediaPlayer.stop()
            }
        }.start()
        timerRunning = true
        isPaused = false
    }

    private fun stopPomodoroSession() {
        countDownTimer?.cancel()
        if (::mediaPlayer.isInitialized) mediaPlayer.stop()
        // No reset, just stop
        pauseTimeLeft = timeLeftInMillis
        isPaused = false
        timerRunning = false
    }

    private fun playMusic(musicType: String) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        when (musicType) {
            "Ambient" -> mediaPlayer = MediaPlayer.create(this, R.raw.ambient_music)
            "Relaxing" -> mediaPlayer = MediaPlayer.create(this, R.raw.relaxing_music)
            "Rain" -> mediaPlayer = MediaPlayer.create(this, R.raw.rain_sounds)
        }
        // Set volume to system default (no custom SeekBar)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun stopIfRunningAndReset() {
        if (timerRunning || isPaused) {
            stopPomodoroSession()
        }
        // Reinicia el tiempo restante para reflejar el nuevo valor seleccionado
        timeLeftInMillis = workMinutes * 60 * 1000L
    }

    // Modifica showCustomTimeDialog para aceptar un callback opcional
    private fun showCustomTimeDialog(onSelected: (() -> Unit)? = null) {
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
                timeLeftInMillis = workMinutes * 60 * 1000L
                d.dismiss()
                onSelected?.invoke()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        // Detener la música si la actividad deja de estar en primer plano
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos del MediaPlayer al destruir la actividad
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
