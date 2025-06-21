package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.models.User

class UserAdapter(
    private var users: List<User>,
    private val onUserSelected: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val selectedUsers = mutableListOf<User>()
    private var isMultipleSelectionMode = false
    
    fun setSelectionMode(multipleSelection: Boolean) {
        isMultipleSelectionMode = multipleSelection
        selectedUsers.clear()
        notifyDataSetChanged()
    }
    
    fun getSelectedUser(): User? {
        return selectedUsers.firstOrNull()
    }
    
    fun getSelectedUsers(): List<User> {
        return selectedUsers.toList()
    }

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStudentId: TextView = itemView.findViewById(R.id.tvStudentId)
        private val cbSelected: CheckBox = itemView.findViewById(R.id.cbSelected)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = users[position]
                    
                    if (isMultipleSelectionMode) {
                        if (selectedUsers.contains(user)) {
                            selectedUsers.remove(user)
                            cbSelected.isChecked = false
                        } else {
                            selectedUsers.add(user)
                            cbSelected.isChecked = true
                        }
                    } else {
                        // 단일 선택 모드
                        selectedUsers.clear()
                        selectedUsers.add(user)
                        notifyDataSetChanged()
                    }
                    
                    onUserSelected(user)
                }
            }
        }

        fun bind(user: User) {
            tvName.text = user.name
            tvStudentId.text = user.studentId
            ivProfile.setImageResource(user.profileImage ?: R.drawable.default_profile)
            
            cbSelected.visibility = if (isMultipleSelectionMode) View.VISIBLE else View.GONE
            cbSelected.isChecked = selectedUsers.contains(user)
        }
    }
}
