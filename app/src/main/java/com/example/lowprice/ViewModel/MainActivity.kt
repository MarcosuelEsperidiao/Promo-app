package com.example.lowprice.ViewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lowprice.Model.ApiService.LoginCheck_Api
import com.example.lowprice.Model.LoginRequest
import com.example.lowprice.Model.LoginResponse
import com.example.lowprice.R
import com.example.lowprice.Model.UserCreatActivity
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

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        val isAuthenticated = sharedPreferences.getBoolean("isAuthenticated", false)
        if (isAuthenticated) {
            val intent = Intent(this, LayoutUserActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val textCreate: TextView = findViewById(R.id.text_creat)
        val editPhone: EditText = findViewById(R.id.edit_name)
        val editPassword: EditText = findViewById(R.id.edit_password)
        val btnLogin: Button = findViewById(R.id.btn_button)

        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        textCreate.setOnClickListener {
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
            val intent = Intent(this, UserCreatActivity::class.java)
            startActivity(intent)
        }

        // Adicionar TextWatcher ao EditText de telefone para formatação
        editPhone.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val hint = editPhone.hint // Armazenar o hint original

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val raw = s.toString().replace("\\D".toRegex(), "")
                val formatted = formatPhoneNumber(raw)

                isUpdating = true
                s?.replace(0, s.length, formatted)
                isUpdating = false

                if (s.isNullOrEmpty()) {
                    editPhone.hint = hint // Restaurar o hint quando o campo estiver vazio
                }
            }

            private fun formatPhoneNumber(s: String): String {
                val length = s.length
                return when {
                    length == 0 -> ""
                    length <= 2 -> "($s"
                    length <= 7 -> "(${s.substring(0, 2)}) ${s.substring(2)}"
                    length <= 11 -> "(${s.substring(0, 2)}) ${s.substring(2, 3)} ${
                        s.substring(
                            3,
                            7
                        )
                    }-${s.substring(7)}"

                    else -> "(${s.substring(0, 2)}) ${s.substring(2, 3)} ${
                        s.substring(
                            3,
                            7
                        )
                    }-${s.substring(7, 11)}"
                }
            }
        })

        btnLogin.setOnClickListener {
            val phone = editPhone.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT)
                    .show()
            } else if (phone.length < 16) { // Número de telefone formatado deve ter 16 caracteres
                Toast.makeText(
                    this,
                    "O número de telefone deve ter no mínimo 11 dígitos",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password.length != 6) { // Senha deve ter no mínimo 6 dígitos
                Toast.makeText(this, "Dados inválidos", Toast.LENGTH_SHORT).show()
            } else {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://144.22.225.3:5000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(LoginCheck_Api::class.java)
                val loginRequest = LoginRequest(phone, password)

                service.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null) {
                                sharedPreferences.edit().putBoolean("isAuthenticated", true).apply()
                                sharedPreferences.edit().putString("userName", loginResponse.name)
                                    .apply()
                                val intent =
                                    Intent(this@MainActivity, LayoutUserActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Dados inválidos", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }
        }
    }
}
