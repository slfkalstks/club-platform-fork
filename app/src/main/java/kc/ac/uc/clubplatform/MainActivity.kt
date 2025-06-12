package kc.ac.uc.clubplatform

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.fragments.ChatFragment
import kc.ac.uc.clubplatform.fragments.HomeFragment
import kc.ac.uc.clubplatform.fragments.ScheduleFragment
import kc.ac.uc.clubplatform.fragments.BoardListFragment
import kc.ac.uc.clubplatform.databinding.ActivityMainBinding

class ClubPlatformApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // ApiClient 초기화
        ApiClient.init(applicationContext)
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 기본 화면을 홈으로 설정
        replaceFragment(HomeFragment())

        // 바텀 네비게이션 설정
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_home -> replaceFragment(HomeFragment())
                R.id.menu_schedule -> replaceFragment(ScheduleFragment())
                R.id.menu_board_list -> replaceFragment(BoardListFragment())
                R.id.menu_chat -> replaceFragment(ChatFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
