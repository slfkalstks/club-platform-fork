package kc.ac.uc.clubplatform.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kc.ac.uc.clubplatform.databinding.ActivityWritePostBinding
import kc.ac.uc.clubplatform.api.ApiClient
import kotlinx.coroutines.launch
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import android.util.Log
import kc.ac.uc.clubplatform.models.CreatePostRequest

class WritePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWritePostBinding
    private lateinit var boardType: String
    private var boardId: Int = -1
    private var clubId: Int = -1
    private lateinit var markwon: Markwon
    private lateinit var editor: MarkwonEditor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 정보 가져오기
        boardType = intent.getStringExtra("board_type") ?: "general"
        boardId = intent.getIntExtra("board_id", -1)
        clubId = intent.getIntExtra("club_id", -1)

        // 현재 동아리 ID 가져오기
        if (clubId == -1) {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            clubId = sharedPreferences.getInt("current_club_id", -1)
        }

        setupMarkdown()
        setupUI()
        setupListeners()
    }

    private fun setupMarkdown() {
        try {
            // 마크다운 초기화
            markwon = Markwon.create(this)
            editor = MarkwonEditor.create(markwon)

            // 마크다운 에디터 적용
            binding.etContent.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))
        } catch (e: Exception) {
            Log.e("WritePostActivity", "Markwon initialization failed", e)
            // 마크다운 초기화 실패 시에도 기본 기능은 동작하도록 함
        }
    }

    private fun setupUI() {
        // 게시판 타입에 따른 UI 설정
        when (boardType) {
            "notice" -> {
                binding.tvBoardName.text = "공지게시판 - 글쓰기"
                binding.cbNotice.visibility = View.VISIBLE
            }
            "tips" -> {
                binding.tvBoardName.text = "Tips - 글쓰기"
                binding.cbNotice.visibility = View.GONE
            }
            else -> {
                binding.tvBoardName.text = "일반게시판 - 글쓰기"
                binding.cbNotice.visibility = View.GONE
            }
        }

        // 마크다운 도움말 추가
        binding.etContent.hint = """내용을 입력하세요."""
    }

    private fun setupListeners() {
        // 완료 버튼 클릭 리스너
        binding.btnComplete.setOnClickListener {
            if (validateInput()) {
                createPost()
            }
        }

        // 취소 버튼 클릭 리스너
        binding.ivCancel.setOnClickListener {
            finish()
        }

        // 파일 업로드 버튼 (추후 구현)
        binding.btnUploadFile.setOnClickListener {
            Toast.makeText(this, "파일 업로드 기능은 추후 구현 예정입니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "제목을 입력해주세요"
            binding.etTitle.requestFocus()
            return false
        }

        if (content.isEmpty()) {
            binding.etContent.error = "내용을 입력해주세요"
            binding.etContent.requestFocus()
            return false
        }

        if (boardId == -1) {
            Toast.makeText(this, "게시판 정보가 없습니다", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val isAnonymous = binding.cbAnonymous?.isChecked ?: false
        val isNotice = binding.cbNotice.isChecked

        // 공지글 권한 확인 (실제로는 서버에서 권한 확인)
        if (isNotice && boardType != "notice") {
            Toast.makeText(this, "이 게시판에서는 공지글을 작성할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreatePostRequest(
            boardId = boardId,
            title = title,
            content = content,
            isAnonymous = isAnonymous,
            isNotice = isNotice,
            attachments = null // 추후 파일 업로드 구현
        )

        lifecycleScope.launch {
            try {
                showLoading(true)

                val response = ApiClient.apiService.createPost(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@WritePostActivity, "게시글이 작성되었습니다", Toast.LENGTH_SHORT).show()

                    // 결과를 반환하고 액티비티 종료
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "게시글 작성에 실패했습니다"
                    Toast.makeText(this@WritePostActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WritePostActivity", "Error creating post", e)
                Toast.makeText(this@WritePostActivity, "게시글 작성 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnComplete.isEnabled = false
            binding.btnComplete.text = "작성 중..."
        } else {
            binding.btnComplete.isEnabled = true
            binding.btnComplete.text = "완료"
        }
    }
}