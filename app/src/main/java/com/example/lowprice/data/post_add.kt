package com.example.lowprice.data

import com.example.lowprice.data.model.Product
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ProductService {
    @POST("/products")
    fun addProduct(@Body product: Product): Call<Void>

    @GET("/products")
    fun getProducts(): Call<List<Product>>

}
