// models/Schedule.kt
package kc.ac.uc.clubplatform.models

import java.util.Date

data class Schedule(
    val id: Int,
    val title: String,
    val place: String,
    val date: Date,
    val time: String,
    val content: String = ""
)