package com.example.lowprice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.content.SharedPreferences
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
import com.example.lowprice.app.router_backend.LoginRequest
import com.example.lowprice.app.router_backend.Userlogin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        // Inicialize SharedPreferences
        var sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        // Verifique se o usuário está autenticado
        val isAuthenticated = sharedPreferences.getBoolean("isAuthenticated", false)
        if (isAuthenticated) {
            val intent = Intent(this, layout_user::class.java)
            startActivity(intent)
            finish()
            return
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
            val phone = editName.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // Verifique se o dispositivo tem suporte para vibração
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                // Configurar Retrofit
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.0.64:5000/") // Altere para o endereço correto do seu servidor
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(Userlogin::class.java)
                val loginRequest = LoginRequest(phone, password)

                // Enviar a solicitação de login
                service.loginUser(loginRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            // Login bem-sucedido, iniciar a próxima activity
                            sharedPreferences.edit().putBoolean("isAuthenticated", true).apply()
                            val intent = Intent(this@MainActivity, layout_user::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Login falhou, mostrar mensagem de erro
                            Toast.makeText(this@MainActivity, "Dados inválidos", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        // Falha na solicitação, mostrar mensagem de erro
                        Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
