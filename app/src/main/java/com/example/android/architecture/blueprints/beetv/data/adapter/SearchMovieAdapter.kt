package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.Movie
import com.example.android.architecture.blueprints.beetv.databinding.ItemSearchMovieBinding
import com.example.android.architecture.blueprints.beetv.databinding.ItemSlideBinding
import com.example.android.architecture.blueprints.beetv.modules.ads.AdsViewModel
import com.example.android.architecture.blueprints.beetv.modules.home.HomeViewModel
import com.example.android.architecture.blueprints.beetv.modules.search.SearchViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import java.util.*
import kotlin.math.ceil

class SearchMovieAdapter(private val viewModel: SearchViewModel,
                         val widthItem: Int, val heightItem: Int) :
        ListAdapter<BMovie, SearchMovieAdapter.ViewHolder>(MovieDiffCallback()) {


    private var mLastFocus: Int = -1

    var onFocusListener: ((View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemSearchMovieBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: SearchViewModel, item: BMovie, widthItem: Int, heightItem: Int) {

            binding.viewmodel = viewModel
            item.score = String.format("%.1f", (item.score?:"0").toFloat())
            binding.movie = item
            binding.executePendingBindings()
            binding.main.layoutParams.width = widthItem
            binding.main.layoutParams.height = heightItem

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.billing, item.position_updated_at.toLong())
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
                val binding = ItemSearchMovieBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.main.tag = position
        holder.binding.main.id = 1999999+position
        holder.bind(viewModel, item, widthItem, heightItem)


        val row = ceil((itemCount / 4f).toDouble())



        if (ceil((position / 4f).toDouble()) == row || itemCount <= 4) {
            holder.binding.main.nextFocusDownId = holder.binding.main.id
        } else {
            holder.binding.main.nextFocusDownId = -1
        }



        holder.binding.main.setOnFocusChangeListener { view, b ->
            if (b){
                mLastFocus = view.tag as Int
                onFocusListener?.invoke(view)
            }
        }
        if (mLastFocus == position){
            holder.binding.main.requestFocus()
        }
    }
}

