package kc.ac.uc.clubplatform.api

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
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?
)

data class LogoutRequest(
    val refreshToken: String
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class User(
    val userId: Int,
    val email: String,
    val name: String,
    val university: String,
    val department: String,
    val major: String,
    val studentId: String,
    val profileImage: String?
)
