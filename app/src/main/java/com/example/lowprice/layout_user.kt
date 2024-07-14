package com.example.lowprice

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class layout_user : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layout_user)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val iconAddCircle: ImageView = findViewById(R.id.icon_add_circle)
        iconAddCircle.setOnClickListener {
            val intent = Intent(this, add_product::class.java)
            startActivity(intent)
        }

        // Receber dados das preferências compartilhadas
        val sharedPreferences = getSharedPreferences("ProductInfo", Context.MODE_PRIVATE)
        val productCount = sharedPreferences.getInt("product_count", 0)

        val productListLayout = findViewById<LinearLayout>(R.id.product_list_layout)

        for (i in 0 until productCount) {
            val location = sharedPreferences.getString("text_location_$i", "")
            val locario = sharedPreferences.getString("text_locario_$i", "")
            val priceText = sharedPreferences.getString("price_$i", "")
            val imageString = sharedPreferences.getString("image_$i", "")


            val productView = layoutInflater.inflate(R.layout.product_item, null)

            val textLocation = productView.findViewById<TextView>(R.id.text_location)
            val textLocario = productView.findViewById<TextView>(R.id.text_locario)
            val textPriceDetail = productView.findViewById<TextView>(R.id.text_price_detail)
            val imageViewPreview = productView.findViewById<ImageView>(R.id.imageViewPreview)


            textLocation.text = location
            textLocario.text = locario
            textPriceDetail.text = priceText

            if (!imageString.isNullOrEmpty()) {
                val byteArray = imageString.split(",").map { it.toByte() }.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                imageViewPreview.setImageBitmap(bitmap)
            }

            productListLayout.addView(productView)
        }
    }
}
