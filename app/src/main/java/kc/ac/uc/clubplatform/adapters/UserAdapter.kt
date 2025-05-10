package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.models.User

class UserAdapter(
    private var users: List<User>,
    private val onUserClickListener: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var selectedUser: User? = null
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, position == selectedPosition)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        selectedUser = null
        selectedPosition = -1
        notifyDataSetChanged()
    }
    
    fun getSelectedUser(): User? = selectedUser

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: ConstraintLayout = itemView.findViewById(R.id.containerUser)
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStudentId: TextView = itemView.findViewById(R.id.tvStudentId)

        fun bind(user: User, isSelected: Boolean) {
            tvName.text = user.name
            tvStudentId.text = user.studentId
            ivProfile.setImageResource(user.profileImage)
            
            // 선택된 아이템 배경색 변경
            if (isSelected) {
                container.setBackgroundResource(R.color.colorSelectedItem)
            } else {
                container.background = null
            }

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                selectedUser = user
                
                // 이전에 선택된 항목과 새로 선택된 항목 갱신
                if (oldPosition != -1) notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                
                onUserClickListener(user)
            }
        }
    }
}
