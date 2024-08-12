package com.example.lowprice.ViewModel

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.lowprice.R

class PerfilUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_user)

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")

        val textViewName: TextView = findViewById(R.id.name_profile)
        textViewName.text = "Olá, ${userName ?: "usuário"}"

        val imgProfile: ImageView = findViewById(R.id.photo_profile)
        loadProfileImage(imgProfile)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutProfile: ImageView = findViewById(R.id.logout_profile)
        val logoutText: TextView = findViewById(R.id.logout_text)
        val rightProfileLogout: ImageView = findViewById(R.id.right_profile_logout)

        val logoutClickListener = {
            showLogoutConfirmationDialog()
        }

        logoutProfile.setOnClickListener { logoutClickListener() }
        logoutText.setOnClickListener { logoutClickListener() }
        rightProfileLogout.setOnClickListener { logoutClickListener() }

        val termsTextView: TextView = findViewById(R.id.form_text_profile)
        val profileImageView: ImageView = findViewById(R.id.form_profile)

        val showTermsListener = {
            showTermsAndConditions()
        }

        termsTextView.setOnClickListener { showTermsListener() }
        profileImageView.setOnClickListener { showTermsListener() }
    }

    private fun loadProfileImage(imageView: ImageView) {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("profileImage", null)
        encodedImage?.let {
            val byteArray = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            val bitmapDrawable = BitmapDrawable(resources, bitmap)

            Glide.with(this)
                .load(bitmapDrawable)
                .transform(CircleCrop())
                .into(imageView)
        }


    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Você realmente deseja sair?")
            .setPositiveButton("Sim") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showTermsAndConditions() {
        val terms = getString(R.string.terms_and_conditions)

        AlertDialog.Builder(this)
            .setTitle("Termos e Condições")
            .setMessage(terms)
            .setPositiveButton("Fechar", null)
            .show()
    }

}
