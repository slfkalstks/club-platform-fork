package kc.ac.uc.clubplatform.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.databinding.FragmentScheduleBinding
import kc.ac.uc.clubplatform.models.Schedule
import kc.ac.uc.clubplatform.models.ScheduleRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.cleverpumpkin.calendar.CalendarDate
import ru.cleverpumpkin.calendar.CalendarView.SelectionMode
import ru.cleverpumpkin.calendar.CalendarView.DateIndicator
import java.text.SimpleDateFormat
import java.util.*

// DateIndicator 구현체로 점 표시
data class ScheduleIndicator(
    override val date: CalendarDate,
    override val color: Int,
    val scheduleId: Int  // 해당 일정의 ID
) : DateIndicator

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleList = mutableListOf<Schedule>()
    private var selectedDate: CalendarDate? = null
    private val indicatorList = mutableListOf<ScheduleIndicator>()
    
    // 현재 사용자의 동아리 ID (실제로는 세션이나 설정에서 가져와야 함)
    private var currentClubId = -1

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
        setupCalendar()
        setupFAB()
        fetchSchedules()
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        calendar.set(currentYear - 1, Calendar.JANUARY, 1)
        val minDate = CalendarDate(calendar.time)
        calendar.set(currentYear + 1, Calendar.DECEMBER, 31)
        val maxDate = CalendarDate(calendar.time)
        val initialDate = CalendarDate(Calendar.getInstance().time)

        binding.crunchyCalendarView.setupCalendar(
            initialDate = initialDate,
            minDate = minDate,
            maxDate = maxDate,
            selectionMode = SelectionMode.RANGE,
            firstDayOfWeek = Calendar.SUNDAY,
            showYearSelectionView = true
        )

        // 점(인디케이터) 표시
        binding.crunchyCalendarView.datesIndicators = indicatorList

        binding.crunchyCalendarView.onDateClickListener = { date ->
            selectedDate = date
            filterSchedulesByDate(date)
        }
    }

    private fun setupFAB() {
        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }
    }

    private fun showAddScheduleDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_schedule, null, false)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etPlace = dialogView.findViewById<EditText>(R.id.etPlace) // 장소 필드 추가
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)
        val btnSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)

        // 시간 필드에 클릭 리스너 추가
        etTime.setOnClickListener {
            showTimePickerDialog { startTime, endTime ->
                // 선택된 시간을 텍스트 필드에 표시
                etTime.setText("$startTime ~ $endTime")
            }
        }

        // 선택된 날짜 범위를 가져와서 시작일과 종료일 필드에 설정
        val (startDate, endDate) = getSelectedDateRange()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if (startDate != null) {
            etStartDate.setText(dateFormat.format(startDate))
            // 종료일이 없으면(단일 날짜 선택) 시작일과 동일하게 설정
            val finalEndDate = endDate ?: startDate
            etEndDate.setText(dateFormat.format(finalEndDate))
        }

        // AlertDialog 객체 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("일정 추가")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val startDateStr = etStartDate.text.toString()
            val endDateStr = etEndDate.text.toString()
            val timeStr = etTime.text.toString()
            val place = etPlace.text.toString()
            val content = etContent.text.toString()

            if (title.isBlank() || startDateStr.isBlank() || endDateStr.isBlank()) {
                Toast.makeText(requireContext(), "제목과 날짜를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 날짜와 시간 파싱
            val startTimeStr = if (timeStr.contains("~")) timeStr.split("~")[0].trim() else ""
            val endTimeStr = if (timeStr.contains("~")) timeStr.split("~")[1].trim() else ""

            // ISO 형식(yyyy-MM-dd'T'HH:mm:ss)으로 변환
            val isoStartDate = "${startDateStr}T${startTimeStr}:00"
            val isoEndDate = "${endDateStr}T${endTimeStr}:00" 

            // 스케줄 생성 요청 생성
            val scheduleRequest = ScheduleRequest(
                clubId = currentClubId,
                title = title,
                description = content.ifBlank { null },
                place = place.ifBlank { null },
                startDate = isoStartDate,
                endDate = isoEndDate,
                allDay = timeStr.isBlank()
            )

            // API 요청 전송
            createSchedule(scheduleRequest)
            dialog.dismiss()
        }

        // 취소 버튼 클릭 리스너
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createSchedule(scheduleRequest: ScheduleRequest) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.createSchedule(scheduleRequest)
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        fetchSchedules() // 일정 목록 새로고침
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정 추가에 실패했습니다: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getSelectedDateRange(): Pair<Date?, Date?> {
        val selectedDates = binding.crunchyCalendarView.selectedDates

        if (selectedDates.isEmpty()) {
            return Pair(null, null)
        }

        // 선택된 날짜들을 Date로 변환하고 정렬
        val dates = selectedDates.map { it.date }.sorted()

        val startDate = dates.first() // 가장 빠른 날짜
        val endDate = if (dates.size == 1) {
            // 단일 날짜 선택시 종료일도 동일한 날짜로 설정
            dates.first()
        } else {
            // 여러 날짜 선택시 가장 늦은 날짜
            dates.last()
        }

        return Pair(startDate, endDate)
    }

    private fun fetchSchedules() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 전체 일정 또는 동아리별 일정 가져오기
                val response = if (currentClubId > 0) {
                    ApiClient.apiService.getSchedulesByClub(currentClubId)
                } else {
                    ApiClient.apiService.getAllSchedules()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val scheduleResponse = response.body()!!
                    val tempList = mutableListOf<Schedule>()
                    val tempIndicators = mutableListOf<ScheduleIndicator>()

                    scheduleResponse.schedules.forEach { schedule ->
                        tempList.add(schedule)
                        
                        // 시작~종료 범위 모두 점 표시 (인디케이터 추가)
                        val cal = Calendar.getInstance()
                        cal.time = schedule.startDate
                        val end = schedule.endDate ?: schedule.startDate
                        
                        // 날짜별로 인디케이터 추가
                        while (!cal.time.after(end)) {
                            tempIndicators.add(
                                ScheduleIndicator(
                                    date = CalendarDate(cal.time),
                                    color = getScheduleColor(schedule),
                                    scheduleId = schedule.scheduleId
                                )
                            )
                            cal.add(Calendar.DATE, 1)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        scheduleList.clear()
                        scheduleList.addAll(tempList)
                        indicatorList.clear()
                        indicatorList.addAll(tempIndicators)
                        binding.crunchyCalendarView.datesIndicators = indicatorList
                        selectedDate?.let { filterSchedulesByDate(it) }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정을 불러오지 못했습니다: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "일정 데이터를 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 일정 유형에 따라 색상 지정
    private fun getScheduleColor(schedule: Schedule): Int {
        // 예시: 동아리 ID에 따라 색상 지정
        return when {
            schedule.allDay -> 0xFF9C27B0.toInt() // 하루종일 일정은 보라색
            schedule.place?.isNotBlank() == true -> 0xFFE91E63.toInt() // 장소가 있는 일정은 분홍색
            else -> 0xFF2196F3.toInt() // 기본은 파란색
        }
    }

    private fun filterSchedulesByDate(date: CalendarDate) {
        val cal = Calendar.getInstance()
        cal.time = date.date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.time
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.time

        // 해당 날짜에 해당하는 일정 필터링
        val filtered = scheduleList.filter {
            // 일정 시작일이 선택한 날짜와 같거나,
            // 일정 종료일이 선택한 날짜 범위 내에 있는 경우
            (it.startDate >= startOfDay && it.startDate < endOfDay) ||
            (it.endDate != null && it.startDate <= startOfDay && it.endDate >= startOfDay)
        }

        if (filtered.isEmpty()) {
            binding.tvScheduleList.text = "이 날짜에는 일정이 없습니다."
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        for (item in filtered) {
            sb.append("📌 ${item.title}\n")
            if (!item.place.isNullOrBlank()) {
                sb.append("📍 장소: ${item.place}\n")
            }
            sb.append("⏰ 시간: ${sdf.format(item.startDate)} ~ ${item.endDate?.let { sdf.format(it) } ?: "미정"}\n")
            if (!item.description.isNullOrBlank()) {
                sb.append("📝 설명: ${item.description}\n")
            }
            sb.append(if (item.allDay) "[하루종일]\n" else "")
            sb.append("\n")
        }
        binding.tvScheduleList.text = sb.toString().trim()
        
        // 일정 클릭 가능하도록 이벤트 설정
        binding.tvScheduleList.setOnClickListener {
            if (filtered.isNotEmpty()) {
                showScheduleOptions(filtered[0])
            }
        }
    }

    private fun showScheduleOptions(schedule: Schedule) {
        val options = arrayOf("수정", "삭제", "취소")
        
        AlertDialog.Builder(requireContext())
            .setTitle(schedule.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditScheduleDialog(schedule)
                    1 -> confirmDeleteSchedule(schedule)
                }
            }
            .show()
    }

    private fun showEditScheduleDialog(schedule: Schedule) {
        // 수정 다이얼로그 표시 - 기존 일정 정보 채우기
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_schedule, null, false)

        // 다이얼로그 내 뷰 설정
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etPlace = dialogView.findViewById<EditText>(R.id.etPlace)
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)

        // 기존 데이터 채우기
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        etTitle.setText(schedule.title)
        etStartDate.setText(dateFormat.format(schedule.startDate))
        etEndDate.setText(schedule.endDate?.let { dateFormat.format(it) } ?: dateFormat.format(schedule.startDate))
        
        if (!schedule.allDay) {
            val startTime = timeFormat.format(schedule.startDate)
            val endTime = schedule.endDate?.let { timeFormat.format(it) } ?: startTime
            etTime.setText("$startTime ~ $endTime")
        }
        
        etPlace.setText(schedule.place ?: "")
        etContent.setText(schedule.description ?: "")

        // 다이얼로그 빌더 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("일정 수정")
            .setView(dialogView)
            .create()

        // 저장 버튼 클릭 리스너
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave).setOnClickListener {
            // 수정된 데이터 가져오기
            val title = etTitle.text.toString()
            val startDateStr = etStartDate.text.toString()
            val endDateStr = etEndDate.text.toString()
            val timeStr = etTime.text.toString()
            val place = etPlace.text.toString()
            val content = etContent.text.toString()

            if (title.isBlank() || startDateStr.isBlank()) {
                Toast.makeText(requireContext(), "제목과 시작 날짜는 필수입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 날짜와 시간 파싱
            val startTimeStr = if (timeStr.contains("~")) timeStr.split("~")[0].trim() else ""
            val endTimeStr = if (timeStr.contains("~")) timeStr.split("~")[1].trim() else ""

            // ISO 형식(yyyy-MM-dd'T'HH:mm:ss)으로 변환
            val isoStartDate = "${startDateStr}T${startTimeStr}:00"
            val isoEndDate = "${endDateStr}T${endTimeStr}:00" 

            // 스케줄 업데이트 요청 생성
            val scheduleRequest = ScheduleRequest(
                clubId = schedule.clubId,
                title = title,
                description = content.ifBlank { null },
                place = place.ifBlank { null },
                startDate = isoStartDate,
                endDate = isoEndDate,
                allDay = timeStr.isBlank()
            )

            // 업데이트 API 요청
            updateSchedule(schedule.scheduleId, scheduleRequest)
            dialog.dismiss()
        }

        // 취소 버튼 클릭 리스너
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSchedule(scheduleId: Int, request: ScheduleRequest) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.updateSchedule(scheduleId, request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        fetchSchedules() // 일정 목록 새로고침
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정 수정에 실패했습니다: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmDeleteSchedule(schedule: Schedule) {
        AlertDialog.Builder(requireContext())
            .setTitle("일정 삭제")
            .setMessage("\"${schedule.title}\" 일정을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteSchedule(schedule.scheduleId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteSchedule(scheduleId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.deleteSchedule(scheduleId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        fetchSchedules() // 일정 목록 새로고침
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "일정 삭제에 실패했습니다: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTimePickerDialog(onTimeSelected: (String, String) -> Unit) {
        // Dialog 대신 AlertDialog.Builder 사용
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_picker, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // 다이얼로그 배경 투명하게 설정
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvDateRange = dialogView.findViewById<TextView>(R.id.tvDateRange)
        val tvStartTimeTab = dialogView.findViewById<TextView>(R.id.tvStartTimeTab)
        val tvEndTimeTab = dialogView.findViewById<TextView>(R.id.tvEndTimeTab)
        val npAmPm = dialogView.findViewById<NumberPicker>(R.id.npAmPm)
        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)
        val btnAddHour = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddHour)
        val btnAdd20Min = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAdd20Min)
        val btnAdd10Min = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAdd10Min)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm)
        val tvClose = dialogView.findViewById<TextView>(R.id.tvClose)
        val tvRefresh = dialogView.findViewById<TextView>(R.id.tvRefresh)

        // NumberPicker 설정
        npAmPm.minValue = 0
        npAmPm.maxValue = 1
        npAmPm.displayedValues = arrayOf("오전", "오후")

        npHour.minValue = 1
        npHour.maxValue = 12

        npMinute.minValue = 0
        npMinute.maxValue = 5
        npMinute.displayedValues = arrayOf("00", "10", "20", "30", "40", "50")

        // 기본값 설정 (현재 시간)
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentAmPm = calendar.get(Calendar.AM_PM)

        npAmPm.value = currentAmPm
        npHour.value = if (currentHour == 0) 12 else currentHour
        npMinute.value = currentMinute / 10

        var isSelectingStartTime = true
        var selectedStartHour = npHour.value
        var selectedStartMinute = npMinute.value * 10
        var selectedStartAmPm = npAmPm.value
        var selectedEndHour = npHour.value
        var selectedEndMinute = npMinute.value * 10
        var selectedEndAmPm = npAmPm.value

        fun updateTabUI() {
            if (isSelectingStartTime) {
                tvStartTimeTab.setBackgroundResource(R.drawable.tab_selected_background)
                tvStartTimeTab.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                tvEndTimeTab.setBackgroundResource(R.drawable.tab_unselected_background)
                tvEndTimeTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))

                npAmPm.value = selectedStartAmPm
                npHour.value = selectedStartHour
                npMinute.value = selectedStartMinute / 10
            } else {
                tvEndTimeTab.setBackgroundResource(R.drawable.tab_selected_background)
                tvEndTimeTab.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                tvStartTimeTab.setBackgroundResource(R.drawable.tab_unselected_background)
                tvStartTimeTab.setTextColor(resources.getColor(android.R.color.darker_gray, null))

                npAmPm.value = selectedEndAmPm
                npHour.value = selectedEndHour
                npMinute.value = selectedEndMinute / 10
            }
        }

        // 탭 클릭 리스너
        tvStartTimeTab.setOnClickListener {
            if (!isSelectingStartTime) {
                // 현재 선택된 종료시간 저장
                selectedEndAmPm = npAmPm.value
                selectedEndHour = npHour.value
                selectedEndMinute = npMinute.value * 10

                isSelectingStartTime = true
                updateTabUI()
            }
        }

        tvEndTimeTab.setOnClickListener {
            if (isSelectingStartTime) {
                // 현재 선택된 시작시간 저장
                selectedStartAmPm = npAmPm.value
                selectedStartHour = npHour.value
                selectedStartMinute = npMinute.value * 10

                isSelectingStartTime = false
                updateTabUI()
            }
        }

        // NumberPicker 변경 리스너
        npAmPm.setOnValueChangedListener { _, _, newVal ->
            if (isSelectingStartTime) {
                selectedStartAmPm = newVal
            } else {
                selectedEndAmPm = newVal
            }
        }

        npHour.setOnValueChangedListener { _, _, newVal ->
            if (isSelectingStartTime) {
                selectedStartHour = newVal
            } else {
                selectedEndHour = newVal
            }
        }

        npMinute.setOnValueChangedListener { _, _, newVal ->
            if (isSelectingStartTime) {
                selectedStartMinute = newVal * 10
            } else {
                selectedEndMinute = newVal * 10
            }
        }

        // 빠른 추가 버튼들
        btnAddHour.setOnClickListener {
            if (isSelectingStartTime) {
                selectedStartHour = if (selectedStartHour == 12) 1 else selectedStartHour + 1
                npHour.value = selectedStartHour
            } else {
                selectedEndHour = if (selectedEndHour == 12) 1 else selectedEndHour + 1
                npHour.value = selectedEndHour
            }
        }

        btnAdd20Min.setOnClickListener {
            if (isSelectingStartTime) {
                selectedStartMinute = (selectedStartMinute + 20) % 60
                npMinute.value = selectedStartMinute / 10
            } else {
                selectedEndMinute = (selectedEndMinute + 20) % 60
                npMinute.value = selectedEndMinute / 10
            }
        }

        btnAdd10Min.setOnClickListener {
            if (isSelectingStartTime) {
                selectedStartMinute = (selectedStartMinute + 10) % 60
                npMinute.value = selectedStartMinute / 10
            } else {
                selectedEndMinute = (selectedEndMinute + 10) % 60
                npMinute.value = selectedEndMinute / 10
            }
        }

        // 새로고침 버튼 (현재 시간으로 리셋)
        tvRefresh.setOnClickListener {
            val now = Calendar.getInstance()
            val nowHour = now.get(Calendar.HOUR)
            val nowMinute = now.get(Calendar.MINUTE)
            val nowAmPm = now.get(Calendar.AM_PM)

            if (isSelectingStartTime) {
                selectedStartAmPm = nowAmPm
                selectedStartHour = if (nowHour == 0) 12 else nowHour
                selectedStartMinute = (nowMinute / 10) * 10

                npAmPm.value = selectedStartAmPm
                npHour.value = selectedStartHour
                npMinute.value = selectedStartMinute / 10
            } else {
                selectedEndAmPm = nowAmPm
                selectedEndHour = if (nowHour == 0) 12 else nowHour
                selectedEndMinute = (nowMinute / 10) * 10

                npAmPm.value = selectedEndAmPm
                npHour.value = selectedEndHour
                npMinute.value = selectedEndMinute / 10
            }
        }

        // 닫기 버튼
        tvClose.setOnClickListener {
            dialog.dismiss()
        }

        // 확인 버튼
        btnConfirm.setOnClickListener {
            // 현재 선택중인 시간 저장
            if (isSelectingStartTime) {
                selectedStartAmPm = npAmPm.value
                selectedStartHour = npHour.value
                selectedStartMinute = npMinute.value * 10
            } else {
                selectedEndAmPm = npAmPm.value
                selectedEndHour = npHour.value
                selectedEndMinute = npMinute.value * 10
            }

            // 24시간 형식으로 변환
            val start24Hour = if (selectedStartAmPm == 0) {
                if (selectedStartHour == 12) 0 else selectedStartHour
            } else {
                if (selectedStartHour == 12) 12 else selectedStartHour + 12
            }

            val end24Hour = if (selectedEndAmPm == 0) {
                if (selectedEndHour == 12) 0 else selectedEndHour
            } else {
                if (selectedEndHour == 12) 12 else selectedEndHour + 12
            }

            // 시작 시간과 종료 시간을 분 단위로 변환하여 비교
            val startTimeInMinutes = start24Hour * 60 + selectedStartMinute
            val endTimeInMinutes = end24Hour * 60 + selectedEndMinute

            // 시작 시간이 종료 시간보다 늦은 경우
            if (startTimeInMinutes >= endTimeInMinutes) {
                Toast.makeText(requireContext(), "종료 시간은 시작 시간보다 늦어야 합니다", Toast.LENGTH_SHORT).show()
                
                // 종료 시간 탭으로 전환하고 시작 시간보다 1시간 뒤로 자동 설정
                isSelectingStartTime = false
                
                // 시작 시간에서 1시간 추가
                val newEndHour = (start24Hour + 1) % 24
                selectedEndAmPm = if (newEndHour < 12) 0 else 1
                selectedEndHour = if (newEndHour % 12 == 0) 12 else newEndHour % 12
                selectedEndMinute = selectedStartMinute
                
                updateTabUI()
                return@setOnClickListener
            }

            val start24Format = String.format("%02d:%02d", start24Hour, selectedStartMinute)
            val end24Format = String.format("%02d:%02d", end24Hour, selectedEndMinute)

            onTimeSelected(start24Format, end24Format)
            dialog.dismiss()
        }

        updateTabUI()
        dialog.show()
    }
}
