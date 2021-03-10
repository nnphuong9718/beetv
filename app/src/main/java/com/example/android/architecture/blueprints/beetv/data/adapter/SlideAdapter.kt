package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.Movie
import com.example.android.architecture.blueprints.beetv.data.models.Slide
import com.example.android.architecture.blueprints.beetv.databinding.ItemSlideBinding
import com.example.android.architecture.blueprints.beetv.modules.ads.AdsViewModel
import com.example.android.architecture.blueprints.beetv.util.hide

class SlideAdapter(private val viewModel: AdsViewModel) :
        ListAdapter<BAds, SlideAdapter.ViewHolder>(AdsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemSlideBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: AdsViewModel, item: BAds) {

            binding.viewmodel = viewModel
            binding.ads = item
            binding.executePendingBindings()
            itemView.tag = adapterPosition
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSlideBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel,item)
    }

}

class AdsDiffCallback : DiffUtil.ItemCallback<BAds>() {
    override fun areItemsTheSame(oldItem: BAds, newItem: BAds): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BAds, newItem: BAds): Boolean {
        return oldItem == newItem
    }
}
