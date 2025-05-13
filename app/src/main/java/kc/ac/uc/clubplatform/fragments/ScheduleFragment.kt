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
            binding.tvScheduleList.visibility = View.GONE
        } else {
            binding.tvNoSchedule.visibility = View.GONE
            binding.tvScheduleList.visibility = View.VISIBLE

            // 일정 목록 텍스트 구성
            val sb = StringBuilder()
            for (schedule in filteredSchedules) {
                sb.append("■ ${schedule.title}\n")
                sb.append("  장소: ${schedule.place}\n")
                sb.append("  시간: ${schedule.time}\n\n")
            }
            binding.tvScheduleList.text = sb.toString()

            // 일정 편집/삭제 기능 구현 (간단하게 각 일정을 길게 누르면 메뉴 표시)
            binding.tvScheduleList.setOnLongClickListener {
                if (filteredSchedules.isNotEmpty()) {
                    showScheduleOptionsDialog(filteredSchedules)
                }
                true
            }
        }
    }

    // 일정 관리 옵션 대화상자 표시
    private fun showScheduleOptionsDialog(scheduleList: List<Schedule>) {
        val scheduleItems = scheduleList.map { it.title }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("관리할 일정 선택")
            .setItems(scheduleItems) { _, which ->
                val selectedSchedule = scheduleList[which]
                showScheduleActionDialog(selectedSchedule)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 일정 관리 액션 대화상자 표시
    private fun showScheduleActionDialog(schedule: Schedule) {
        val options = arrayOf("일정 수정", "일정 삭제")

        AlertDialog.Builder(requireContext())
            .setTitle(schedule.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditScheduleDialog(schedule)
                    1 -> showDeleteScheduleDialog(schedule)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 일정 수정 다이얼로그
    private fun showEditScheduleDialog(schedule: Schedule) {
        val dialogBinding = DialogAddScheduleBinding.inflate(layoutInflater)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // 기존 일정 정보로 초기화
        dialogBinding.etTitle.setText(schedule.title)
        dialogBinding.etDate.setText(dateFormat.format(schedule.date))
        dialogBinding.etTime.setText(schedule.time)
        dialogBinding.etPlace.setText(schedule.place)
        dialogBinding.etContent.setText(schedule.content)

        AlertDialog.Builder(requireContext())
            .setTitle("일정 수정")
            .setView(dialogBinding.root)
            .setPositiveButton("수정") { _, _ ->
                val title = dialogBinding.etTitle.text.toString()
                val place = dialogBinding.etPlace.text.toString()
                val time = dialogBinding.etTime.text.toString()
                val content = dialogBinding.etContent.text.toString()

                if (title.isNotEmpty()) {
                    // 기존 일정 삭제
                    schedules.remove(schedule)

                    // 수정된 일정 추가
                    val updatedSchedule = Schedule(
                        schedule.id,
                        title,
                        place,
                        selectedDate,
                        time,
                        content
                    )
                    schedules.add(updatedSchedule)
                    updateScheduleList()
                    Toast.makeText(requireContext(), "일정이 수정되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "일정 제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 일정 삭제 다이얼로그
    private fun showDeleteScheduleDialog(schedule: Schedule) {
        AlertDialog.Builder(requireContext())
            .setTitle("일정 삭제")
            .setMessage("정말 이 일정을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                schedules.remove(schedule)
                updateScheduleList()
                Toast.makeText(requireContext(), "일정이 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}