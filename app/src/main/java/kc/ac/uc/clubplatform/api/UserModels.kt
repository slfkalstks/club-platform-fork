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
    val password: String,
    val loginTime: Long? = null // 로그인 시간(ms) 추가
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

// 비밀번호 변경 요청 모델
data class ChangePasswordRequest(
    val userId: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

// 회원탈퇴 요청 모델
data class WithdrawRequest(
    val userId: String,
    val password: String
)

// 공통 API 응답 포맷
data class ApiResponse(
    val success: Boolean,
    val message: String?
)

// Base64 이미지 업로드 요청 데이터 클래스
data class UpdateProfileImageBase64Request(
    val userId: String,
    val base64Image: String
)

// 학과정보 변경 요청 데이터 클래스
data class UpdateDepartmentRequest(
    val userId: String,
    val department: String,
    val major: String
)

// RefreshToken 요청 데이터 클래스
data class RefreshTokenRequest(
    val refreshToken: String
)