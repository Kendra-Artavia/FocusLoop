package com.example.focusloop.activities

import android.os.Bundle
import android.widget.*
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import com.example.focusloop.R
import android.os.CountDownTimer
import android.app.Dialog
import android.os.Vibrator
import android.os.VibrationEffect

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

    // NUEVO: Estado para saber si la música está pausada
    private var isMusicPaused = false

    // NUEVO: Variable para el sonido de alarma
    private var alarmPlayer: MediaPlayer? = null

    private var vibrator: Vibrator? = null
    private var isVibrating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)
        vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator

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
            stopVibration() // Detener vibración al iniciar
            if (!timerRunning) {
                // Siempre usa el valor actual de workMinutes antes de iniciar
                if (isPaused && pauseTimeLeft > 0L) {
                    resumePomodoroSession()
                } else {
                    timeLeftInMillis = workMinutes * 60 * 1000L
                    startPomodoroSession()
                }
                timerRunning = true
                isPaused = false
                stopButton.text = getString(R.string.pause_button)
            }
        }

        stopButton.setOnClickListener {
            if (timerRunning) {
                pausePomodoroSession()
                stopButton.text = getString(R.string.resume_button)
            } else if (isPaused) {
                resumePomodoroSession()
                stopButton.text = getString(R.string.pause_button)
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
            Toast.makeText(this, getString(R.string.base_mode_selected), Toast.LENGTH_SHORT).show()
        }

        easyButton.setOnClickListener {
            stopIfRunningAndReset()
            workMinutes = 10
            breakMinutes = 5
            updateTimer()
            highlightSelectedSessionButton(easyButton, baseButton, customButton)
            Toast.makeText(this, getString(R.string.easy_mode_selected), Toast.LENGTH_SHORT).show()
        }

        customButton.setOnClickListener {
            stopIfRunningAndReset()
            showCustomTimeDialog {
                highlightSelectedSessionButton(customButton, baseButton, easyButton)
                Toast.makeText(this, getString(R.string.custom_mode_selected), Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de volumen (pausar/reanudar música)
        val volumeButton = findViewById<ImageButton>(R.id.volumeButton)
        updateVolumeIcon(volumeButton)
        volumeButton.setOnClickListener {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    isMusicPaused = true
                } else if (isMusicPaused) {
                    mediaPlayer.start()
                    isMusicPaused = false
                }
                updateVolumeIcon(volumeButton)
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
        // Detener el sonido de alarma si está sonando
        alarmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            alarmPlayer = null
        }
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
                // Reproducir sonido de alarma al finalizar
                alarmPlayer = MediaPlayer.create(this@PomodoroActivity, R.raw.alarm_clock_sound)
                alarmPlayer?.setOnCompletionListener { it.release(); alarmPlayer = null }
                alarmPlayer?.start()
                vibrateOnAlarm()
                // No iniciar el siguiente tiempo automáticamente
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

    private fun pausePomodoroSession() {
        countDownTimer?.cancel()
        pauseTimeLeft = timeLeftInMillis
        timerRunning = false
        isPaused = true
        // Pausar la música si está sonando
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isMusicPaused = true
        }
    }

    private fun resumePomodoroSession() {
        countDownTimer = object : CountDownTimer(pauseTimeLeft, 1000) {
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
                alarmPlayer = MediaPlayer.create(this@PomodoroActivity, R.raw.alarm_clock_sound)
                alarmPlayer?.setOnCompletionListener { it.release(); alarmPlayer = null }
                alarmPlayer?.start()
                vibrateOnAlarm()
            }
        }.start()
        timerRunning = true
        isPaused = false
        // Reanudar la música si estaba pausada
        if (::mediaPlayer.isInitialized && isMusicPaused) {
            mediaPlayer.start()
            isMusicPaused = false
        }
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
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        isMusicPaused = false // Reinicia el estado al reproducir nueva música
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
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_time)
        dialog.setCancelable(true)

        val input = dialog.findViewById<EditText>(R.id.inputMinutes)
        val inputBreak = dialog.findViewById<EditText>(R.id.inputBreakMinutes)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSet = dialog.findViewById<Button>(R.id.btnSet)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSet.setOnClickListener {
            val work = input.text.toString().toIntOrNull() ?: 25
            val brk = inputBreak.text.toString().toIntOrNull() ?: 5
            if (brk > work) {
                Toast.makeText(this, getString(R.string.break_cannot_be_greater_than_work), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            workMinutes = work
            breakMinutes = brk
            updateTimer()
            timeLeftInMillis = workMinutes * 60 * 1000L
            dialog.dismiss()
            onSelected?.invoke()
        }
        dialog.show()
    }

    private fun vibrateOnAlarm() {
        try {
            val pattern = longArrayOf(0, 500, 500) // vibrar 500ms, esperar 500ms, repetir
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 = repetir desde el inicio
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, 0)
                }
                isVibrating = true
            }
        } catch (e: Exception) {
            // No hacer nada si falla la vibración
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            isVibrating = false
        } catch (e: Exception) {
            // No hacer nada si falla
        }
    }

    private fun updateVolumeIcon(volumeButton: ImageButton) {
        if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) {
            volumeButton.setImageResource(R.drawable.icon_1)
        } else {
            volumeButton.setImageResource(R.drawable.icon_5)
        }
    }

    override fun onPause() {
        super.onPause()
        stopVibration()
        // Stop and release music when leaving the screen or app
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        // Detener y liberar el sonido de alarma si está sonando
        alarmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            alarmPlayer = null
        }
        isMusicPaused = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibration()
        // Release MediaPlayer resources
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        // Detener y liberar el sonido de alarma si está sonando
        alarmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            alarmPlayer = null
        }
        isMusicPaused = false
    }
}
