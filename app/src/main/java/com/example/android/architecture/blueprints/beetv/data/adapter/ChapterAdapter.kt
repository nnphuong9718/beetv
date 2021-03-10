package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BVideo
import com.example.android.architecture.blueprints.beetv.databinding.ItemChapterBinding
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import java.util.*

class ChapterAdapter(private val context: Context, private val viewModel: MovieDetailViewModel) :
        ListAdapter<BVideo, ChapterAdapter.ViewHolder>(VideoDiffCallback()) {

    var positionRequest = -1
    var viewFocus: View? = null

    interface IChapterFocusListener {
        fun onChapterFocus(position: Int, video: BVideo)
    }

    var mFocusListener: IChapterFocusListener? = null

    fun setFocusListener(listener: IChapterFocusListener) {
        mFocusListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, position, mFocusListener)
        holder.itemView.tag = position to item
        holder.itemView.id = 55 + position


        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            if (k) {
                viewFocus?.background = ContextCompat.getDrawable(context, R.drawable.bg_chapter)
                viewFocus = v
                viewFocus?.setBackgroundColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                mFocusListener?.onChapterFocus(position, item)
            }else{
                viewFocus?.background = ContextCompat.getDrawable(context, R.drawable.bg_chapter)
            }
        }

        holder.binding.main.nextFocusLeftId = holder.binding.main.id
        if (positionRequest == position) {
            holder.itemView.requestFocus()
        }
        if (position == itemCount -1){
            holder.binding.main.nextFocusDownId = holder.binding.main.id
        }
    }

    class ViewHolder private constructor(val binding: ItemChapterBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MovieDetailViewModel, item: BVideo, position: Int, focusListener: IChapterFocusListener?) {
            binding.viewmodel = viewModel
            binding.video = item

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel("", item.created_at_unix_timestamp.toLong())
            if (label.isNotBlank()) {
                binding.tvTop.setText(label)
                binding.tvTop.setBkgColor(DisplayAdaptive.getColorByLabel(label))
                binding.tvTop.show()
            } else {
                binding.tvTop.hide()
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChapterBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class VideoDiffCallback : DiffUtil.ItemCallback<BVideo>() {
    override fun areItemsTheSame(oldItem: BVideo, newItem: BVideo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BVideo, newItem: BVideo): Boolean {
        return oldItem == newItem
    }
}