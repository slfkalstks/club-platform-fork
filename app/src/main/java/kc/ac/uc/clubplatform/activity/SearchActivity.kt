// 업데이트된 SearchActivity.kt
package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.adapters.RecentSearchAdapter
import kc.ac.uc.clubplatform.adapters.SearchResultAdapter
import kc.ac.uc.clubplatform.databinding.ActivitySearchBinding
import kc.ac.uc.clubplatform.models.PostInfo
import kc.ac.uc.clubplatform.models.RecentSearch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // 최근 검색어 목록 (실제로는 SharedPreferences나 DB에서 가져와야 함)
    private val recentSearches = mutableListOf<RecentSearch>()

    // 검색 결과 (실제로는 API 호출로 가져와야 함)
    private val searchResults = mutableListOf<PostInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 샘플 데이터 추가
        recentSearches.add(RecentSearch(1, "동아리"))
        recentSearches.add(RecentSearch(2, "모임"))
        recentSearches.add(RecentSearch(3, "프로젝트"))

        setupSearchBar()
        setupRecentSearches()
    }

    private fun setupSearchBar() {
        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 검색창에서 검색 버튼 클릭 시
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    // 최근 검색어에 추가
                    addToRecentSearches(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // 검색 버튼 클릭 시
        binding.ivSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
                // 최근 검색어에 추가
                addToRecentSearches(query)
            }
        }
    }

    private fun setupRecentSearches() {
        if (recentSearches.isEmpty()) {
            binding.tvNoRecentSearches.visibility = View.VISIBLE
            binding.rvRecentSearches.visibility = View.GONE
            binding.btnClearRecentSearches.visibility = View.GONE
        } else {
            binding.tvNoRecentSearches.visibility = View.GONE
            binding.rvRecentSearches.visibility = View.VISIBLE
            binding.btnClearRecentSearches.visibility = View.VISIBLE

            val adapter = RecentSearchAdapter(
                recentSearches,
                onItemClick = { recentSearch ->
                    // 최근 검색어 클릭 시 해당 검색어로 검색
                    binding.etSearch.setText(recentSearch.query)
                    performSearch(recentSearch.query)
                },
                onDeleteClick = { recentSearch ->
                    // 최근 검색어 삭제
                    recentSearches.remove(recentSearch)
                    setupRecentSearches()
                }
            )

            binding.rvRecentSearches.layoutManager = LinearLayoutManager(this)
            binding.rvRecentSearches.adapter = adapter

            // 최근 검색어 전체 삭제 버튼
            binding.btnClearRecentSearches.setOnClickListener {
                recentSearches.clear()
                setupRecentSearches()
            }
        }
    }

    private fun addToRecentSearches(query: String) {
        // 이미 있는 검색어라면 제거
        recentSearches.removeAll { it.query == query }

        // 새로운 검색어 추가
        recentSearches.add(0, RecentSearch(recentSearches.size + 1, query))

        // 최근 검색어는 최대 10개만 유지
        if (recentSearches.size > 10) {
            recentSearches.removeAt(recentSearches.size - 1)
        }

        setupRecentSearches()
    }

    private fun performSearch(query: String) {
        // 실제 검색 수행 (API 호출 등)
        // 여기서는 샘플 데이터로 대체

        searchResults.clear()

        // PostInfo 모델로 샘플 데이터 생성
        searchResults.add(PostInfo(
            postId = 1,
            title = "5월 정기 모임 안내",
            content = "5월 10일 오후 6시부터 중앙도서관 스터디룸에서 정기 모임이 있습니다. 모든 회원 참석 부탁드립니다.",
            authorName = "관리자",
            createdAt = "2025-05-01T18:00:00",
            viewCount = 15,
            commentCount = 3,
            isNotice = true
        ))

        searchResults.add(PostInfo(
            postId = 2,
            title = "춘계 MT 참가 신청",
            content = "이번 학기 춘계 MT 참가 신청을 받습니다. 5월 24일부터 26일까지 2박 3일 일정입니다.",
            authorName = "관리자",
            createdAt = "2025-04-28T14:30:00",
            viewCount = 32,
            commentCount = 10,
            isNotice = false
        ))

        searchResults.add(PostInfo(
            postId = 3,
            title = "새내기를 위한 대학 생활 꿀팁",
            content = "1. 수강신청은 미리 준비하세요\n2. 도서관 이용방법을 숙지하세요\n3. 교수님 연구실 위치를 알아두세요",
            authorName = "선배01",
            createdAt = "2025-04-25T10:15:00",
            viewCount = 45,
            commentCount = 8,
            isNotice = false
        ))

        // 검색 결과가 있는 경우
        if (searchResults.isNotEmpty()) {
            binding.layoutRecentSearches.visibility = View.GONE
            binding.layoutSearchResults.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.tvNoSearchResults.visibility = View.GONE

            val adapter = SearchResultAdapter(searchResults) { post ->
                // 검색 결과 클릭 이벤트 처리
                val intent = Intent(this, BoardActivity::class.java)
                intent.putExtra("post_id", post.postId)
                // 현재 동아리 ID 전달
                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val clubId = sharedPreferences.getInt("current_club_id", -1)
                intent.putExtra("club_id", clubId)
                startActivity(intent)
            }

            binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
            binding.rvSearchResults.adapter = adapter
        } else {
            // 검색 결과가 없는 경우
            binding.layoutRecentSearches.visibility = View.GONE
            binding.layoutSearchResults.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
            binding.tvNoSearchResults.visibility = View.VISIBLE
            binding.tvNoSearchResults.text = "'$query'에 대한 검색 결과가 없습니다."
        }
    }
}