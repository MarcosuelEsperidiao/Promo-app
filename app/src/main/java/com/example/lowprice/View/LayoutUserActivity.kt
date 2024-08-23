package com.example.lowprice.View

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.lowprice.Model.ApiService.AddProduct_Api
import com.example.lowprice.Model.Product_Add
import com.example.lowprice.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class LayoutUserActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var scrollView: ScrollView
    private lateinit var imgPerfil: ImageView

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    @SuppressLint("MissingInflatedId", "SetTextI18n")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layout_user)

        getWindow().setNavigationBarColor(
            ContextCompat.getColor(
                this,
                R.color.navigation_bar_color
            )
        );


        scrollView = findViewById(R.id.scroll_view)
        imgPerfil = findViewById(R.id.img_perfil)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")

        val textViewName: TextView = findViewById(R.id.t_name)
        textViewName.text = "Olá, ${userName ?: "usuário"}"


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val profileUser: ImageView = findViewById(R.id.profile_user_)
        profileUser.setOnClickListener {
            val intent = Intent(this, PerfilUserActivity::class.java)
            startActivity(intent)
        }

        val iconAddCircle: ImageView = findViewById(R.id.icon_add_circle)
        iconAddCircle.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)

        }

        val iconHome: ImageView = findViewById(R.id.icon_home)
        iconHome.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchProducts()
        }


        imgPerfil.setOnClickListener {
            showImagePickerDialog()
        }

        // Carregar imagem de perfil salva
        loadProfileImage()


        // Fetch products initially
        fetchProducts()
    }


    private fun showImagePickerDialog() {
        val options = arrayOf("Tirar Foto", "Escolher da Galeria")
        MaterialAlertDialogBuilder(this)
            .setTitle("Escolha uma opção")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    saveProfileImage(imageBitmap)
                    Glide.with(this)
                        .load(imageBitmap)
                        .transform(CircleCrop())
                        .into(imgPerfil)
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    selectedImage?.let {
                        val inputStream = contentResolver.openInputStream(it)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        saveProfileImage(imageBitmap)
                        Glide.with(this)
                            .load(selectedImage)
                            .transform(CircleCrop())
                            .into(imgPerfil)
                    }
                }
            }
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
        editor.putString("profileImage", encodedImage)
        editor.apply()
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("profileImage", null)
        encodedImage?.let {
            val byteArray = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            Glide.with(this)
                .load(bitmap)
                .transform(CircleCrop())
                .into(imgPerfil)
        }
    }

    private fun fetchProducts() {


        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.64:5000/") // Altere para o endereço correto do seu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(AddProduct_Api::class.java)

        service.getProducts().enqueue(object : Callback<List<Product_Add>> {
            override fun onResponse(
                call: Call<List<Product_Add>>,
                response: Response<List<Product_Add>>
            ) {
                if (response.isSuccessful) {
                    val productList = response.body() ?: emptyList()
                    addProductsToLayout(productList)
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    Toast.makeText(
                        this@LayoutUserActivity,
                        "Failed to load products",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Product_Add>>, t: Throwable) {
                Toast.makeText(this@LayoutUserActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun addProductsToLayout(productAdds: List<Product_Add>) {
        val productListLayout = findViewById<LinearLayout>(R.id.product_list_layout)
        productListLayout.removeAllViews()


        for (product in productAdds) {
            try {
                val productView = layoutInflater.inflate(R.layout.product_item, null)

                val textLocation = productView.findViewById<TextView>(R.id.text_location)
                val textLocario = productView.findViewById<TextView>(R.id.text_locario)
                val textPriceDetail = productView.findViewById<TextView>(R.id.text_price_detail)
                val imageViewPreview = productView.findViewById<ImageView>(R.id.imageViewPreview)

                val textUserName = productView.findViewById<TextView>(R.id.text_user_name)
                val imageViewProfile = productView.findViewById<ImageView>(R.id.imageViewProfile)



                textLocation.text = product.location
                textLocario.text = "Endereço: ${product.locario}"
                textPriceDetail.text = "Preço: R$ ${product.price}"

                product.image?.let {
                    if (it.isNotEmpty()) {
                        val byteArray = Base64.decode(it, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        imageViewPreview.setImageBitmap(bitmap)
                    }
                }

                textUserName.text = product.userName

                product.profileImage?.let {
                    if (it.isNotEmpty()) {
                        val byteArray = Base64.decode(it, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        Glide.with(this)
                            .load(bitmap)
                            .transform(CircleCrop())
                            .into(imageViewProfile)
                    }
                }

                productListLayout.addView(productView)
            } catch (e: Exception) {
                // Log the exception if necessary
                e.printStackTrace()
                continue // Continue with the next product if there's an error
            }
        }
    }
}
