package com.example.lowprice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lowprice.app.router_backend.Product
import com.example.lowprice.app.router_backend.ProductService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class add_product : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_IMAGE_CAPTURE = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextLocario: EditText
    private lateinit var imageViewPreview: ImageView
    private lateinit var editTextLocation: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var sendButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        editTextLocario = findViewById(R.id.text_locario)
        imageViewPreview = findViewById(R.id.imageViewPreview)
        editTextLocation = findViewById(R.id.text_location)
        editTextPrice = findViewById(R.id.price)
        val imageViewLocation: ImageView = findViewById(R.id.imageView_location)
        sendButton = findViewById(R.id.sendButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageViewCamera: ImageView = findViewById(R.id.imageViewCamera)

        imageViewCamera.setOnClickListener {
            abrirCamera()
        }

        imageViewLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                getLastLocation()
            }
        }

        sendButton.setOnClickListener {
            salvarInformacoes()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAllFields()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        editTextLocation.addTextChangedListener(textWatcher)
        editTextLocario.addTextChangedListener(textWatcher)
        editTextPrice.addTextChangedListener(textWatcher)
        imageViewPreview.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            checkAllFields()
        }

        checkAllFields()
    }

    private fun abrirCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                imageViewPreview.setImageBitmap(it)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getAddressFromLocation(latitude, longitude)
                } else {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!

            if (addresses.isNotEmpty()) {
                val address: String = addresses[0].getAddressLine(0)
                editTextLocario.setText(address)
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Geocoder error", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error retrieving address", Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarInformacoes() {
        val locationText = editTextLocation.text.toString()
        val locarioText = editTextLocario.text.toString()
        val priceText = editTextPrice.text.toString()

        val sharedPreferences = getSharedPreferences("ProductInfo", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Recuperar o contador de produtos e incrementar
        val productCount = sharedPreferences.getInt("product_count", 0)

        editor.putString("text_location_$productCount", locationText)
        editor.putString("text_locario_$productCount", locarioText)
        editor.putString("price_$productCount", priceText)

        val bitmap = (imageViewPreview.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val imageString = Base64.encodeToString(byteArray, Base64.DEFAULT)
        editor.putString("image_$productCount", imageString)


        editor.putInt("product_count", productCount + 1)

        editor.apply()

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

        val service = retrofit.create(ProductService::class.java)


        val product = Product(locationText, locarioText, priceText.toFloat(), imageString,)

        service.addProduct(product).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@add_product,
                        "Product added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navegar para layout_user
                    val intent = Intent(this@add_product, layout_user::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@add_product, "Failed to add product", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@add_product, "Error: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAllFields() {
        val isLocationFilled = editTextLocation.text.toString().isNotEmpty()
        val isLocarioFilled = editTextLocario.text.toString().isNotEmpty()
        val isPriceFilled = editTextPrice.text.toString().isNotEmpty()
        val isImageFilled = imageViewPreview.drawable != null

        sendButton.isEnabled = isLocationFilled && isLocarioFilled && isPriceFilled && isImageFilled
    }
}

