// fragments/HomeFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.databinding.FragmentHomeBinding
import kc.ac.uc.clubplatform.BoardActivity
import kc.ac.uc.clubplatform.NotificationActivity
import kc.ac.uc.clubplatform.ProfileActivity
import kc.ac.uc.clubplatform.SearchActivity
import kc.ac.uc.clubplatform.adapters.NoticeAdapter
import kc.ac.uc.clubplatform.adapters.TipAdapter
import kc.ac.uc.clubplatform.models.Post

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupNoticesRecyclerView()
        setupTipsRecyclerView()
        setupBoardButtons()
    }

    private fun setupHeader() {
        // 검색 아이콘 클릭 시 검색 페이지로 이동
        binding.ivSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        // 알림 아이콘 클릭 시 알림 페이지로 이동
        binding.ivNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        // 프로필 아이콘 클릭 시 프로필 페이지로 이동
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
    }

    private fun setupNoticesRecyclerView() {
        // 샘플 공지사항 데이터
        val notices = listOf(
            Post(1, "5월 정기 모임 안내", "5월 10일 오후 6시부터 중앙도서관...", "관리자", "2025-05-01", 15, 3),
            Post(2, "춘계 MT 참가 신청", "이번 학기 춘계 MT 참가 신청을 받습니다...", "관리자", "2025-04-28", 32, 10)
        )

        val adapter = NoticeAdapter(notices) { post ->
            // 공지사항 클릭 이벤트 처리
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = adapter

        // 공지사항 더보기 버튼 클릭 이벤트
        binding.btnMoreNotices.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            startActivity(intent)
        }
    }

    private fun setupTipsRecyclerView() {
        // 샘플 팁 데이터
        val tips = listOf(
            Post(3, "새내기를 위한 대학 생활 꿀팁", "1. 수강신청은 미리 준비하세요...", "선배01", "2025-04-25", 45, 8),
            Post(4, "동아리 첫 모임에서 알아두면 좋은 것", "동아리 첫 모임에 가기 전에...", "회장", "2025-04-20", 38, 12)
        )

        val adapter = TipAdapter(tips) { post ->
            // 팁 클릭 이벤트 처리
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "tips")
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }

        binding.rvTips.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTips.adapter = adapter

        // 팁 더보기 버튼 클릭 이벤트
        binding.btnMoreTips.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "tips")
            startActivity(intent)
        }
    }

    private fun setupBoardButtons() {
        // 일반 게시판 버튼 클릭 이벤트
        binding.btnGeneralBoard.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "general")
            startActivity(intent)
        }

        // 공지 게시판 버튼 클릭 이벤트
        binding.btnNoticeBoard.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}