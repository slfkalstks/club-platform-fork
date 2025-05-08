// NotificationActivity.kt
package kc.ac.uc.clubplatform

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kc.ac.uc.clubplatform.databinding.ActivityNotificationBinding
import kc.ac.uc.clubplatform.fragments.NewsFeedFragment
import kc.ac.uc.clubplatform.fragments.NotificationKeywordFragment
import com.google.android.material.tabs.TabLayoutMediator

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupViewPager()
    }

    private fun setupHeader() {
        // 뒤로가기 버튼 설정
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        // 뷰페이저 어댑터 설정
        val adapter = NotificationPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 탭 레이아웃과 뷰페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "새 소식"
                1 -> "키워드"
                else -> ""
            }
        }.attach()
    }

    // 뷰페이저 어댑터
    private inner class NotificationPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NewsFeedFragment()
                1 -> NotificationKeywordFragment()
                else -> NewsFeedFragment()
            }
        }
    }
}