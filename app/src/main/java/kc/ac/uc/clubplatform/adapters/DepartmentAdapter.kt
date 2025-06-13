package kc.ac.uc.clubplatform.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.models.Department

class DepartmentAdapter(private val onDepartmentClicked: (Department) -> Unit) : 
    RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder>() {

    private val departments = mutableListOf<Department>()
    private var searchQuery: String = ""

    fun updateDepartments(newDepartments: List<Department>) {
        departments.clear()
        departments.addAll(newDepartments)
        notifyDataSetChanged()
    }
    
    fun setSearchQuery(query: String) {
        searchQuery = query
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_department, parent, false)
        return DepartmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        val department = departments[position]
        holder.bind(department, searchQuery)
    }

    override fun getItemCount(): Int = departments.size

    inner class DepartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDepartmentName: TextView = itemView.findViewById(R.id.tvDepartmentName)

        fun bind(department: Department, query: String) {
            // 학과명과 학부 정보 결합
            val displayText = if (department.facultyName.isNotEmpty()) {
                "${department.majorName} (${department.facultyName})"
            } else {
                department.majorName
            }
            
            // 검색어가 있을 경우 해당 부분 강조
            if (query.isNotEmpty() && displayText.contains(query, ignoreCase = true)) {
                val spannableString = SpannableString(displayText)
                val startIndex = displayText.lowercase().indexOf(query.lowercase())
                val endIndex = startIndex + query.length
                
                if (startIndex >= 0) {
                    spannableString.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.parseColor("#FF4081")), // 핑크 색상으로 강조
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    tvDepartmentName.text = spannableString
                } else {
                    tvDepartmentName.text = displayText
                }
            } else {
                tvDepartmentName.text = displayText
            }
            
            itemView.setOnClickListener {
                onDepartmentClicked(department)
            }
        }
    }
}
