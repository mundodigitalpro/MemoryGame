package com.josejordan.memorygame
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.josejordan.memorygame.databinding.MemoryCardItemBinding

class MemoryCardAdapter(
    private val memoryCards: List<MemoryCard>,
    private val clickListener: (Int) -> Unit
) : RecyclerView.Adapter<MemoryCardAdapter.MemoryCardViewHolder>() {

    inner class MemoryCardViewHolder(private val binding: MemoryCardItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(memoryCard: MemoryCard, clickListener: (Int) -> Unit) {
            binding.ivMemoryCardImage.setImageResource(
                if (memoryCard.isFaceUp) memoryCard.imageResource else R.drawable.card_back
            )
            binding.ivMemoryCardImage.setOnClickListener { clickListener(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryCardViewHolder {
        val binding = MemoryCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoryCardViewHolder, position: Int) {
        holder.bind(memoryCards[position], clickListener)
    }

    override fun getItemCount(): Int = memoryCards.size
}