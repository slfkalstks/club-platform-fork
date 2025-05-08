// adapters/NotificationAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemNotificationBinding
import kc.ac.uc.clubplatform.models.Notification

class NotificationAdapter(
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.tvNotificationType.text = notification.type
            binding.tvNotificationMessage.text = notification.message
            binding.tvNotificationDate.text = notification.date

            // 읽지 않은 알림은 배경색 다르게 표시
            if (!notification.isRead) {
                binding.cardView.setCardBackgroundColor(0xFFF5F8FF.toInt())
            } else {
                binding.cardView.setCardBackgroundColor(0xFFFFFFFF.toInt())
            }
        }
    }
}