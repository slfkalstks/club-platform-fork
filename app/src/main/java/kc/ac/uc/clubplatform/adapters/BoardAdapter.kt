// adapters/BoardAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemBoardBinding
import kc.ac.uc.clubplatform.models.Board

class BoardAdapter(
    private val boards: List<Board>,
    private val onItemClick: (Board) -> Unit
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ItemBoardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(boards[position])
    }

    override fun getItemCount() = boards.size

    inner class BoardViewHolder(private val binding: ItemBoardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(board: Board) {
            binding.tvBoardName.text = board.name
            binding.tvBoardDescription.text = board.description

            binding.root.setOnClickListener {
                onItemClick(board)
            }
        }
    }
}