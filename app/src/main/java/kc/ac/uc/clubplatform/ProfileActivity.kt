// ProfileActivity.kt
package kc.ac.uc.clubplatform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kc.ac.uc.clubplatform.databinding.ActivityProfileBinding
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    // ProfileActivity 클래스 내부에 추가
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY_IMAGE = 2
    private val REQUEST_PERMISSION = 100
    private var currentPhotoPath: String = ""

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
        // 학과 선택 다이얼로그 표시
        val departments = arrayOf(
            "컴퓨터공학과", "소프트웨어학과", "정보통신공학과", "전자공학과",
            "기계공학과", "건축공학과", "산업공학과", "화학공학과",
            "경영학과", "경제학과", "국어국문학과", "영어영문학과",
            "법학과", "행정학과", "심리학과", "사회학과"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("학과 선택")

        builder.setItems(departments) { _, which ->
            val selectedDepartment = departments[which]
            binding.tvMajor.text = selectedDepartment
            Toast.makeText(this, "학과가 변경되었습니다", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }


    private fun showProfileImageOptions() {
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택", "프로필 사진 삭제")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> {
                        // 프로필 이미지 삭제
                        binding.ivProfileImage.setImageResource(R.drawable.ic_profile)
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

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION
            )
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 이미지 파일 이름 생성
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "kc.ac.uc.clubplatform.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // onActivityResult 메소드 추가 또는 수정
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // 카메라로 찍은 사진 처리
                    val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    binding.ivProfileImage.setImageBitmap(bitmap)
                }
                REQUEST_GALLERY_IMAGE -> {
                    // 갤러리에서 선택한 사진 처리
                    val selectedImage = data?.data
                    binding.ivProfileImage.setImageURI(selectedImage)
                }
            }
        }
    }
}