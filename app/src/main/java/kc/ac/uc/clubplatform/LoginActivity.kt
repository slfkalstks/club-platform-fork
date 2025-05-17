package kc.ac.uc.clubplatform

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kc.ac.uc.clubplatform.api.RetrofitClient
import kc.ac.uc.clubplatform.databinding.ActivityLoginBinding
import kc.ac.uc.clubplatform.models.LoginRequest
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 연결 테스트 - 앱 시작 시 실행
        testApiConnection()

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

    private fun testApiConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "API 연결 테스트 시작...")
                val response = RetrofitClient.apiService.testConnection()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "API 연결 성공! 응답 코드: ${response.code()}")
                        Toast.makeText(this@LoginActivity,
                            "서버 연결 확인 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "API 연결 실패. 응답 코드: ${response.code()}")
                        Toast.makeText(this@LoginActivity,
                            "서버 응답: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "API 연결 테스트 중 예외 발생: ${e.javaClass.simpleName}: ${e.message}")
                withContext(Dispatchers.Main) {
                    val message = when (e) {
                        is UnknownHostException -> "서버 주소를 찾을 수 없습니다"
                        is SocketTimeoutException -> "서버 응답 시간 초과"
                        else -> "서버 연결 오류: ${e.message}"
                    }
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }
            }
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
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginRequest = LoginRequest(email, password)
                Log.d(TAG, "로그인 시도: $email")

                val response = RetrofitClient.apiService.login(loginRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!

                        if (loginResponse.success) {
                            // 로그인 성공
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인 성공!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // 토큰 저장
                            saveUserSession(loginResponse.token, loginResponse.userId.toString())

                            // 메인 화면으로 이동
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // 로그인 실패 (서버에서 success=false)
                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse.message ?: "로그인에 실패했습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // HTTP 에러 (4xx, 5xx)
                        val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                        Log.e(TAG, "로그인 실패: 코드 ${response.code()}, 에러: $errorBody")

                        Toast.makeText(
                            this@LoginActivity,
                            "로그인 실패: ${response.code()} - $errorBody",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "로그인 예외: ${e.javaClass.simpleName}: ${e.message}")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    val message = when (e) {
                        is UnknownHostException -> "서버 주소를 찾을 수 없습니다"
                        is SocketTimeoutException -> "서버 응답 시간 초과"
                        else -> "로그인 오류: ${e.message}"
                    }

                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                }
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

    private fun saveUserSession(token: String?, userId: String?) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            putString("user_id", userId)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}