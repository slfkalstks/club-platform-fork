// fragments/ScheduleFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kc.ac.uc.clubplatform.databinding.FragmentScheduleBinding
import kc.ac.uc.clubplatform.databinding.DialogAddScheduleBinding
import kc.ac.uc.clubplatform.models.Schedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    // 일정 목록 (실제로는 DB에서 불러와야 함)
    private val schedules = mutableListOf<Schedule>()

    // 현재 선택된 날짜
    private var selectedDate: Date = Date()

    // 관리자 여부 (실제로는 로그인 정보에서 확인해야 함)
    private val isAdmin = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 샘플 데이터 추가
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        schedules.add(Schedule(1, "정기 모임", "중앙도서관 스터디룸", sdf.parse("2025-05-10")!!, "18:00~20:00"))
        schedules.add(Schedule(2, "춘계 MT", "강원도 펜션", sdf.parse("2025-05-24")!!, "2박 3일"))

        setupCalendar()
        setupAddButton()
        updateScheduleList()
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            updateScheduleList()
        }
    }

    private fun setupAddButton() {
        // 관리자만 일정 추가 버튼 표시
        if (isAdmin) {
            binding.fabAddSchedule.visibility = View.VISIBLE
            binding.fabAddSchedule.setOnClickListener {
                showAddScheduleDialog()
            }
        } else {
            binding.fabAddSchedule.visibility = View.GONE
        }
    }

    private fun showAddScheduleDialog() {
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dialogBinding.etDate.setText(dateFormat.format(selectedDate))

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("일정 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("등록") { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val place = dialogBinding.etPlace.text.toString()
                val time = dialogBinding.etTime.text.toString()

                if (title.isNotEmpty()) {
                    // 새 일정 추가
                    val newSchedule = Schedule(
                        schedules.size + 1,
                        title,
                        place,
                        selectedDate,
                        time
                    )
                    schedules.add(newSchedule)
                    updateScheduleList()
                    Toast.makeText(requireContext(), "일정이 등록되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "일정 제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    private fun updateScheduleList() {
        // 선택된 날짜의 일정만 필터링
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(selectedDate)

        val filteredSchedules = schedules.filter {
            dateFormat.format(it.date) == selectedDateStr
        }

        if (filteredSchedules.isEmpty()) {
            binding.tvNoSchedule.visibility = View.VISIBLE
            binding.rvSchedules.visibility = View.GONE
        } else {
            binding.tvNoSchedule.visibility = View.GONE
            binding.rvSchedules.visibility = View.VISIBLE

            // 여기에 일정 목록 어댑터 설정 (생략)
            // binding.rvSchedules.adapter = ScheduleAdapter(filteredSchedules)

            // 간단히 텍스트로 표시 (예시)
            val scheduleText = filteredSchedules.joinToString("\n\n") {
                "${it.title}\n${it.place}\n${it.time}"
            }
            binding.tvScheduleList.text = scheduleText
            binding.tvScheduleList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}