package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.api.School

class SchoolAdapter(
    private val schools: MutableList<School> = mutableListOf(),
    private val onSchoolClicked: (School) -> Unit
) : RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {

    class SchoolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSchoolName: TextView = view.findViewById(R.id.tvSchoolName)
        val tvSchoolAddress: TextView = view.findViewById(R.id.tvSchoolAddress)
        val tvSchoolType: TextView = view.findViewById(R.id.tvSchoolType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_school, parent, false)
        return SchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val school = schools[position]
        holder.tvSchoolName.text = school.schoolName
        holder.tvSchoolAddress.text = school.schoolAddr
        holder.tvSchoolType.text = "${school.establishmentType} ${school.schoolType}"

        holder.itemView.setOnClickListener {
            onSchoolClicked(school)
        }
    }

    override fun getItemCount(): Int = schools.size

    fun updateSchools(newSchools: List<School>) {
        schools.clear()
        schools.addAll(newSchools)
        notifyDataSetChanged()
    }
}