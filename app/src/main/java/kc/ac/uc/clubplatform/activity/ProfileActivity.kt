// ProfileActivity.kt
package kc.ac.uc.clubplatform.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kc.ac.uc.clubplatform.R

import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.api.CareerNetApiClient
import kc.ac.uc.clubplatform.api.ChangePasswordRequest
import kc.ac.uc.clubplatform.api.UpdateDepartmentRequest
import kc.ac.uc.clubplatform.api.UpdateProfileImageBase64Request
import kc.ac.uc.clubplatform.api.WithdrawRequest
import kc.ac.uc.clubplatform.databinding.ActivityProfileBinding
import kc.ac.uc.clubplatform.models.Department
import kc.ac.uc.clubplatform.util.AuthenticatedUrlLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.ByteArrayOutputStream

// 정보갱신 추가 필요
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var encodedImage: String? = null
    private var schoolCode: String? = null
    private var selectedDepartment: Department? = null

    // 갤러리에서 이미지 선택 결과를 처리하는 런처를 회원가입과 동일한 방식으로 변경
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                // 이미지를 화면에 표시
                binding.ivProfileImage.setImageURI(uri)
                
                // 비트맵으로 변환하고 인코딩
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val resizedBitmap = resizeBitmap(bitmap, 500)
                encodeImageToBase64(resizedBitmap)
                
                // 서버로 업로드
                encodedImage?.let { base64Image ->
                    uploadProfileImageBase64(base64Image)
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "이미지 처리 오류", e)
                showToast("이미지 처리 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupProfileInfo()
        setupMenuItems()
    }

    override fun onResume() {
        super.onResume()
        setupProfileInfo() // 항상 최신 정보로 갱신
    }

    private fun setupHeader() {
        // 뒤로가기 버튼 설정
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupProfileInfo() {
        // SharedPreferences에서 사용자 정보 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", null)
        val schoolName = sharedPreferences.getString("school_name", null)
        val major = sharedPreferences.getString("major", null)
        val studentId = sharedPreferences.getString("student_id", null)
        val profileImageUrl = sharedPreferences.getString("profile_image_url", null)
        val userId = sharedPreferences.getString("user_id", null)
        
        // 학교 코드 저장 (학과 설정에 사용)
        schoolCode = sharedPreferences.getString("school_code", null)
        
        // 이메일 값을 가져와서 로그에 출력
        val email = sharedPreferences.getString("email", null)
        Log.d("ProfileActivity", "User email from SharedPreferences: $email")
        Log.d("ProfileActivity", "User ID from SharedPreferences: $userId")
        
        binding.tvUserName.text = userName ?: "NULL"
        binding.tvSchoolName.text = schoolName ?: "NULL"
        binding.tvMajor.text = major ?: "NULL"
        binding.tvStudentId.text = studentId ?: "NULL"
        
        // 아이디 칸에 이메일 표시 (이메일이 없으면 userId 사용, 둘 다 없으면 "NULL" 표시)
        binding.tvUserId.text = when {
            !email.isNullOrEmpty() -> email
            !userId.isNullOrEmpty() -> "사용자 #$userId"
            else -> "NULL"
        }
        
        // 프로필 사진 표시 개선
        loadProfileImage(profileImageUrl)

        // 프로필 관리 버튼 클릭 이벤트
        binding.btnManageProfile.setOnClickListener {
            showProfileManageDialog()
        }
    }

    /**
     * 프로필 이미지를 안전하게 로드하는 메서드
     */
    private fun loadProfileImage(profileImageUrl: String?) {
        // 사용자 ID 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        
        if (userId != null) {
            Log.d("ProfileActivity", "Loading profile image for user: $userId")
            
            try {
                // ProfileImageUrlUtil을 사용하여 URL 생성
                val imageUrl = ApiClient.getProfileImageUrl(userId)
                Log.d("ProfileActivity", "Profile image URL: $imageUrl")
                
                // 인증 헤더가 포함된 URL 생성
                val authenticatedUrl = AuthenticatedUrlLoader(this, imageUrl).getGlideUrl()
                
                // Glide를 사용하여 이미지 로드 - 메모리 및 디스크 캐시 설정 추가
                Glide.with(applicationContext)
                    .load(authenticatedUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .skipMemoryCache(false) // 메모리 캐시 사용
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐시 전략
                    .override(150, 150) // 이미지 크기 최적화
                    .dontAnimate() // 애니메이션 비활성화로 성능 향상
                    .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<android.graphics.drawable.Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("ProfileActivity", "Failed to load profile image from: $model", e)
                            if (e != null) {
                                for (t in e.rootCauses) {
                                    Log.e("ProfileImageLoad", "Root cause: ", t)
                                }
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable?,
                            model: Any?,
                            target: Target<android.graphics.drawable.Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("ProfileActivity", "Profile image loaded successfully")
                            Log.d("ProfileActivity", "Image source: ${dataSource?.name}") // 캐시 여부 확인
                            return false
                        }
                    })
                    .into(binding.ivProfileImage)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Exception during image loading setup", e)
                try {
                    binding.ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
                } catch (e2: Exception) {
                    Log.e("ProfileActivity", "Failed to set placeholder image", e2)
                }
            }
        } else {
            Log.d("ProfileActivity", "No user ID found, using default image")
            try {
                binding.ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Failed to set placeholder image", e)
            }
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
            performLogout()
        }
    }

    private fun showProfileManageDialog() {
        val options = arrayOf("학과 설정", "프로필 사진 변경")
        AlertDialog.Builder(this)
            .setTitle("프로필 관리")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDepartmentSettingDialog() // 학과 설정
                    1 -> showProfileImageOptions()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showProfileImageOptions() {
        val options = arrayOf("프로필 사진 변경", "프로필 사진 삭제")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // 갤러리에서 이미지 선택 (회원가입과 동일한 방식으로 변경)
                        getContent.launch("image/*")
                    }
                    1 -> {
                        // 프로필 이미지 삭제 (추후 구현)
                        Toast.makeText(this, "프로필 사진이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 회원가입과 동일한 학과 검색 다이얼로그 활용
    private fun showDepartmentSettingDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_department_search)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        val etSearchDepartment = dialog.findViewById<EditText>(R.id.etSearchDepartment)
        val btnSearchDepartment = dialog.findViewById<Button>(R.id.btnSearchDepartment)
        val rvDepartmentList = dialog.findViewById<RecyclerView>(R.id.rvDepartmentList)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvSchoolName)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = "학과 검색"

        val departmentAdapter = kc.ac.uc.clubplatform.adapters.DepartmentAdapter { department ->
            // 학과 선택 시 처리
            selectedDepartment = department
            binding.tvMajor.text = department.majorName

            // 서버에 학과 정보 변경 요청
            updateDepartmentOnServer(department)

            // SharedPreferences에 저장
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("major", department.majorName)
                .putString("department", department.facultyName)
                .apply()

            Toast.makeText(this, "학과가 변경되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        rvDepartmentList.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = departmentAdapter
            setHasFixedSize(true)
        }

        btnSearchDepartment.setOnClickListener {
            val majorName = etSearchDepartment.text.toString().trim()
            if (majorName.isEmpty()) {
                Toast.makeText(this, "검색할 학과명을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            searchMajorsByName(majorName, progressBar, departmentAdapter)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 초기에 전체 학과 목록 로드
        progressBar.visibility = View.VISIBLE
        loadAllMajors(progressBar, departmentAdapter)

        dialog.show()
    }

    // 서버에 학과 정보 변경 요청
    private fun updateDepartmentOnServer(department: Department) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null) ?: return

        val request = kc.ac.uc.clubplatform.api.UpdateDepartmentRequest(
            userId = userId,
            department = department.facultyName,
            major = department.majorName
        )

        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = kc.ac.uc.clubplatform.api.ApiClient.apiService.updateDepartment(request)
                val body = response.body()
                // 응답이 Map 또는 JSONObject일 수 있으므로 안전하게 처리
                val isSuccess = when (body) {
                    is Map<*, *> -> body["success"] == true
                    is org.json.JSONObject -> body.optBoolean("success", false)
                    else -> false
                }
                val message = when (body) {
                    is Map<*, *> -> body["message"]?.toString() ?: ""
                    is org.json.JSONObject -> body.optString("message", "")
                    else -> ""
                }
                if (response.isSuccessful && isSuccess) {
                    showToast("학과 정보가 서버에 성공적으로 변경되었습니다.")
                } else {
                    showToast("서버에 학과 정보 변경 실패: $message")
                }
            } catch (e: Exception) {
                showToast("서버 통신 오류: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    // 회원가입과 동일한 방식의 학과 검색 함수
    private fun searchMajorsByName(
        majorName: String,
        progressBar: ProgressBar,
        adapter: kc.ac.uc.clubplatform.adapters.DepartmentAdapter
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                var allDepartments = mutableListOf<Department>()
                var currentPage = 1
                val perPage = 100
                var hasMoreResults = true
                while (hasMoreResults && currentPage <= 3) {
                    val result = kc.ac.uc.clubplatform.api.CareerNetApiClient.searchMajorByName(majorName, currentPage, perPage)
                    val departments = parseDepartmentsFromJsonResponse(result)
                    if (departments.isEmpty()) {
                        hasMoreResults = false
                    } else {
                        allDepartments.addAll(departments)
                        currentPage++
                    }
                }
                val filteredDepartments = allDepartments.filter {
                    it.majorName.contains(majorName, ignoreCase = true) ||
                    it.facultyName.contains(majorName, ignoreCase = true)
                }
                val sortedDepartments = filteredDepartments.sortedWith(compareBy(
                    { !it.majorName.startsWith(majorName, ignoreCase = true) },
                    { !it.majorName.contains(majorName, ignoreCase = true) },
                    { !it.facultyName.contains(majorName, ignoreCase = true) },
                    { it.majorName }
                ))
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (sortedDepartments.isEmpty()) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "검색 결과가 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        adapter.updateDepartments(sortedDepartments)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ProfileActivity,
                        "학과 검색 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // 회원가입과 동일한 방식의 전체 학과 목록 로드 함수
    private fun loadAllMajors(
        progressBar: ProgressBar,
        adapter: kc.ac.uc.clubplatform.adapters.DepartmentAdapter
    ) {
        lifecycleScope.launch {
            try {
                val result = kc.ac.uc.clubplatform.api.CareerNetApiClient.getAllMajors()
                val departments = parseDepartmentsFromJsonResponse(result)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (departments.isEmpty()) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "학과 정보를 불러올 수 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        adapter.updateDepartments(departments)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ProfileActivity,
                        "학과 정보 로드 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // 회원가입과 동일한 방식의 학과 파싱 함수
    private fun parseDepartmentsFromJsonResponse(jsonResponse: org.json.JSONObject): List<Department> {
        val departments = mutableListOf<Department>()
        try {
            if (!jsonResponse.has("dataSearch")) return departments
            val dataSearch = jsonResponse.getJSONObject("dataSearch")
            if (!dataSearch.has("content")) return departments
            val content = dataSearch.optJSONArray("content") ?: return departments
            for (i in 0 until content.length()) {
                val depJson = content.getJSONObject(i)
                val majorName = depJson.optString("mClass", "")
                val majorSeq = depJson.optString("majorSeq", "")
                val lClass = depJson.optString("lClass", "")
                if (majorName.isNotEmpty()) {
                    departments.add(
                        Department(
                            majorName = majorName,
                            majorSeq = majorSeq,
                            facultyName = lClass
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return departments
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * 비트맵을 Base64로 인코딩 (회원가입과 동일한 방식)
     */
    private fun encodeImageToBase64(bitmap: Bitmap) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d("ProfileActivity", "Image encoded, length: ${encodedImage?.length ?: 0}")
        } catch (e: Exception) {
            Log.e("ProfileActivity", "인코딩 오류", e)
            showToast("이미지 변환 중 오류가 발생했습니다")
            encodedImage = null
        }
    }
    
    /**
     * Base64 이미지를 서버로 업로드
     */
    private fun uploadProfileImageBase64(base64Image: String) {
        // SharedPreferences에서 userId 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        
        if (userId == null) {
            showToast("사용자 정보를 찾을 수 없습니다")
            return
        }
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // 요청 로깅 추가 (디버깅 목적)
                Log.d("ProfileActivity", "Uploading profile image for user: $userId")
                Log.d("ProfileActivity", "Base64 image length: ${base64Image.length}")
                
                // API 요청 데이터 생성
                val request = UpdateProfileImageBase64Request(userId, base64Image)
                
                // API 호출
                val response = ApiClient.apiService.updateProfileImageBase64(request)
                
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    showToast("프로필 사진이 성공적으로 변경되었습니다")
                    
                    // 프로필 이미지 리로드 (Glide 캐시 초기화)
                    Glide.get(applicationContext).clearMemory() // 메인 스레드에서 메모리 캐시 삭제
                    lifecycleScope.launch(Dispatchers.IO) {
                        Glide.get(applicationContext).clearDiskCache() // 디스크 캐시 삭제 (백그라운드 스레드에서 실행)
                    }
                    
                    // 약간의 지연 후 이미지 리로드
                    delay(300)
                    withContext(Dispatchers.Main) {
                        loadProfileImage(null)
                    }
                } else {
                    // 오류 응답 상세 로깅
                    val errorMessage = response.body()?.get("message")?.toString() ?: 
                                      "프로필 사진 변경에 실패했습니다 (${response.code()})"
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileActivity", "Profile image update failed: $errorMessage")
                    Log.e("ProfileActivity", "Error response body: $errorBody")
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Profile image update exception", e)
                showToast("프로필 사진 변경 중 오류가 발생했습니다: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showPasswordChangeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        
        // 다이얼로그 내부의 뷰 요소 가져오기
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("비밀번호 변경")
            .setView(dialogView)
            .setPositiveButton("변경", null) // 버튼만 정의하고 리스너는 나중에 설정
            .setNegativeButton("취소", null)
            .create()
        
        dialog.show()
        
        // 직접 버튼을 가져와서 커스텀 리스너 설정 (다이얼로그 자동 종료 방지)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // 입력값 가져오기
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            // 입력값 검증
            when {
                currentPassword.isEmpty() -> {
                    etCurrentPassword.error = "현재 비밀번호를 입력해주세요"
                }
                newPassword.isEmpty() -> {
                    etNewPassword.error = "새 비밀번호를 입력해주세요"
                }
                newPassword.length < 8 -> {
                    etNewPassword.error = "비밀번호는 8자 이상이어야 합니다"
                }
                confirmPassword.isEmpty() -> {
                    etConfirmPassword.error = "비밀번호 확인을 입력해주세요"
                }
                newPassword != confirmPassword -> {
                    etConfirmPassword.error = "비밀번호가 일치하지 않습니다"
                }
                else -> {
                    // 모든 검증 통과 시 비밀번호 변경 처리
                    changePassword(currentPassword, newPassword, confirmPassword, dialog)
                }
            }
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String, dialog: AlertDialog) {
        // SharedPreferences에서 userId 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        
        if (userId == null) {
            showToast("사용자 정보를 찾을 수 없습니다")
            return
        }
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val request = ChangePasswordRequest(userId, currentPassword, newPassword, confirmPassword)
                val response = ApiClient.apiService.changePassword(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("비밀번호가 성공적으로 변경되었습니다")
                    dialog.dismiss() // 성공 시 다이얼로그 닫기
                } else {
                    // 실패 시 오류 메시지 표시
                    val errorMessage = response.body()?.message ?: 
                                      "비밀번호 변경에 실패했습니다 (${response.code()})"
                    showToast(errorMessage)
                    
                    Log.e("ProfileActivity", "Password change failed: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Password change exception", e)
                showToast("비밀번호 변경 중 오류가 발생했습니다: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showWithdrawDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_withdraw, null)
        
        // 다이얼로그 내부의 비밀번호 입력 필드 가져오기
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.")
            .setView(dialogView)
            .setPositiveButton("탈퇴", null) // 버튼만 정의하고 리스너는 나중에 설정
            .setNegativeButton("취소", null)
            .create()
        
        dialog.show()
        
        // 직접 버튼을 가져와서 커스텀 리스너 설정 (다이얼로그 자동 종료 방지)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val password = etPassword.text.toString().trim()
            
            if (password.isEmpty()) {
                etPassword.error = "비밀번호를 입력해주세요"
            } else {
                // 비밀번호 입력 시 회원탈퇴 진행
                withdrawAccount(password, dialog)
            }
        }
    }

    private fun withdrawAccount(password: String, dialog: AlertDialog) {
        // SharedPreferences에서 userId 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        
        if (userId == null) {
            showToast("사용자 정보를 찾을 수 없습니다")
            return
        }
        
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val request = WithdrawRequest(userId, password)
                val response = ApiClient.apiService.withdrawAccount(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("회원 탈퇴가 완료되었습니다")
                    dialog.dismiss() // 성공 시 다이얼로그 닫기
                    
                    // 로컬 데이터 삭제 및 로그인 화면으로 이동
                    clearUserDataAndNavigateToLogin()
                } else {
                    // 실패 시 오류 메시지 표시
                    val errorMessage = response.body()?.message ?: 
                                      "회원 탈퇴에 실패했습니다 (${response.code()})"
                    showToast(errorMessage)
                    
                    Log.e("ProfileActivity", "Account withdrawal failed: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Account withdrawal exception", e)
                showToast("회원 탈퇴 중 오류가 발생했습니다: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun performLogout() {
        // 로그아웃 버튼 클릭 시 확인 다이얼로그 표시
        showLogoutDialog()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                executeLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun executeLogout() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // refreshToken 사용하지 않음 - 백엔드 API가 본문을 기대하지 않음
                Log.d("ProfileActivity", "Logging out...")
                
                // 백엔드 API와 일치하도록 파라미터 없이 호출
                val response = ApiClient.apiService.logout()

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("ProfileActivity", "Logout successful")
                    clearUserDataAndNavigateToLogin()
                } else {
                    // 오류 응답 상세 로깅 개선
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    val errorCode = response.code()
                    Log.e("ProfileActivity", "Logout failed: HTTP $errorCode, Error: $errorBody")
                    
                    showToast("로그아웃에 실패했습니다: ${response.body()?.message ?: "서버 오류 ($errorCode)"}")
                    // 서버 응답이 실패해도 로컬에서는 로그아웃 처리
                    clearUserDataAndNavigateToLogin()
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Logout exception", e)
                showToast("로그아웃 중 오류가 발생했습니다: ${e.message}")
                // 예외 발생해도 로컬에서는 로그아웃 처리
                clearUserDataAndNavigateToLogin()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 사용자 데이터를 지우고 로그인 화면으로 이동하는 통합 함수
     */
    private fun clearUserDataAndNavigateToLogin() {
        // 올바른 SharedPreferences 키 사용하여 모든 데이터 삭제
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        
        Log.d("ProfileActivity", "User data cleared, navigating to login")
        
        // 로그인 화면으로 이동
        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        showToast("로그아웃 되었습니다")
    }

    private fun showLoading(isLoading: Boolean) {
        // 로딩 UI 표시 로직
        binding.root.alpha = if (isLoading) 0.5f else 1.0f
        binding.root.isEnabled = !isLoading
        // 로딩 표시자가 있다면 표시/숨김 처리
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
