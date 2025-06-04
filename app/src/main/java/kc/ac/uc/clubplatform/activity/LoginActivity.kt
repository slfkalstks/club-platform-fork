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

        // 토큰 기반 자동 로그인 확인
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", null)
        val refreshToken = sharedPref.getString("refresh_token", null)

        if (accessToken != null && refreshToken != null) {
            // 토큰이 존재하면 자동 로그인 (추후 토큰 유효성 검사 추가 필요)
            navigateToMain()
        }

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
        //테스트용
        binding.etEmail.setText("test@test.com")
        binding.etPassword.setText("123123123")
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
                val loginRequest = LoginRequest(email, password) // API 패키지의 LoginRequest 사용
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
                            saveTokens(token, refreshToken, user.userId.toString())
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

    private fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            // 기존 ID/PW 제거
            remove("id")
            remove("pw")

            // 토큰 저장
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("user_id", userId)
            putBoolean("is_logged_in", true)
            apply()
        }
        Log.d(TAG, "토큰 저장 완료")
        Log.d(TAG, "TOKEN: accessToken=$accessToken, refreshToken=$refreshToken, userId=$userId")
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }
}
