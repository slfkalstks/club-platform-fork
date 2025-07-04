package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kc.ac.uc.clubplatform.MainActivity
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.api.LoginRequest
import kc.ac.uc.clubplatform.api.RefreshTokenRequest
import kotlinx.coroutines.launch
import kc.ac.uc.clubplatform.databinding.ActivityLoginBinding
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자동 로그인 개선
        checkAutoLogin()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.etEmail.error = "이메일을 입력해주세요"
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "비밀번호를 입력해주세요"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val loginTime = System.currentTimeMillis() // 현재 시간(ms)
                val loginRequest = LoginRequest(email, password, loginTime) // loginTime 추가
                val response = ApiClient.apiService.login(loginRequest)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true) {
                        // 로그인 성공 처리
                        val token = loginResponse.accessToken
                        val refreshToken = loginResponse.refreshToken
                        val user = loginResponse.user

                        // 토큰 로그 출력
                        Log.d(TAG, "accessToken: $token")
                        Log.d(TAG, "refreshToken: $refreshToken")

                        // 토큰과 유저 정보 저장
                        if (token != null && refreshToken != null && user != null) {
                            // 서버 응답의 이메일 대신 사용자가 입력한 이메일 사용
                            saveTokens(
                                token,
                                refreshToken,
                                user.userId.toString(),
                                user.name,
                                user.university,
                                user.major,
                                user.studentId,
                                user.profileImage,
                                email, // 서버 응답의 user.email 대신 입력한 이메일 사용
                                password // 비밀번호도 저장
                            )
                            navigateToMain()
                        } else {
                            showToast("토큰 정보가 없습니다.")
                        }
                    } else {
                        // 로그인 실패 (서버에서 success = false로 응답)
                        showToast(loginResponse?.message ?: "로그인에 실패했습니다.")
                    }
                } else {
                    // HTTP 에러 처리
                    showToast("로그인 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                // 예외 처리
                showToast("로그인 오류: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.btnSignup.isEnabled = !isLoading
    }

    private fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userName: String?,
        schoolName: String?,
        major: String?,
        studentId: String?,
        profileImageUrl: String?,
        email: String, // 이메일 파라미터
        password: String // 비밀번호 파라미터 추가
    ) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        
        // userId 로깅
        Log.d(TAG, "Saving user ID: $userId")
        Log.d(TAG, "Saving user email: $email") // 이메일 로깅 추가
        
        with(sharedPref.edit()) {
            // 기존 ID/PW 제거
            remove("id")
            remove("pw")

            // 토큰 저장
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("user_id", userId)
            putBoolean("is_logged_in", true)
            // 사용자 정보 저장
            putString("user_name", userName)
            putString("school_name", schoolName)
            putString("major", major)
            putString("student_id", studentId)
            putString("email", email) // 이메일 저장
            putString("pw", password) // 비밀번호 저장
            // 프로필 이미지 URL은 더 이상 저장하지 않음 - API에서 직접 가져오기 때문
            apply()
        }
        Log.d(TAG, "토큰 및 사용자 정보 저장 완료")
        Log.d(TAG, "TOKEN: accessToken=$accessToken, refreshToken=$refreshToken, userId=$userId")
        Log.d(TAG, "USER EMAIL: $email") // 이메일 로깅 추가
    }

    private fun navigateToMain() {
        // 로그인 성공 후 동아리 가입 여부 확인
        checkClubMembership()
    }

    private fun checkClubMembership() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getMyClubs()

                if (response.isSuccessful && response.body()?.success == true) {
                    val clubs = response.body()?.data ?: emptyList()

                    if (clubs.isNotEmpty()) {
                        // 기존 사용자: 저장된 현재 동아리 정보 확인
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val savedClubId = sharedPreferences.getInt("current_club_id", -1)
                        val savedClubName = sharedPreferences.getString("current_club_name", "")

                        // 저장된 동아리가 현재 가입된 동아리 목록에 있는지 확인
                        val savedClub = clubs.find { it.clubId == savedClubId }

                        if (savedClub != null && !savedClubName.isNullOrEmpty()) {
                            // 저장된 동아리가 유효하면 그대로 사용 (기존 사용자)
                            Log.d(TAG, "Using saved club: ${savedClub.name} (ID: ${savedClub.clubId})")
                        } else {
                            // 저장된 동아리가 없거나 유효하지 않으면 첫 번째 동아리 설정 (새 사용자 또는 동아리 탈퇴한 경우)
                            val firstClub = clubs.first()
                            saveCurrentClub(firstClub.clubId, firstClub.name)
                            Log.d(TAG, "Setting first club as current: ${firstClub.name} (ID: ${firstClub.clubId})")
                        }

                        // 메인 화면으로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 가입된 동아리가 없으면 동아리 가입 화면으로 이동
                        val intent = Intent(this@LoginActivity, ClubJoinActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // API 호출 실패 시 동아리 가입 화면으로 이동
                    val intent = Intent(this@LoginActivity, ClubJoinActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check club membership", e)
                // 예외 발생 시 동아리 가입 화면으로 이동
                val intent = Intent(this@LoginActivity, ClubJoinActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun saveCurrentClub(clubId: Int, clubName: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("current_club_id", clubId)
            .putString("current_club_name", clubName)
            .apply()

        Log.d(TAG, "Current club saved: $clubName (ID: $clubId)")
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 자동 로그인 검사 및 처리
     */
    private fun checkAutoLogin() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        val accessToken = sharedPref.getString("access_token", null)
        val refreshToken = sharedPref.getString("refresh_token", null)
        val email = sharedPref.getString("email", null)
        var password = sharedPref.getString("pw", null) // 비밀번호 저장되어 있다고 가정

        Log.d(TAG, "자동 로그인 검사: isLoggedIn=$isLoggedIn, accessToken=${accessToken != null}, refreshToken=${refreshToken != null}, email=${email != null}, password=${password != null}")

        if (isLoggedIn && email != null) {
            // 비밀번호가 없으면 사용자에게 입력받거나, 자동로그인 불가 처리
            if (password == null) {
                // 비밀번호가 없으면 자동로그인 불가, 로그인 화면 유지
                Log.d(TAG, "자동로그인 불가: 저장된 비밀번호 없음")
                return
            }
            // 자동 로그인 시에도 서버에 로그인 요청을 보내서 last_login_at 갱신
            performLogin(email, password)
        } else if (isLoggedIn && accessToken != null && refreshToken != null) {
            // 기존 토큰 방식 유지 (이메일/비밀번호가 없을 때)
            if (email == null) {
                Log.d(TAG, "이메일 정보 없음, 사용자 정보 다시 가져와야 함")
                // 여기서 사용자 정보를 API로 다시 가져오는 로직을 구현할 수 있음
                // 지금은 일단 토큰 유효성 검사로 진행
            }
            if (isTokenValid(accessToken)) {
                Log.d(TAG, "유효한 토큰으로 자동 로그인 진행")
                navigateToMain()
            } else {
                Log.d(TAG, "액세스 토큰 만료, 리프레시 토큰으로 갱신 시도")
                refreshAccessToken(refreshToken)
            }
        } else {
            Log.d(TAG, "자동 로그인 조건 미충족")
        }
    }

    /**
     * 토큰의 유효성 검사 (JWT 토큰 구조 확인 및 만료 시간 검증)
     */
    private fun isTokenValid(token: String): Boolean {
        try {
            // 간단한 JWT 토큰 만료 시간 확인 로직
            // 실제 구현에서는 라이브러리 사용 권장 (예: com.auth0:java-jwt)
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w(TAG, "JWT 토큰 형식이 아님")
                return false
            }
            
            // JWT 페이로드 디코딩 (Base64)
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
            val payloadJson = String(payload)
            
            // 만료 시간 확인 (exp 필드)
            // 간단한 구현을 위해 정규식 사용
            val expPattern = "\"exp\":(\\d+)".toRegex()
            val matchResult = expPattern.find(payloadJson)
            
            if (matchResult != null) {
                val expTime = matchResult.groupValues[1].toLong()
                val currentTime = System.currentTimeMillis() / 1000
                
                Log.d(TAG, "토큰 만료 시간: $expTime, 현재 시간: $currentTime")
                return expTime > currentTime
            }
            
            Log.w(TAG, "토큰에 만료 시간(exp) 정보 없음")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "토큰 유효성 검사 중 오류 발생", e)
            return false
        }
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰 갱신
     */
    private fun refreshAccessToken(refreshToken: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                Log.d(TAG, "토큰 갱신 시도")
                
                // 토큰 갱신 API 호출 - RefreshTokenRequest 객체로 변환하여 전달
                val refreshRequest = RefreshTokenRequest(refreshToken)
                val response = ApiClient.apiService.refreshToken(refreshRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val newAccessToken = response.body()?.accessToken
                    val newRefreshToken = response.body()?.refreshToken
                    
                    if (newAccessToken != null) {
                        // 새 토큰 저장 (이메일은 그대로 유지)
                        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val email = sharedPref.getString("email", null)
                        
                        with(sharedPref.edit()) {
                            putString("access_token", newAccessToken)
                            if (newRefreshToken != null) {
                                putString("refresh_token", newRefreshToken)
                            }
                            
                            // 이메일 정보 확인 및 로깅
                            if (email != null) {
                                Log.d(TAG, "토큰 갱신 중 기존 이메일 정보 유지: $email")
                            } else {
                                Log.w(TAG, "토큰 갱신 중 이메일 정보 없음")
                                // 여기서 서버로부터 사용자 정보를 가져오는 API를 호출할 수도 있음
                            }
                            
                            apply()
                        }
                        
                        Log.d(TAG, "토큰 갱신 성공, 메인 화면으로 이동")
                        navigateToMain()
                    } else {
                        Log.w(TAG, "새 액세스 토큰이 없음")
                        clearLoginState()
                    }
                } else {
                    Log.w(TAG, "토큰 갱신 실패: ${response.errorBody()?.string()}")
                    clearLoginState()
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 갱신 중 오류 발생", e)
                clearLoginState()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 로그인 상태 초기화
     */
    private fun clearLoginState() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_logged_in", false)
            // 이메일과 같은 사용자 정보는 유지 (다음 로그인에 사용될 수 있음)
            // 전체 정보를 지우려면 clear() 사용
            apply()
        }
        Log.d(TAG, "로그인 상태 초기화됨")
    }
}