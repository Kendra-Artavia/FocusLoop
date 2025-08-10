package com.example.focusloop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configuración del nombre de la aplicación
        val appName: TextView = findViewById(R.id.appName)
        appName.text = getString(R.string.app_name)

        // Configuración del botón
        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            // Mostrar el mensaje de "Feliz Relax"
            Toast.makeText(this, getString(R.string.toast_message), Toast.LENGTH_SHORT).show()

            // Iniciar la siguiente actividad
            val intent = Intent(this, ScreenActivity::class.java)
            startActivity(intent)
        }
    }
}
