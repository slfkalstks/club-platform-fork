package kc.ac.uc.clubplatform

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kc.ac.uc.clubplatform.databinding.ActivityWritePostBinding
import kc.ac.uc.clubplatform.models.Post
import java.text.SimpleDateFormat
import java.util.*

class WritePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWritePostBinding
    private lateinit var boardType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardType = intent.getStringExtra("board_type") ?: "general"

        binding.btnComplete.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val newPost = Post(
                    id = Random().nextInt(1000), // 임의의 ID 생성
                    title = title,
                    content = content,
                    author = "작성자", // 실제 앱에서는 로그인된 사용자 정보 사용
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    viewCount = 0,
                    commentCount = 0
                )

                val resultIntent = intent
                resultIntent.putExtra("new_post", newPost)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // 제목 또는 내용을 입력하지 않은 경우 처리
//                binding.tvError.text = "제목과 내용을 모두 입력해주세요."
            }
        }

        binding.ivCancel.setOnClickListener {
            finish()
        }
    }
}
