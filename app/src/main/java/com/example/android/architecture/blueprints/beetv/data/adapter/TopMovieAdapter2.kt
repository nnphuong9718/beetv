package com.example.android.architecture.blueprints.beetv.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.databinding.ItemTopMovieBinding
import com.example.android.architecture.blueprints.beetv.modules.home.HomeViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import java.util.*

class TopMovieAdapter2(private val viewModel: HomeViewModel,
                       val widthItem: Int, val heightItem: Int) :
        ListAdapter<BMovie, TopMovieAdapter2.ViewHolder>(MovieDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemTopMovieBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: HomeViewModel, item: BMovie, widthItem: Int, heightItem: Int) {

            binding.viewmodel = viewModel
            item.score = String.format("%.1f", (item.score?:"0").toFloat())
            binding.movie = item
            binding.executePendingBindings()
            binding.main.layoutParams.width = widthItem
            binding.main.layoutParams.height = heightItem

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.billing,item.position_updated_at.toLong())

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
                val binding = ItemTopMovieBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, widthItem, heightItem)
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusRightId = holder.binding.main.id
        }
        if (position == 0) {
            holder.binding.main.nextFocusLeftId = holder.binding.main.id
        }
    }
}

class MovieDiffCallback : DiffUtil.ItemCallback<BMovie>() {
    override fun areItemsTheSame(oldItem: BMovie, newItem: BMovie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BMovie, newItem: BMovie): Boolean {
        return oldItem == newItem
    }
}
