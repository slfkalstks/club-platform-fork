package kc.ac.uc.clubplatform.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun registerUser(@Body userData: RegisterRequest): Response<RegisterResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshRequest: RefreshTokenRequest): Response<LoginResponse>

    // 백엔드 엔드포인트와 일치하도록 경로 수정
    @GET("auth/profile-image/{userId}")
    suspend fun getProfileImage(@Path("userId") userId: String): Response<ResponseBody>

    @GET("users")
    suspend fun testConnection(): Response<Any>
    
    // 비밀번호 변경 API
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse>
    
    // 회원탈퇴 API
    @POST("auth/withdraw")
    suspend fun withdrawAccount(@Body request: WithdrawRequest): Response<ApiResponse>
    
    // 프로필 이미지 업로드 API (Base64 방식)
    @POST("auth/profile-image/update")
    suspend fun updateProfileImageBase64(@Body request: UpdateProfileImageBase64Request): Response<Map<String, Any>>
    
    // 학과정보 변경 API
    @POST("auth/update-department")
    suspend fun updateDepartment(@Body request: UpdateDepartmentRequest): Response<Map<String, Any>>
}


