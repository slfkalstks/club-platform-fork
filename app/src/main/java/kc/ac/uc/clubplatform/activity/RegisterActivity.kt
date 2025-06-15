package kc.ac.uc.clubplatform.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.adapters.DepartmentAdapter
import kc.ac.uc.clubplatform.models.Department
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.api.CareerNetApiClient
import kc.ac.uc.clubplatform.api.RegisterRequest
import kc.ac.uc.clubplatform.api.RegisterResponse
import kc.ac.uc.clubplatform.api.School
import kc.ac.uc.clubplatform.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.Base64
import android.text.TextWatcher
import android.text.Editable

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var selectedSchool: School? = null
    private var profileImageUri: Uri? = null
    private var encodedImage: String? = null
    private var selectedDepartment: Department? = null

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
                val userData = collectUserData()
                performRegistration(userData)
            }
        }

        // 취소 버튼
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // 학과 검색 설정
        binding.etDepartment.setOnClickListener {
            searchDepartment()
        }
        
        // 학과 검색 버튼 설정
        binding.btnSearchDepartment.setOnClickListener {
            searchDepartment()
        }
        
        // 실시간 입력값 변경 시 테두리 색상 초기화
        setupValidationListeners()
    }

    private fun setupValidationListeners() {
        // 이메일 실시간 검증
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s?.toString()?.trim() ?: ""
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.etEmailLayout.error = "유효한 이메일 주소를 입력해주세요"
                    setBoxStrokeError(binding.etEmailLayout)
                } else {
                    binding.etEmailLayout.error = null
                    setBoxStrokeSuccess(binding.etEmailLayout)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 비밀번호 실시간 검증
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                if (password.isEmpty() || password.length < 8) {
                    binding.etPasswordLayout.error = "비밀번호는 최소 8자리 이상이어야 합니다"
                    setBoxStrokeError(binding.etPasswordLayout)
                } else {
                    binding.etPasswordLayout.error = null
                    setBoxStrokeSuccess(binding.etPasswordLayout)
                }
                // 비밀번호 확인도 같이 검증
                validateConfirmPassword()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 비밀번호 확인 실시간 검증
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 학번 실시간 검증
        binding.etStudentId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val studentId = s?.toString()?.trim() ?: ""
                val studentIdPattern = Regex("^\\d{8}$")
                if (studentId.isEmpty() || !studentIdPattern.matches(studentId)) {
                    binding.etStudentIdLayout.error = "학번(8자리 숫자)을 입력해주세요"
                    setBoxStrokeError(binding.etStudentIdLayout)
                } else {
                    binding.etStudentIdLayout.error = null
                    setBoxStrokeSuccess(binding.etStudentIdLayout)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // 비밀번호 확인 실시간 검증 함수
    private fun validateConfirmPassword() {
        val password = binding.etPassword.text?.toString() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
        if (confirmPassword.isEmpty() || confirmPassword != password) {
            binding.etConfirmPasswordLayout.error = "비밀번호가 일치하지 않습니다"
            setBoxStrokeError(binding.etConfirmPasswordLayout)
        } else {
            binding.etConfirmPasswordLayout.error = null
            setBoxStrokeSuccess(binding.etConfirmPasswordLayout)
        }
    }

    private fun resetBoxStroke(layout: com.google.android.material.textfield.TextInputLayout) {
        // outline_default 색상이 없으므로 기본 회색(Color.GRAY) 사용
        layout.boxStrokeColor = ContextCompat.getColor(this, android.R.color.darker_gray)
    }

    private fun setBoxStrokeError(layout: com.google.android.material.textfield.TextInputLayout) {
        layout.boxStrokeColor = ContextCompat.getColor(this, android.R.color.holo_red_dark)
    }

    private fun setBoxStrokeSuccess(layout: com.google.android.material.textfield.TextInputLayout) {
        layout.boxStrokeColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
    }

    private fun searchDepartment() {
        Log.d("RegisterActivity", "searchDepartment called, selectedSchool: $selectedSchool")
        // 전공 검색 다이얼로그를 직접 표시
        showMajorSearchDialog()
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

        val schoolAdapter = kc.ac.uc.clubplatform.adapters.SchoolAdapter { school ->
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
        adapter: kc.ac.uc.clubplatform.adapters.SchoolAdapter
    ) {
        lifecycleScope.launch {
            try {
                val result = CareerNetApiClient.searchUniversity(schoolName)
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
                
                // schoolCode 필드 로깅 추가
                val schoolCode = schoolJson.optString("schoolCode")
                Log.d("RegisterActivity", "School: ${schoolJson.optString("schoolName")}, Code: $schoolCode")
                
                // API 응답에서 schoolCode가 없거나 비어있을 수 있음 - "seq" 필드를 대체로 사용
                val finalSchoolCode = if (schoolCode.isNullOrEmpty()) {
                    val seq = schoolJson.optString("seq")
                    Log.d("RegisterActivity", "Using seq as schoolCode: $seq")
                    seq
                } else {
                    schoolCode
                }

                val school = School(
                    schoolName = schoolJson.optString("schoolName"),
                    schoolType = schoolJson.optString("schoolType"),
                    region = schoolJson.optString("region"),
                    schoolCode = finalSchoolCode,  // 수정된 코드 사용
                    schoolAddr = schoolJson.optString("adres") ?: schoolJson.optString("schoolAddr"),
                    establishmentType = schoolJson.optString("estType")
                )
                
                schools.add(school)
            }
        } catch (e: JSONException) {
            Log.e("RegisterActivity", "Error parsing schools", e)
            e.printStackTrace()
        }

        return schools
    }

    private fun loadDepartmentInfo(schoolCode: String) {
        lifecycleScope.launch {
            try {
                val depInfoResult = CareerNetApiClient.getDepartmentInfo(schoolCode)
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

    private fun showMajorSearchDialog() {
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

        val departmentAdapter = DepartmentAdapter { department ->
            // 학과 선택 시 처리
            Log.d("RegisterActivity", "Selected department: ${department.majorName}, Faculty: ${department.facultyName}")
            selectedDepartment = department
            
            // 학과명 설정
            binding.etMajor.setText(department.majorName)
            
            // 학부 정보가 있는 경우 학부/단과대학 필드에 설정
            if (department.facultyName.isNotEmpty()) {
                binding.etDepartment.setText(department.facultyName)
                
                // 학부 정보를 메시지로 표시
                Toast.makeText(this, "해당 학과의 학부: ${department.facultyName}", Toast.LENGTH_LONG).show()
            }
            
            dialog.dismiss()
        }

        rvDepartmentList.apply {
            layoutManager = LinearLayoutManager(this@RegisterActivity)
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

        // 초기에 모든 학과 목록 로드
        progressBar.visibility = View.VISIBLE
        loadAllMajors(progressBar, departmentAdapter)

        dialog.show()
    }

    private fun searchMajorsByName(
        majorName: String,
        progressBar: ProgressBar,
        adapter: DepartmentAdapter
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                // 여러 페이지의 결과를 가져오기 위한 변수
                var allDepartments = mutableListOf<Department>()
                var currentPage = 1
                val perPage = 100
                var hasMoreResults = true
                
                // 최대 3페이지(300개)까지 결과를 가져오도록 설정
                while (hasMoreResults && currentPage <= 3) {
                    val result = CareerNetApiClient.searchMajorByName(majorName, currentPage, perPage)
                    Log.d("RegisterActivity", "Major search API response received for page $currentPage")
                    
                    val departments = parseDepartmentsFromJsonResponse(result)
                    Log.d("RegisterActivity", "Parsed departments count for page $currentPage: ${departments.size}")
                    
                    if (departments.isEmpty()) {
                        hasMoreResults = false
                    } else {
                        allDepartments.addAll(departments)
                        currentPage++
                    }
                }
                
                // 클라이언트 측에서 검색어 필터링 추가
                val filteredDepartments = allDepartments.filter { 
                    it.majorName.contains(majorName, ignoreCase = true) || 
                    it.facultyName.contains(majorName, ignoreCase = true) 
                }
                
                // 정렬: 검색어와 정확히 일치하는 항목을 먼저 표시
                val sortedDepartments = filteredDepartments.sortedWith(compareBy(
                    // 1. 검색어로 시작하는 학과명을 최우선
                    { !it.majorName.startsWith(majorName, ignoreCase = true) },
                    // 2. 검색어가 포함된 학과명을 다음으로
                    { !it.majorName.contains(majorName, ignoreCase = true) },
                    // 3. 검색어가 포함된 학부명을 그 다음으로
                    { !it.facultyName.contains(majorName, ignoreCase = true) },
                    // 4. 학과명 알파벳 순
                    { it.majorName }
                ))
                
                Log.d("RegisterActivity", "Filtered and sorted departments count: ${sortedDepartments.size}")
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (sortedDepartments.isEmpty()) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "검색 결과가 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // 검색 결과에 컴퓨터 관련 학과가 있는지 로그로 확인
                        val computerDepts = sortedDepartments.filter { it.majorName.contains("컴퓨터") }
                        if (computerDepts.isNotEmpty()) {
                            Log.d("RegisterActivity", "컴퓨터 관련 학과 검색 결과: ${computerDepts.size}개")
                            computerDepts.forEachIndexed { index, dept ->
                                Log.d("RegisterActivity", "$index: ${dept.majorName}, ${dept.facultyName}")
                            }
                        }
                        
                        adapter.updateDepartments(sortedDepartments)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Log.e("RegisterActivity", "Failed to search majors", e)
                    Toast.makeText(
                        this@RegisterActivity,
                        "학과 검색 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadAllMajors(
        progressBar: ProgressBar,
        adapter: DepartmentAdapter
    ) {
        lifecycleScope.launch {
            try {
                val result = CareerNetApiClient.getAllMajors()
                Log.d("RegisterActivity", "All majors API response received")
                
                val departments = parseDepartmentsFromJsonResponse(result)
                Log.d("RegisterActivity", "Parsed departments count: ${departments.size}")

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (departments.isEmpty()) {
                        Toast.makeText(
                            this@RegisterActivity,
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
                    Log.e("RegisterActivity", "Failed to load all majors", e)
                    Toast.makeText(
                        this@RegisterActivity,
                        "학과 정보 로드 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showMajorInputDialog(department: Department) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_major_input)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDepartment = dialog.findViewById<TextView>(R.id.tvDepartment)
        val etMajor = dialog.findViewById<EditText>(R.id.etMajor)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        tvDepartment.text = department.majorName

        btnConfirm.setOnClickListener {
            val major = etMajor.text.toString().trim()
            if (major.isNotEmpty()) {
                binding.etMajor.setText(major)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "전공을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDirectInputDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_department_input)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val etDepartment = dialog.findViewById<EditText>(R.id.etDepartment)
        val etMajor = dialog.findViewById<EditText>(R.id.etMajor)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            val department = etDepartment.text.toString().trim()
            val major = etMajor.text.toString().trim()

            if (department.isEmpty()) {
                Toast.makeText(this, "학과를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (major.isEmpty()) {
                Toast.makeText(this, "전공을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.etDepartment.setText(department)
            binding.etMajor.setText(major)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // 이름 검증
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "이름을 입력해주세요"
            isValid = false
        }

        // 이메일 검증
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailLayout.error = "유효한 이메일 주소를 입력해주세요"
            setBoxStrokeError(binding.etEmailLayout)
            isValid = false
        } else {
            binding.etEmailLayout.error = null
            setBoxStrokeSuccess(binding.etEmailLayout)
        }

        // 비밀번호 검증
        val password = binding.etPassword.text.toString()
        if (password.isEmpty() || password.length < 8) {
            binding.etPasswordLayout.error = "비밀번호는 최소 8자리 이상이어야 합니다"
            setBoxStrokeError(binding.etPasswordLayout)
            isValid = false
        } else {
            binding.etPasswordLayout.error = null
            setBoxStrokeSuccess(binding.etPasswordLayout)
        }

        // 비밀번호 확인 검증
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (confirmPassword != password || confirmPassword.isEmpty()) {
            binding.etConfirmPasswordLayout.error = "비밀번호가 일치하지 않습니다"
            setBoxStrokeError(binding.etConfirmPasswordLayout)
            isValid = false
        } else {
            binding.etConfirmPasswordLayout.error = null
            setBoxStrokeSuccess(binding.etConfirmPasswordLayout)
        }

        // 학교 정보 검증
        if (selectedSchool == null) {
            Toast.makeText(this, "학교를 선택해주세요", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // 전공 검증
        val major = binding.etMajor.text.toString().trim()
        if (major.isEmpty()) {
            binding.etMajor.error = "전공을 입력해주세요"
            isValid = false
        }

        // 학번 검증 (숫자 8자리 예시)
        val studentId = binding.etStudentId.text.toString().trim()
        val studentIdPattern = Regex("^\\d{8}$")
        if (studentId.isEmpty() || !studentIdPattern.matches(studentId)) {
            binding.etStudentIdLayout.error = "학번(8자리 숫자)을 입력해주세요"
            setBoxStrokeError(binding.etStudentIdLayout)
            isValid = false
        } else {
            binding.etStudentIdLayout.error = null
            setBoxStrokeSuccess(binding.etStudentIdLayout)
        }

        return isValid
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

    private fun collectUserData(): RegisterRequest {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val university = binding.etSchoolName.text.toString().trim()
        val major = binding.etMajor.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()

        return RegisterRequest(
            email = email,
            password = password,
            name = name,
            university = university,
            department = major, // 전공 정보를 department 필드에 저장
            major = major,      // 전공 정보를 major 필드에도 저장
            studentId = studentId,
            profileImage = encodedImage
        )
    }

    private fun performRegistration(userData: RegisterRequest) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = ApiClient.apiService.registerUser(userData)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        // 회원가입 성공 처리
                        showToast("회원가입이 완료되었습니다.")
                        navigateToLogin() // 로그인 화면으로 이동
                    } else {
                        showToast(registerResponse?.message ?: "회원가입에 실패했습니다.")
                    }
                } else {
                    showToast("회원가입 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("회원가입 오류: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun parseDepartmentsFromJsonResponse(jsonResponse: JSONObject): List<Department> {
        val departments = mutableListOf<Department>()

        try {
            Log.d("RegisterActivity", "Parsing department response")
            
            if (!jsonResponse.has("dataSearch")) {
                Log.e("RegisterActivity", "JSON response doesn't have dataSearch field")
                Log.d("RegisterActivity", "Response structure: ${jsonResponse.keys().asSequence().toList()}")
                return departments
            }
            
            val dataSearch = jsonResponse.getJSONObject("dataSearch")
            
            if (!dataSearch.has("content")) {
                Log.e("RegisterActivity", "dataSearch doesn't have content field")
                Log.d("RegisterActivity", "dataSearch structure: ${dataSearch.keys().asSequence().toList()}")
                return departments
            }
            
            val content = dataSearch.optJSONArray("content") ?: run {
                Log.e("RegisterActivity", "content is not a JSONArray")
                return departments
            }

            Log.d("RegisterActivity", "Found ${content.length()} items in content array")

            if (content.length() > 0) {
                // 첫 번째 항목의 전체 구조를 로깅
                val firstItem = content.getJSONObject(0)
                Log.d("RegisterActivity", "First item structure: ${firstItem.keys().asSequence().toList()}")
                
                for (i in 0 until content.length()) {
                    try {
                        val depJson = content.getJSONObject(i)
                        
                        // API 응답에서 필드 추출
                        val majorName = depJson.optString("mClass", "")
                        val majorSeq = depJson.optString("majorSeq", "")
                        val lClass = depJson.optString("lClass", "") // 대분류(학부)
                        
                        // 디버그 로그
                        Log.d("RegisterActivity", "Department: $majorName, Faculty: $lClass, Seq: $majorSeq")
                        
                        if (majorName.isNotEmpty()) {
                            departments.add(Department(
                                majorName = majorName,
                                majorSeq = majorSeq,
                                facultyName = lClass
                            ))
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterActivity", "Error parsing department at index $i", e)
                    }
                }
            }
            
            Log.d("RegisterActivity", "Successfully parsed ${departments.size} departments")
        } catch (e: JSONException) {
            Log.e("RegisterActivity", "JSON parsing error", e)
            e.printStackTrace()
        }

        return departments
    }
}