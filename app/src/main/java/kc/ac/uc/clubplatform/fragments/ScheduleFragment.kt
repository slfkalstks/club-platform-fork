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
import androidx.wear.compose.material.dialog.Dialog
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.databinding.FragmentScheduleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import ru.cleverpumpkin.calendar.CalendarDate
import ru.cleverpumpkin.calendar.CalendarView.SelectionMode
import ru.cleverpumpkin.calendar.CalendarView.DateIndicator
import java.text.SimpleDateFormat
import java.util.*

data class ScheduleData(
    val scheduleId: Int,
    val clubId: Int,
    val title: String,
    val description: String?,
    val startDate: Date,
    val endDate: Date?,
    val allDay: Boolean,
    val createdBy: Int
)

// DateIndicator 구현체로 점 표시
data class MyIndicator(
    override val date: CalendarDate,
    override val color: Int
) : DateIndicator

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleList = mutableListOf<ScheduleData>()
    private var selectedDate: CalendarDate? = null
    private val indicatorList = mutableListOf<MyIndicator>()

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
        fetchSchedulesFromApi()
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
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_schedule, null, false)

            val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
            val etStartDate = dialogView.findViewById<EditText>(R.id.etStartDate)
            val etEndDate = dialogView.findViewById<EditText>(R.id.etEndDate)
            val etTime = dialogView.findViewById<EditText>(R.id.etTime)
            val etContent = dialogView.findViewById<EditText>(R.id.etContent)

            // 선택된 날짜 범위를 가져와서 시작일과 종료일 필드에 설정
            val (startDate, endDate) = getSelectedDateRange()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            if (startDate != null) {
                etStartDate.setText(dateFormat.format(startDate))
                // 종료일이 없으면(단일 날짜 선택) 시작일과 동일하게 설정
                val finalEndDate = endDate ?: startDate
                etEndDate.setText(dateFormat.format(finalEndDate))
            }

            AlertDialog.Builder(requireContext())
                .setTitle("일정 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val title = etTitle.text.toString()
                    val startDateStr = etStartDate.text.toString()
                    val endDateStr = etEndDate.text.toString()
                    val timeStr = etTime.text.toString()
                    val content = etContent.text.toString()

                    if (title.isBlank() || startDateStr.isBlank() || endDateStr.isBlank()) {
                        Toast.makeText(requireContext(), "제목과 날짜를 입력하세요.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val timeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                    val startDate: Date = try {
                        if (timeStr.isNotBlank()) {
                            // 시간이 있으면 시간 포함해서 파싱
                            val startTime = if (timeStr.contains("~")) {
                                timeStr.split("~")[0].trim()
                            } else {
                                timeStr
                            }
                            timeSdf.parse("$startDateStr $startTime") ?: sdf.parse(startDateStr) ?: Date()
                        } else {
                            sdf.parse(startDateStr) ?: Date()
                        }
                    } catch (e: Exception) {
                        try {
                            sdf.parse(startDateStr) ?: Date()
                        } catch (e2: Exception) {
                            Date()
                        }
                    }

                    val endDate: Date = try {
                        if (timeStr.isNotBlank()) {
                            // 시간이 있으면 시간 포함해서 파싱
                            val endTime = if (timeStr.contains("~")) {
                                timeStr.split("~")[1].trim()
                            } else {
                                timeStr
                            }
                            timeSdf.parse("$endDateStr $endTime") ?: sdf.parse(endDateStr) ?: Date()
                        } else {
                            sdf.parse(endDateStr) ?: Date()
                        }
                    } catch (e: Exception) {
                        try {
                            sdf.parse(endDateStr) ?: Date()
                        } catch (e2: Exception) {
                            Date()
                        }
                    }

                    val schedule = ScheduleData(
                        scheduleId = -1,
                        clubId = -1,
                        title = title,
                        description = content,
                        startDate = startDate,
                        endDate = endDate,
                        allDay = false,
                        createdBy = -1
                    )
                    scheduleList.add(schedule)
                    // 인디케이터(점) 추가
                    indicatorList.add(MyIndicator(CalendarDate(startDate), 0xFF2196F3.toInt()))
                    binding.crunchyCalendarView.datesIndicators = indicatorList
                    selectedDate?.let { filterSchedulesByDate(it) }
                    Toast.makeText(requireContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null)
                .show()
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

    private fun fetchSchedulesFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = java.net.URL("https://hide-ipv4.xyz/api/schedule")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val responseCode = conn.responseCode
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonArr = JSONArray(response)
                    val tempList = mutableListOf<ScheduleData>()
                    val tempIndicators = mutableListOf<MyIndicator>()

                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        val schedule = ScheduleData(
                            scheduleId = obj.getInt("schedule_id"),
                            clubId = obj.getInt("club_id"),
                            title = obj.getString("title"),
                            description = if (obj.isNull("description")) null else obj.getString("description"),
                            startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(obj.getString("start_date")) ?: Date(),
                            endDate = if (obj.isNull("end_date")) null else SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(obj.getString("end_date")),
                            allDay = obj.optBoolean("all_day", false),
                            createdBy = obj.getInt("created_by")
                        )
                        tempList.add(schedule)
                        // 시작~종료 범위 모두 점 표시
                        val cal = Calendar.getInstance()
                        cal.time = schedule.startDate
                        val end = schedule.endDate ?: schedule.startDate
                        while (!cal.time.after(end)) {
                            tempIndicators.add(MyIndicator(CalendarDate(cal.time), 0xFF2196F3.toInt()))
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
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "일정 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
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

        val filtered = scheduleList.filter {
            it.startDate >= startOfDay && it.startDate < endOfDay
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        for (item in filtered) {
            sb.append("제목: ${item.title}\n")
            sb.append("설명: ${item.description ?: ""}\n")
            sb.append("시작: ${sdf.format(item.startDate)}\n")
            sb.append("종료: ${item.endDate?.let { sdf.format(it) } ?: ""}\n")
            sb.append("하루종일: ${if (item.allDay) "예" else "아니오"}\n")
            sb.append("\n")
        }
        binding.tvScheduleList.text = sb.toString().trim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTimePickerDialog(onTimeSelected: (String, String) -> Unit) {
        val dialog = Dialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_time_picker, null)
        dialog.setContentView(dialogView)

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

            val start24Format = String.format("%02d:%02d", start24Hour, selectedStartMinute)
            val end24Format = String.format("%02d:%02d", end24Hour, selectedEndMinute)

            onTimeSelected(start24Format, end24Format)
            dialog.dismiss()
        }

        updateTabUI()
        dialog.show()
    }
}