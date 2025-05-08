// BoardActivity.kt
package kc.ac.uc.clubplatform

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import kc.ac.uc.clubplatform.databinding.ActivityBoardBinding
import kc.ac.uc.clubplatform.adapters.PostAdapter
import kc.ac.uc.clubplatform.adapters.CommentAdapter
import kc.ac.uc.clubplatform.models.Post

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private lateinit var boardType: String
    private var postId: Int? = null
    private val comments = mutableListOf<String>() // 댓글 리스트
    private lateinit var commentsAdapter: CommentAdapter // 댓글 어댑터
    private val posts = mutableListOf<Post>() // 게시글 리스트
    private lateinit var postAdapter: PostAdapter // 게시글 어댑터

    private val writePostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newPost = result.data?.getParcelableExtra<Post>("new_post")
            if (newPost != null) {
                posts.add(0, newPost) // 새 게시글을 리스트 맨 앞에 추가
                postAdapter.notifyItemInserted(0)
                binding.rvPosts.scrollToPosition(0)
            }
        }
    }

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
            writePostLauncher.launch(intent) // 게시글 작성 화면으로 이동
        }

        // 게시글 목록 표시
        posts.clear()
        posts.addAll(getDummyPosts())

        postAdapter = PostAdapter(posts) { post ->
            // 게시글 클릭 시 해당 게시글 상세 페이지로 이동
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_type", boardType)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
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

        // 댓글 RecyclerView 설정
        commentsAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentsAdapter

        // 댓글 전송 버튼
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotEmpty()) {
                // 댓글 리스트에 추가
                comments.add(commentText)

                // RecyclerView 업데이트
                commentsAdapter.notifyItemInserted(comments.size - 1)
                binding.rvComments.scrollToPosition(comments.size - 1)

                // 입력 필드 초기화
                binding.etComment.setText("")
            } else {
                Toast.makeText(this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
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
                Post(7, "동아리 스터디 같이하실 분", "알고리즘 스터디 같이 하실 분 구합니다. 매주 화요일 오후 7시에 진행 예정입니다.", "스터디장", "2025-05-01", 20, 4),
                Post(8, "자유게시판 테스트", "이 게시글은 자유게시판 테스트용입니다.", "테스터", "2025-05-02", 10, 2)
            )
        }
    }
}
