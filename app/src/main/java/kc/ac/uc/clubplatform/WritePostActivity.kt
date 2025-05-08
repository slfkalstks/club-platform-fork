// WritePostActivity.kt
package kc.ac.uc.clubplatform

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kc.ac.uc.clubplatform.databinding.ActivityWritePostBinding

class WritePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWritePostBinding
    private lateinit var boardType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardType = intent.getStringExtra("board_type") ?: "general"

        setupHeader()
        setupButtons()
    }

    private fun setupHeader() {
        // 게시판 이름 설정
        val boardName = when (boardType) {
            "notice" -> "공지게시판"
            "tips" -> "Tips"
            else -> "일반게시판"
        }
        binding.tvBoardName.text = "$boardName 글쓰기"

        // 취소 버튼
        binding.ivCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        // 게시글 작성 완료 버튼
        binding.btnComplete.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 글 작성 처리 로직 (생략)

            Toast.makeText(this, "게시글이 등록되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 파일 업로드 버튼
        binding.btnUploadFile.setOnClickListener {
            // 파일 업로드 처리 (생략)
        }

        // 공지 체크박스는 notice 게시판일 때만 표시
        if (boardType == "notice") {
            binding.cbNotice.visibility = android.view.View.VISIBLE
        } else {
            binding.cbNotice.visibility = android.view.View.GONE
        }
    }
}