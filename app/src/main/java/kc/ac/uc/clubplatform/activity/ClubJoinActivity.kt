package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kc.ac.uc.clubplatform.MainActivity
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.models.ClubJoinRequest
import kc.ac.uc.clubplatform.databinding.ActivityClubJoinBinding
import kotlinx.coroutines.launch

class ClubJoinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClubJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClubJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // 완료 버튼 클릭 리스너
        binding.btnJoinClub.setOnClickListener {
            val inviteCode = binding.etInviteCode.text.toString().trim()

            if (inviteCode.isEmpty()) {
                binding.etInviteCode.error = "가입 코드를 입력해주세요"
                return@setOnClickListener
            }

            joinClub(inviteCode)
        }

        // 나중에 하기 버튼 (선택사항)
        binding.btnSkip.setOnClickListener {
            navigateToMain()
        }
    }

    private fun joinClub(inviteCode: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val request = ClubJoinRequest(inviteCode)
                val response = ApiClient.apiService.joinClub(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val joinResponse = response.body()!!

                    // 가입 성공 시 동아리 목록을 다시 조회해서 최신 정보 저장
                    if (joinResponse.clubId != null) {
                        loadAndSaveClubInfo(joinResponse.clubId)
                    } else {
                        // clubId가 없으면 전체 동아리 목록을 조회해서 가장 최근 가입한 동아리 찾기
                        loadMyClubsAndNavigate()
                    }

                    showToast("동아리에 성공적으로 가입되었습니다!")
                } else {
                    val errorMessage = response.body()?.message ?: "가입 코드가 유효하지 않습니다"
                    showToast(errorMessage)
                    binding.etInviteCode.error = "유효하지 않은 가입 코드입니다"
                }
            } catch (e: Exception) {
                showToast("네트워크 오류가 발생했습니다: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private suspend fun loadAndSaveClubInfo(clubId: Int) {
        try {
            val clubsResponse = ApiClient.apiService.getMyClubs()
            if (clubsResponse.isSuccessful && clubsResponse.body()?.success == true) {
                val clubs = clubsResponse.body()?.data ?: emptyList()
                val targetClub = clubs.find { it.clubId == clubId }

                if (targetClub != null) {
                    saveCurrentClub(targetClub.clubId, targetClub.name)
                    navigateToMain()
                } else {
                    // 찾지 못했으면 첫 번째 동아리로 설정
                    if (clubs.isNotEmpty()) {
                        val firstClub = clubs.first()
                        saveCurrentClub(firstClub.clubId, firstClub.name)
                    }
                    navigateToMain()
                }
            } else {
                navigateToMain()
            }
        } catch (e: Exception) {
            navigateToMain()
        }
    }

    private suspend fun loadMyClubsAndNavigate() {
        try {
            val clubsResponse = ApiClient.apiService.getMyClubs()
            if (clubsResponse.isSuccessful && clubsResponse.body()?.success == true) {
                val clubs = clubsResponse.body()?.data ?: emptyList()

                if (clubs.isNotEmpty()) {
                    // 새로 가입한 사용자: 가장 최근에 가입한 동아리 (마지막 동아리)를 현재 동아리로 설정
                    val latestClub = clubs.last()
                    saveCurrentClub(latestClub.clubId, latestClub.name)
                }
            }
        } catch (e: Exception) {
            // 오류 발생 시 로그만 남기고 계속 진행
        }
        navigateToMain()
    }

    private fun saveCurrentClub(clubId: Int, clubName: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("current_club_id", clubId)
            .putString("current_club_name", clubName)
            .apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnJoinClub.isEnabled = !isLoading
        binding.etInviteCode.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}