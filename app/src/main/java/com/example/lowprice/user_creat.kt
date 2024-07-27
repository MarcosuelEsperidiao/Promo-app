package com.example.lowprice

import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lowprice.app.router_backend.UserCreateRequest
import com.example.lowprice.app.router_backend.UserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class user_creat : AppCompatActivity() {

    private lateinit var userService: UserService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_creat)



        // Configure insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar Retrofit
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.64:5000/")  // Substitua pelo IP do seu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        userService = retrofit.create(UserService::class.java)

        // Obter referências para os elementos de interface do usuário
        val nameEditText: EditText = findViewById(R.id.text_name)
        val phoneEditText: EditText = findViewById(R.id.text_phone)
        val passwordEditText: EditText = findViewById(R.id.text_creat_password)
        val createAccountButton: Button = findViewById(R.id.btn_creat_count)

        // Adicionar OnClickListener ao botão "Criar conta"
        createAccountButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val userCreateRequest = UserCreateRequest(name, phone, password)

                userService.createUser(userCreateRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@user_creat, "Conta criada com sucesso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@user_creat, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@user_creat, "Falha ao criar conta", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@user_creat, "Erro: " + t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}