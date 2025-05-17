package kc.ac.uc.clubplatform.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val userId: Int,
    val name: String,
    val email: String,
    val token: String
)