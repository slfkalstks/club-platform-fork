// RegisterActivity.kt
package kc.ac.uc.clubplatform

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kc.ac.uc.clubplatform.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateStepUI()

        binding.btnNext.setOnClickListener {
            when (currentStep) {
                1 -> validateStep1()
                2 -> validateStep2()
                3 -> validateStep3()
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateStep1() {
        val schoolName = binding.etSchoolName.text.toString()
        val schoolEmail = binding.etSchoolEmail.text.toString()
        val department = binding.etDepartment.text.toString()
        val major = binding.etMajor.text.toString()
        val studentId = binding.etStudentId.text.toString()

        if (schoolName.isEmpty() || schoolEmail.isEmpty() ||
            department.isEmpty() || major.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 학교 이메일 인증 처리 (실제 구현은 생략)
        // ...

        currentStep++
        updateStepUI()
    }

    private fun validateStep2() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }

        currentStep++
        updateStepUI()
    }

    private fun validateStep3() {
        val clubName = binding.etClubName.text.toString()

        // 동아리 검색 및 가입 처리 (실제 구현은 생략)
        // ...

        Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateStepUI() {
        // 모든 스텝 UI 숨기기
        binding.layoutStep1.visibility = View.GONE
        binding.layoutStep2.visibility = View.GONE
        binding.layoutStep3.visibility = View.GONE

        // 현재 스텝 활성화
        when (currentStep) {
            1 -> {
                binding.layoutStep1.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.GONE
                binding.btnNext.text = "다음"
            }
            2 -> {
                binding.layoutStep2.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnNext.text = "다음"
            }
            3 -> {
                binding.layoutStep3.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnNext.text = "가입하기"
            }
        }

        // 프로그레스 바 업데이트
        binding.progressStep1.isSelected = currentStep >= 1
        binding.progressStep2.isSelected = currentStep >= 2
        binding.progressStep3.isSelected = currentStep >= 3
    }
}