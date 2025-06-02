package kc.ac.uc.clubplatform.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("auth/register")
    fun registerUser(@Body userData: RegisterRequest): Call<RegisterResponse>

    @POST("auth/login")
    fun loginUser(@Body loginData: LoginRequest): Call<LoginResponse>
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val university: String,
    val department: String,
    val major: String,
    val studentId: String,
    val profileImage: String?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Int?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class User(
    val user_id: Int,
    val email: String,
    val name: String,
    val university: String,
    val department: String,
    val major: String,
    val studentId: String,
    val profileImage: String?
)
