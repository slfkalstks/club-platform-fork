// fragments/BoardListFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.activity.BoardActivity
import kc.ac.uc.clubplatform.adapters.BoardAdapter
import kc.ac.uc.clubplatform.databinding.FragmentBoardListBinding
import kc.ac.uc.clubplatform.models.Board

class BoardListFragment : Fragment() {
    private var _binding: FragmentBoardListBinding? = null
    private val binding get() = _binding!!

    // 게시판 목록 (실제로는 DB에서 불러와야 함)
    private val boards = mutableListOf<Board>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 샘플 데이터 추가
        boards.add(Board(1, "내가 쓴 글", "my_posts", "내가 작성한 게시글 모아보기"))
        boards.add(Board(2, "댓글 단 글", "my_comments", "내가 댓글을 작성한 게시글 모아보기"))
        boards.add(Board(3, "스크랩", "my_scraps", "내가 스크랩한 게시글 모아보기"))
        boards.add(Board(4, "HOT 게시판", "hot", "인기 있는 게시글 모아보기"))
        boards.add(Board(5, "BEST 게시판", "best", "좋아요를 많이 받은 게시글 모아보기"))
        boards.add(Board(6, "일반게시판", "general", "자유롭게 소통하는 게시판"))
        boards.add(Board(7, "공지게시판", "notice", "동아리 공지사항을 확인하는 게시판"))
        boards.add(Board(8, "Tips", "tips", "유용한 정보를 공유하는 게시판"))

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // 샘플 데이터 추가
        boards.clear()
        boards.add(Board(1, "내가 쓴 글", "my_posts", "내가 작성한 게시글 모아보기"))
        boards.add(Board(2, "댓글 단 글", "my_comments", "내가 댓글을 작성한 게시글 모아보기"))
        boards.add(Board(3, "스크랩", "my_scraps", "내가 스크랩한 게시글 모아보기"))
        boards.add(Board(4, "HOT 게시판", "hot", "일주일 내 인기 게시글 (20+ 활동)"))
        boards.add(Board(5, "BEST 게시판", "best", "한달 내 인기 게시글 (50+ 활동)"))
        boards.add(Board(6, "일반게시판", "general", "자유롭게 소통하는 게시판"))
        boards.add(Board(7, "공지게시판", "notice", "동아리 공지사항을 확인하는 게시판"))
        boards.add(Board(8, "Tips", "tips", "유용한 정보를 공유하는 게시판"))

        val adapter = BoardAdapter(boards) { board ->
            // 게시판 클릭 이벤트 처리
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", board.type)
            startActivity(intent)
        }

        binding.rvBoards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBoards.adapter = adapter
    }
}