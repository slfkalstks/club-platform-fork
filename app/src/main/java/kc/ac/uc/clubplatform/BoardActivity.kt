// BoardActivity.kt
package kc.ac.uc.clubplatform

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import kc.ac.uc.clubplatform.databinding.ActivityBoardBinding
import kc.ac.uc.clubplatform.adapters.PostAdapter
import kc.ac.uc.clubplatform.adapters.CommentAdapter
import kc.ac.uc.clubplatform.models.Post
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            "my_posts" -> "내가 쓴 글"
            "my_comments" -> "댓글 단 글"
            "my_scraps" -> "스크랩"
            "hot" -> "HOT 게시판"
            "best" -> "BEST 게시판"
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
        // 게시글 작성 버튼 표시 (특정 게시판에서만 표시)
        if (boardType in listOf("general", "notice", "tips")) {
            binding.fabWritePost.show()
            binding.fabWritePost.setOnClickListener {
                val intent = Intent(this, WritePostActivity::class.java)
                intent.putExtra("board_type", boardType)
                writePostLauncher.launch(intent)
            }
        } else {
            binding.fabWritePost.hide()
        }

        // 게시글 목록 표시
        posts.clear()
        posts.addAll(getPostsForBoardType(boardType))

        postAdapter = PostAdapter(posts) { post ->
            // 게시글 클릭 시 해당 게시글 상세 페이지로 이동
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_type", boardType)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
        binding.rvPosts.visibility = View.VISIBLE
        binding.layoutPostDetail.visibility = View.GONE
    }

    // showPostDetail() 메소드 수정
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

        // 게시글 출처 표시 (공지냐 일반이냐)
        val sourceTag = if (post.isNotice) "공지" else "일반"
        binding.tvPostTag.text = sourceTag
        binding.tvPostTag.visibility = View.VISIBLE

        binding.rvPosts.visibility = View.GONE
        binding.layoutPostDetail.visibility = View.VISIBLE

        // 댓글 RecyclerView 설정
        commentsAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentsAdapter

        // 좋아요 버튼을 아이콘으로 변경
        binding.btnLike.text = ""
        binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0)
        binding.btnLike.setPadding(16, 16, 16, 16)

        // 스크랩 버튼을 아이콘으로 변경
        binding.btnScrap.text = ""
        binding.btnScrap.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_scrap, 0, 0, 0)
        binding.btnScrap.setPadding(16, 16, 16, 16)

        // 좋아요 버튼 클릭 이벤트
        binding.btnLike.setOnClickListener {
            // 추천 상태 토글
            val isLiked = binding.btnLike.tag as? Boolean ?: false
            val newState = !isLiked
            binding.btnLike.tag = newState

            // 버튼 상태 업데이트
            if (newState) {
                binding.btnLike.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.btnLike.compoundDrawableTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            } else {
                binding.btnLike.setBackgroundColor(resources.getColor(R.color.light_gray))
                binding.btnLike.compoundDrawableTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_gray))
            }

            Toast.makeText(this, if (newState) "추천되었습니다." else "추천이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 스크랩 버튼 클릭 이벤트
        binding.btnScrap.setOnClickListener {
            // 스크랩 상태 토글
            val isScraped = binding.btnScrap.tag as? Boolean ?: false
            val newState = !isScraped
            binding.btnScrap.tag = newState

            // 버튼 상태 업데이트
            if (newState) {
                binding.btnScrap.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.btnScrap.compoundDrawableTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            } else {
                binding.btnScrap.setBackgroundColor(resources.getColor(R.color.light_gray))
                binding.btnScrap.compoundDrawableTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_gray))
            }

            Toast.makeText(this, if (newState) "스크랩되었습니다." else "스크랩이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }

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

    private fun getPostsForBoardType(boardType: String): List<Post> {
        // 현재 날짜 기준 계산을 위한 Calendar 객체
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // 일주일 전 날짜 계산
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        val oneWeekAgo = calendar.time

        // 한 달 전 날짜 계산
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val oneMonthAgo = calendar.time

        // 날짜 문자열 파싱용 SimpleDateFormat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // 모든 게시글 가져오기 (샘플 데이터)
        val allPosts = getDummyPosts()

        return when (boardType) {
            "my_posts" -> {
                // 내가 쓴 글 (현재는 "작성자"가 "나"인 게시글로 가정)
                allPosts.filter { it.author == "나" }
            }
            "my_comments" -> {
                // 댓글 단 글 (실제 구현에서는 댓글 정보 필요)
                // 여기서는 commentCount가 5 이상인 게시글로 가정
                allPosts.filter { it.commentCount >= 5 }
            }
            "my_scraps" -> {
                // 스크랩한 글 (실제 구현에서는 스크랩 정보 필요)
                // 여기서는 임의로 id가 짝수인 게시글로 가정
                allPosts.filter { it.id % 2 == 0 }
            }
            "hot" -> {
                // HOT 게시판: 일주일 내 활동(조회수+댓글수) 20+ 게시글
                allPosts.filter { post ->
                    try {
                        val postDate = dateFormat.parse(post.date)
                        postDate != null && postDate.after(oneWeekAgo) &&
                                (post.viewCount + post.commentCount) >= 20
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            "best" -> {
                // BEST 게시판: 한달 내 활동(조회수+댓글수) 50+ 게시글
                allPosts.filter { post ->
                    try {
                        val postDate = dateFormat.parse(post.date)
                        postDate != null && postDate.after(oneMonthAgo) &&
                                (post.viewCount + post.commentCount) >= 50
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            "notice" -> allPosts.filter { it.isNotice }
            "tips" -> allPosts.filter { !it.isNotice && (it.title.contains("팁") || it.content.contains("꿀팁")) }
            else -> allPosts.filter { !it.isNotice } // general
        }
    }

    private fun getDummyPosts(): List<Post> {
        val posts = mutableListOf<Post>()

        // 공지사항
        posts.add(Post(1, "5월 정기 모임 안내", "5월 10일 오후 6시부터 중앙도서관 스터디룸에서 정기 모임이 있습니다.", "관리자", "2025-05-01", 15, 3, true))
        posts.add(Post(2, "춘계 MT 참가 신청", "이번 학기 춘계 MT 참가 신청을 받습니다.", "관리자", "2025-04-28", 32, 10, true))
        posts.add(Post(5, "동아리 회비 납부 안내", "5월 회비 납부 기간은 5월 1일부터 10일까지입니다.", "회계", "2025-04-25", 28, 5, true))

        // Tips
        posts.add(Post(3, "새내기를 위한 대학 생활 꿀팁", "1. 수강신청은 미리 준비하세요\n2. 도서관 이용방법을 숙지하세요", "선배01", "2025-04-25", 45, 8))
        posts.add(Post(4, "동아리 첫 모임에서 알아두면 좋은 것", "동아리 첫 모임에 가기 전에 동아리 페이지를 미리 둘러보고 가세요.", "회장", "2025-04-20", 38, 12))
        posts.add(Post(6, "효율적인 시간 관리 방법", "1. 투두리스트 활용하기\n2. 뽀모도로 기법 시도해보기", "시간관리장인", "2025-04-15", 56, 14))

        // 내가 쓴 글
        posts.add(Post(9, "내가 작성한 첫번째 게시글", "안녕하세요! 새로 가입했습니다.", "나", "2025-05-03", 5, 1))
        posts.add(Post(13, "내가 작성한 두번째 게시글", "활동 인증합니다.", "나", "2025-05-04", 8, 2))
        posts.add(Post(17, "내가 작성한 세번째 게시글", "다음 모임에 참석 못할 것 같습니다.", "나", "2025-05-05", 3, 0))

        // 일반 게시글 (많은 댓글 - 댓글 단 글에 표시될 게시글)
        posts.add(Post(7, "동아리 스터디 같이하실 분", "알고리즘 스터디 같이 하실 분 구합니다.", "스터디장", "2025-05-01", 20, 7))
        posts.add(Post(10, "학교 축제 같이 갈 사람", "이번 주 금요일 학교 축제 같이 갈 사람 있나요?", "축제러버", "2025-05-02", 30, 15))
        posts.add(Post(15, "동아리 로고 투표", "새로운 동아리 로고 투표를 진행합니다.", "디자인팀", "2025-05-03", 25, 8))

        // HOT 게시글 (일주일 내 활동 20+ 게시글)
        posts.add(Post(11, "인기 많은 HOT 게시글", "이 게시글은 조회수와 댓글이 많은 HOT 게시글입니다.", "인기작가", "2025-05-04", 40, 25))
        posts.add(Post(14, "또 다른 HOT 게시글", "이 게시글도 인기가 많습니다.", "핫유저", "2025-05-05", 35, 18))

        // BEST 게시글 (한달 내 활동 50+ 게시글)
        posts.add(Post(12, "BEST 게시글 예시", "이 게시글은 오랜 기간 인기가 많았던 BEST 게시글입니다.", "베스트작가", "2025-04-10", 150, 45))
        posts.add(Post(16, "또 다른 BEST 게시글", "이 게시글도 매우 인기가 많습니다.", "인기왕", "2025-04-15", 120, 35))

        // 일반 게시글
        posts.add(Post(8, "자유게시판 테스트", "이 게시글은 자유게시판 테스트용입니다.", "테스터", "2025-05-02", 10, 2))
        posts.add(Post(18, "동아리 행사 제안", "다음 달에 어떤 행사를 진행하면 좋을까요?", "기획자", "2025-05-06", 12, 4))

        return posts
    }
}
