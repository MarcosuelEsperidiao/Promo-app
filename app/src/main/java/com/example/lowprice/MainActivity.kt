package com.example.lowprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textCreate: TextView = findViewById(R.id.text_creat)
        val editName: EditText = findViewById(R.id.edit_name)
        val editPassword: EditText = findViewById(R.id.edit_password)
        val btnLogin: Button = findViewById(R.id.btn_button)

        // Inicialize o Vibrator
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        textCreate.setOnClickListener {
            // Verifique se o dispositivo tem suporte para vibração
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            val intent = Intent(this, user_creat::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = editName.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // Verifique se o dispositivo tem suporte para vibração
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, layout_user::class.java)
                startActivity(intent)
            }
        }
    }
}
