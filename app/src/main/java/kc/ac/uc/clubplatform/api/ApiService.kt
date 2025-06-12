package kc.ac.uc.clubplatform.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun registerUser(@Body userData: RegisterRequest): Response<RegisterResponse>
    
    @POST("auth/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): Response<LogoutResponse>

    @GET("users")
    suspend fun testConnection(): Response<Any>
}

// ...데이터 모델 클래스는 별도 파일로 이동...
