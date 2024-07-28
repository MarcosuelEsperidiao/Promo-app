package com.example.lowprice.app.router_backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val phone: String, val password: String)
data class LoginResponse(val message: String, val name: String)


interface Userlogin {
    @POST("/login")
    fun loginUser(@Body loginRequest: LoginRequest):  Call<LoginResponse>




}