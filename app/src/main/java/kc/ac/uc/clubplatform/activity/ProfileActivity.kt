// ProfileActivity.kt
package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupProfileInfo()
        setupMenuItems()
    }

    private fun setupHeader() {
        // 뒤로가기 버튼 설정
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupProfileInfo() {
        // 실제 앱에서는 사용자 정보를 DB나 Preferences에서 가져와야 함
        // 여기서는 샘플 데이터 사용
        binding.tvUserName.text = "홍길동"
        binding.tvSchoolName.text = "서울대학교"
        binding.tvMajor.text = "컴퓨터공학과"
        binding.tvStudentId.text = "2020123456"

        // 프로필 관리 버튼 클릭 이벤트
        binding.btnManageProfile.setOnClickListener {
            showProfileManageDialog()
        }
    }

    private fun setupMenuItems() {
        // 계정 설정 아이템들
        binding.layoutUserId.setOnClickListener {
            // 아이디 정보는 클릭해도 아무 동작 없음 (표시만 하는 용도)
        }

        binding.layoutPasswordChange.setOnClickListener {
            showPasswordChangeDialog()
        }

        // 앱 버전 정보 표시
        binding.tvAppVersion.text = "1.0.0"

        // 회원 탈퇴 버튼
        binding.btnWithdraw.setOnClickListener {
            showWithdrawDialog()
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showProfileManageDialog() {
        val options = arrayOf("학과 설정", "프로필 사진 변경")
        AlertDialog.Builder(this)
            .setTitle("프로필 관리")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDepartmentSettingDialog()
                    1 -> showProfileImageOptions()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDepartmentSettingDialog() {
        // 학과 설정 화면으로 전환 (실제 구현에서는 별도의 액티비티나 다이얼로그로 구현)
        Toast.makeText(this, "학과 설정 화면으로 이동합니다", Toast.LENGTH_SHORT).show()
    }

    private fun showProfileImageOptions() {
        val options = arrayOf("프로필 사진 변경", "프로필 사진 삭제")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // 갤러리에서 이미지 선택 (실제 구현에서는 Intent 사용)
                        Toast.makeText(this, "갤러리에서 이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // 프로필 이미지 삭제
                        Toast.makeText(this, "프로필 사진이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showPasswordChangeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        AlertDialog.Builder(this)
            .setTitle("비밀번호 변경")
            .setView(dialogView)
            .setPositiveButton("변경") { _, _ ->
                // 비밀번호 변경 로직 (실제 구현에서는 입력값 검증 및 API 호출 필요)
                Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showWithdrawDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_withdraw, null)

        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.")
            .setView(dialogView)
            .setPositiveButton("탈퇴") { _, _ ->
                // 회원 탈퇴 로직 (실제 구현에서는 비밀번호 확인 및 API 호출 필요)
                Toast.makeText(this, "회원 탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()

                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                // 로그아웃 로직 (세션 정보 삭제 등)
                Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()

                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}