package kc.ac.uc.clubplatform.api

import kc.ac.uc.clubplatform.models.LoginRequest
import kc.ac.uc.clubplatform.models.LoginResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("users")
    suspend fun testConnection(): Response<Any>
}
