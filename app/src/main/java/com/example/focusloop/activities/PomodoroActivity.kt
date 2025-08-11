package com.example.focusloop.activities

import android.os.Bundle
import android.widget.*
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import com.example.focusloop.R
import android.os.CountDownTimer
import android.media.AudioManager
import android.view.View
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.example.focusloop.activities.PomodoroActivity

class PomodoroActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var timerRunning = false
    private var workMinutes = 25
    private var breakMinutes = 5
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0L
    private var isPaused = false
    private var pauseTimeLeft: Long = 0L
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var pauseButton: Button

    // NUEVO: Guardar el tipo de música seleccionada
    private var selectedMusicType: String = "Ambient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        // --- VOLUMEN ---
        volumeSeekBar = SeekBar(this)
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = currentVolume

        // Add the SeekBar to the layout (below header)
        val headerLayout = findViewById<LinearLayout>(R.id.sessionButtonsLayout).parent as LinearLayout
        headerLayout.addView(volumeSeekBar, 1) // Insert after header

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                if (::mediaPlayer.isInitialized) {
                    val vol = progress / maxVolume.toFloat()
                    mediaPlayer.setVolume(vol, vol)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // --- PAUSA ---
        pauseButton = Button(this)
        pauseButton.id = View.generateViewId()
        pauseButton.text = getString(R.string.pause_button)
        pauseButton.setBackgroundResource(R.drawable.rounded_button_blue)
        pauseButton.setTextColor(resources.getColor(android.R.color.white))
        val timerControlsLayout = findViewById<LinearLayout>(R.id.timerControlsLayout)
        timerControlsLayout.addView(pauseButton, 1) // Insert between Start and Stop

        pauseButton.setOnClickListener {
            if (timerRunning && !isPaused) {
                pausePomodoroSession()
            } else if (isPaused) {
                resumePomodoroSession()
            }
        }

        // --- MUSIC BUTTONS ---
        val musicAmbientButton = findViewById<Button>(R.id.musicAmbientButton)
        val musicRelaxingButton = findViewById<Button>(R.id.musicRelaxingButton)
        val musicRainButton = findViewById<Button>(R.id.musicRainButton)

        musicAmbientButton.setOnClickListener {
            selectedMusicType = "Ambient"
            highlightSelectedMusicButton(musicAmbientButton, musicRelaxingButton, musicRainButton)
            playMusic(selectedMusicType)
        }
        musicRelaxingButton.setOnClickListener {
            selectedMusicType = "Relaxing"
            highlightSelectedMusicButton(musicRelaxingButton, musicAmbientButton, musicRainButton)
            playMusic(selectedMusicType)
        }
        musicRainButton.setOnClickListener {
            selectedMusicType = "Rain"
            highlightSelectedMusicButton(musicRainButton, musicAmbientButton, musicRelaxingButton)
            playMusic(selectedMusicType)
        }
        highlightSelectedMusicButton(musicAmbientButton, musicRelaxingButton, musicRainButton)

        // Botón para iniciar la sesión Pomodoro
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        startButton.setOnClickListener {
            if (!timerRunning) {
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

    // NUEVO: Función para resaltar el botón seleccionado
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
        val duration = if (pauseTimeLeft > 0L) pauseTimeLeft else workMinutes * 60 * 1000L
        countDownTimer = object : CountDownTimer(duration, 1000) {
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

    private fun pausePomodoroSession() {
        countDownTimer?.cancel()
        pauseTimeLeft = timeLeftInMillis
        isPaused = true
        pauseButton.text = getString(R.string.resume_button)
    }

    private fun resumePomodoroSession() {
        startPomodoroSession()
        pauseButton.text = getString(R.string.pause_button)
        isPaused = false
    }

    private fun stopPomodoroSession() {
        countDownTimer?.cancel()
        if (::mediaPlayer.isInitialized) mediaPlayer.stop()
        // No reset, just stop
        pauseTimeLeft = timeLeftInMillis
        isPaused = false
        timerRunning = false
        pauseButton.text = getString(R.string.pause_button)
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
        // Set volume to current SeekBar value
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = volumeSeekBar.progress
        val vol = currentVolume / maxVolume.toFloat()
        mediaPlayer.setVolume(vol, vol)
        mediaPlayer.isLooping = true
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
