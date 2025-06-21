package kc.ac.uc.clubplatform.models

import java.util.Date

data class Schedule(
    val scheduleId: Int = -1,
    val clubId: Int = -1,
    val title: String,
    val description: String? = null,
    val place: String? = null,
    val startDate: Date,
    val endDate: Date? = null,
    val allDay: Boolean = false,
    val createdBy: Int = -1,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)