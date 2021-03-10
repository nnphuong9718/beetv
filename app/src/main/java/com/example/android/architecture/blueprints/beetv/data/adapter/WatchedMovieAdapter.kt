package com.example.android.architecture.blueprints.beetv.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite
import com.example.android.architecture.blueprints.beetv.databinding.ItemFavoriteMovieBinding
import com.example.android.architecture.blueprints.beetv.modules.search.SearchViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import java.util.*

class FavoriteMovieAdapter(private val viewModel: SearchViewModel,
                           val widthItem: Int, val heightItem: Int) :
        ListAdapter<BFavorite, FavoriteMovieAdapter.ViewHolder>(FavoriteDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemFavoriteMovieBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: SearchViewModel, item: BFavorite, widthItem: Int, heightItem: Int) {

            binding.viewmodel = viewModel
            item.movie?.score = String.format("%.1f", (item.movie?.score ?: "0").toFloat())
            binding.favorite = item
            binding.executePendingBindings()
            binding.main.layoutParams.width = widthItem
            binding.main.layoutParams.height = heightItem

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.movie?.billing,item.position_updated_at.toLong())
            if (label.isNotBlank()) {
                binding.tvTop.setText(label)
                binding.tvTop.setBkgColor(DisplayAdaptive.getColorByLabel(label))
                binding.tvTop.show()

            } else {
                binding.tvTop.hide()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFavoriteMovieBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, widthItem, heightItem)

    }
}

class FavoriteDiffCallback : DiffUtil.ItemCallback<BFavorite>() {
    override fun areItemsTheSame(oldItem: BFavorite, newItem: BFavorite): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BFavorite, newItem: BFavorite): Boolean {
        return oldItem == newItem
    }
}
