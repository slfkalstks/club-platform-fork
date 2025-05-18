package kc.ac.uc.clubplatform

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.adapters.SchoolAdapter
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.api.CareerNetApiClient
import kc.ac.uc.clubplatform.api.RegisterRequest
import kc.ac.uc.clubplatform.api.RegisterResponse
import kc.ac.uc.clubplatform.api.School
import kc.ac.uc.clubplatform.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.Base64

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var selectedSchool: School? = null
    private lateinit var careerNetClient: CareerNetApiClient
    private var profileImageUri: Uri? = null
    private var encodedImage: String? = null

    // 갤러리에서 이미지 선택 결과 처리
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            binding.ivProfileImage.setImageURI(profileImageUri)
            encodeImage(profileImageUri!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        careerNetClient = CareerNetApiClient(ApiClient.API_KEY)
        
        setupUI()
    }

    private fun setupUI() {
        // 학교 검색 설정
        binding.etSchoolName.setOnClickListener {
            showSchoolSearchDialog()
        }
        
        binding.btnSearchSchool.setOnClickListener {
            showSchoolSearchDialog()
        }
        
        // 프로필 이미지 선택
        binding.btnSelectImage.setOnClickListener {
            getContent.launch("image/*")
        }
        
        // 가입 버튼
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }
        
        // 취소 버튼
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showSchoolSearchDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_school_search)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        val etSearchSchool = dialog.findViewById<EditText>(R.id.etSearchSchool)
        val btnSearchSchool = dialog.findViewById<Button>(R.id.btnSearchSchool)
        val rvSchoolList = dialog.findViewById<RecyclerView>(R.id.rvSchoolList)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)

        val schoolAdapter = SchoolAdapter { school ->
            // 학교 선택 시 처리
            selectedSchool = school
            binding.etSchoolName.setText(school.schoolName)
            
            // 학교 코드가 있으면 학과 정보 로드
            school.schoolCode?.let { code ->
                loadDepartmentInfo(code)
            }
            
            dialog.dismiss()
        }

        rvSchoolList.apply {
            layoutManager = LinearLayoutManager(this@RegisterActivity)
            adapter = schoolAdapter
        }

        btnSearchSchool.setOnClickListener {
            val schoolName = etSearchSchool.text.toString()
            if (schoolName.isEmpty()) {
                Toast.makeText(this, "학교명을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            progressBar.visibility = View.VISIBLE
            searchUniversities(schoolName, progressBar, schoolAdapter)
        }

        dialog.show()
    }

    private fun searchUniversities(
        schoolName: String,
        progressBar: ProgressBar,
        adapter: SchoolAdapter
    ) {
        lifecycleScope.launch {
            try {
                val result = careerNetClient.searchUniversity(schoolName)
                val schools = parseSchoolsFromJsonResponse(result)
                
                progressBar.visibility = View.GONE
                
                if (schools.isEmpty()) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "검색 결과가 없습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    adapter.updateSchools(schools)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@RegisterActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun parseSchoolsFromJsonResponse(jsonResponse: JSONObject): List<School> {
        val schools = mutableListOf<School>()
        
        try {
            val dataSearch = jsonResponse.getJSONObject("dataSearch")
            val content = dataSearch.getJSONArray("content")
            
            for (i in 0 until content.length()) {
                val schoolJson = content.getJSONObject(i)
                
                val school = School(
                    schoolName = schoolJson.optString("schoolName"),
                    schoolType = schoolJson.optString("schoolType"),
                    region = schoolJson.optString("region"),
                    schoolCode = schoolJson.optString("schoolCode"),
                    schoolAddr = schoolJson.optString("adres") ?: schoolJson.optString("schoolAddr"),
                    establishmentType = schoolJson.optString("estType")
                )
                
                schools.add(school)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        
        return schools
    }
    
    private fun loadDepartmentInfo(schoolCode: String) {
        lifecycleScope.launch {
            try {
                val depInfoResult = careerNetClient.getDepartmentInfo(schoolCode)
                val departments = parseDepartmentsFromJsonResponse(depInfoResult)
                
                // 첫 번째 학과로 학과 필드 자동 채우기 (있는 경우)
                if (departments.isNotEmpty()) {
                    binding.etDepartment.setText(departments[0].majorName)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "학과 정보 로드 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private data class Department(
        val majorName: String,
        val majorSeq: String
    )
    
    private fun parseDepartmentsFromJsonResponse(jsonResponse: JSONObject): List<Department> {
        val departments = mutableListOf<Department>()
        
        try {
            val dataSearch = jsonResponse.getJSONObject("dataSearch")
            val content = dataSearch.getJSONArray("content")
            
            for (i in 0 until content.length()) {
                val depJson = content.getJSONObject(i)
                
                val department = Department(
                    majorName = depJson.optString("majorName"),
                    majorSeq = depJson.optString("majorSeq")
                )
                
                departments.add(department)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        
        return departments
    }

    private fun validateForm(): Boolean {
        // 이름 검증
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "이름을 입력해주세요"
            return false
        }
        
        // 이메일 검증
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "이메일을 입력해주세요"
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "유효한 이메일 주소를 입력해주세요"
            return false
        }
        
        // 비밀번호 검증
        val password = binding.etPassword.text.toString()
        if (password.isEmpty()) {
            binding.etPassword.error = "비밀번호를 입력해주세요"
            return false
        }
        
        if (password.length < 8) {
            binding.etPassword.error = "비밀번호는 최소 8자리 이상이어야 합니다"
            return false
        }
        
        // 비밀번호 확인 검증
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (confirmPassword != password) {
            binding.etConfirmPassword.error = "비밀번호가 일치하지 않습니다"
            return false
        }
        
        // 학교 정보 검증
        if (selectedSchool == null) {
            Toast.makeText(this, "학교를 선택해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // 학과 검증
        val department = binding.etDepartment.text.toString().trim()
        if (department.isEmpty()) {
            binding.etDepartment.error = "학과를 입력해주세요"
            return false
        }
        
        // 전공 검증
        val major = binding.etMajor.text.toString().trim()
        if (major.isEmpty()) {
            binding.etMajor.error = "전공을 입력해주세요"
            return false
        }
        
        // 학번 검증
        val studentId = binding.etStudentId.text.toString().trim()
        if (studentId.isEmpty()) {
            binding.etStudentId.error = "학번을 입력해주세요"
            return false
        }
        
        return true
    }
    
    private fun encodeImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val resizedBitmap = resizeBitmap(bitmap, 500)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            encodedImage = Base64.getEncoder().encodeToString(byteArray)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
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
    
    private fun registerUser() {
        // 입력값 가져오기
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val university = binding.etSchoolName.text.toString().trim()
        val department = binding.etDepartment.text.toString().trim()
        val major = binding.etMajor.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        
        // 로딩 표시
        showLoading(true)
        
        // API 요청 객체 생성
        val registerRequest = RegisterRequest(
            email = email,
            password = password,
            name = name,
            university = university,
            department = department,
            major = major,
            studentId = studentId,
            profileImage = encodedImage
        )
        
        // 서버에 회원가입 요청
        ApiClient.userService.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                showLoading(false)
                
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        Toast.makeText(this@RegisterActivity, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
                        
                        // 로그인 화면으로 이동
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, 
                            registerResponse?.message ?: "회원가입에 실패했습니다", 
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 에러 응답 처리
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = JSONObject(errorBody ?: "{}")
                        val errorMessage = errorJson.optString("message", "회원가입에 실패했습니다")
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, 
                            "회원가입에 실패했습니다: ${response.code()}", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@RegisterActivity, 
                    "네트워크 오류: ${t.message}", 
                    Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
        }
    }
    
    data class UserRegistrationData(
        val email: String,
        val password: String,
        val name: String,
        val university: String,
        val department: String,
        val major: String,
        val studentId: String,
        val profileImage: String?
    )
}
