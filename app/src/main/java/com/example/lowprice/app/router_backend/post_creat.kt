package com.example.lowprice.app.router_backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST



data class UserCreateRequest(val name: String, val phone: String, val password: String)



interface UserService {
    @POST("/users")  // Altere a URL para o endpoint correto
    fun createUser(@Body userCreateRequest: UserCreateRequest): Call<Void>

}