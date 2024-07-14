package com.example.lowprice.app.router_backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Defina uma interface Retrofit para suas chamadas API
interface ProductService {
    @POST("/products")
    fun addProduct(@Body product: Product): Call<Void>
}

// Classe de modelo de dados para enviar para o servidor
data class Product(
    val location: String,
    val locario: String,
    val price: Float,
    val image: String?  // Se você está enviando um caminho de imagem, pode ser uma string
)