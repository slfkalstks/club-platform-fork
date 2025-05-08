// fragments/NotificationKeywordFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.adapters.KeywordAdapter
import kc.ac.uc.clubplatform.databinding.FragmentNotificationKeywordBinding
import kc.ac.uc.clubplatform.models.Keyword

class NotificationKeywordFragment : Fragment() {
    private var _binding: FragmentNotificationKeywordBinding? = null
    private val binding get() = _binding!!

    private val keywords = mutableListOf<Keyword>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 샘플 데이터 추가
        keywords.add(Keyword(1, "동아리"))
        keywords.add(Keyword(2, "모임"))
        keywords.add(Keyword(3, "프로젝트"))

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        if (keywords.isEmpty()) {
            binding.tvNoKeywords.visibility = View.VISIBLE
            binding.rvKeywords.visibility = View.GONE
            binding.btnRegisterKeyword.visibility = View.VISIBLE
        } else {
            binding.tvNoKeywords.visibility = View.GONE
            binding.rvKeywords.visibility = View.VISIBLE
            binding.btnRegisterKeyword.visibility = View.GONE

            val adapter = KeywordAdapter(keywords) { keyword ->
                // 키워드 삭제 이벤트
                keywords.remove(keyword)

                if (keywords.isEmpty()) {
                    binding.tvNoKeywords.visibility = View.VISIBLE
                    binding.rvKeywords.visibility = View.GONE
                    binding.btnRegisterKeyword.visibility = View.VISIBLE
                }
            }

            binding.rvKeywords.layoutManager = LinearLayoutManager(requireContext())
            binding.rvKeywords.adapter = adapter
        }
    }

    private fun setupButtons() {
        // 키워드 등록 버튼
        binding.btnRegisterKeyword.setOnClickListener {
            showKeywordRegistrationScreen()
        }

        // 키워드 추가 버튼
        binding.btnAddKeyword.setOnClickListener {
            val keyword = binding.etKeyword.text.toString().trim()

            if (keyword.isNotEmpty()) {
                // 키워드 추가
                val newKeyword = Keyword(keywords.size + 1, keyword)
                keywords.add(newKeyword)
                binding.etKeyword.text.clear()

                // 리사이클러뷰 업데이트
                setupRecyclerView()

                Toast.makeText(requireContext(), "키워드가 추가되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "키워드를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showKeywordRegistrationScreen() {
        binding.layoutKeywordRegistration.visibility = View.VISIBLE
        binding.btnRegisterKeyword.visibility = View.GONE
        binding.tvNoKeywords.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}