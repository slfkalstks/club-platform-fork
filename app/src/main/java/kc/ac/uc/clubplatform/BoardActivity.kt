// BoardActivity.kt
package kc.ac.uc.clubplatform

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.databinding.ActivityBoardBinding
import kc.ac.uc.clubplatform.adapters.PostAdapter
import kc.ac.uc.clubplatform.models.Post

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private lateinit var boardType: String
    private var postId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 넘어온 인텐트에서 게시판 타입과 게시글 ID를 확인
        boardType = intent.getStringExtra("board_type") ?: "general"
        postId = intent.getIntExtra("post_id", -1).takeIf { it != -1 }

        setupHeader()

        if (postId != null) {
            // 특정 게시글 화면 표시
            showPostDetail(postId!!)
        } else {
            // 게시판 목록 화면 표시
            showPostList()
        }
    }

    private fun setupHeader() {
        // 게시판 이름 설정
        val boardName = when (boardType) {
            "notice" -> "공지게시판"
            "tips" -> "Tips"
            else -> "일반게시판"
        }
        binding.tvBoardName.text = boardName

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 검색 버튼
        binding.ivSearch.setOnClickListener {
            // 게시판 내 검색 기능 구현 (생략)
        }

        // 더보기 버튼 (관리자만 보이도록 설정 가능)
        binding.ivMore.setOnClickListener {
            // 더보기 메뉴 표시 (글쓰기 기능 등)
        }
    }

    private fun showPostList() {
        // 게시글 작성 버튼 표시
        binding.fabWritePost.show()
        binding.fabWritePost.setOnClickListener {
            val intent = Intent(this, WritePostActivity::class.java)
            intent.putExtra("board_type", boardType)
            startActivity(intent)
        }

        // 게시글 목록 표시
        val posts = getDummyPosts()

        val adapter = PostAdapter(posts) { post ->
            // 게시글 클릭 시 해당 게시글 상세 페이지로 이동
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_type", boardType)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
        binding.rvPosts.visibility = android.view.View.VISIBLE
        binding.layoutPostDetail.visibility = android.view.View.GONE
    }

    private fun showPostDetail(postId: Int) {
        // 게시글 작성 버튼 숨기기
        binding.fabWritePost.hide()

        // 게시글 상세 정보 표시
        val post = getDummyPosts().find { it.id == postId } ?: return

        binding.tvPostTitle.text = post.title
        binding.tvPostAuthor.text = post.author
        binding.tvPostDate.text = post.date
        binding.tvPostContent.text = post.content
        binding.tvPostViewCount.text = post.viewCount.toString()
        binding.tvPostCommentCount.text = post.commentCount.toString()

        binding.rvPosts.visibility = android.view.View.GONE
        binding.layoutPostDetail.visibility = android.view.View.VISIBLE

        // 댓글 전송 버튼
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotEmpty()) {
                // 댓글 전송 처리 (생략)
                binding.etComment.text.clear()
            }
        }
    }

    private fun getDummyPosts(): List<Post> {
        return when (boardType) {
            "notice" -> listOf(
                Post(1, "5월 정기 모임 안내", "5월 10일 오후 6시부터 중앙도서관 스터디룸에서 정기 모임이 있습니다. 모든 회원 참석 부탁드립니다.", "관리자", "2025-05-01", 15, 3),
                Post(2, "춘계 MT 참가 신청", "이번 학기 춘계 MT 참가 신청을 받습니다. 5월 24일부터 26일까지 2박 3일 일정입니다.", "관리자", "2025-04-28", 32, 10),
                Post(5, "동아리 회비 납부 안내", "5월 회비 납부 기간은 5월 1일부터 10일까지입니다.", "회계", "2025-04-25", 28, 5)
            )
            "tips" -> listOf(
                Post(3, "새내기를 위한 대학 생활 꿀팁", "1. 수강신청은 미리 준비하세요\n2. 도서관 이용방법을 숙지하세요\n3. 교수님 연구실 위치를 알아두세요", "선배01", "2025-04-25", 45, 8),
                Post(4, "동아리 첫 모임에서 알아두면 좋은 것", "동아리 첫 모임에 가기 전에 동아리 페이지를 미리 둘러보고 가세요.", "회장", "2025-04-20", 38, 12),
                Post(6, "효율적인 시간 관리 방법", "1. 투두리스트 활용하기\n2. 뽀모도로 기법 시도해보기\n3. 일정표 만들기", "시간관리장인", "2025-04-15", 56, 14)
            )
            else -> listOf(
                Post(7, "동아리 스터디 같이하실 분", "알고리즘 스터디 같이 하실 분 구합니다. 주 2회, 2시간씩 진행 예정입니다.", "홍길동", "2025-05-02", 12, 3),
                Post(8, "프로젝트 팀원 모집", "안드로이드 앱 개발 프로젝트 함께 하실 분 모집합니다.", "이몽룡", "2025-05-01", 20, 5),
                Post(9, "지난 행사 사진 공유합니다", "지난 주 워크샵 사진 공유합니다. 즐거운 시간이었네요!", "사진담당", "2025-04-29", 34, 7)
            )
        }
    }
}