package com.example.android.architecture.blueprints.beetv.data.adapter

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.databinding.ItemMenuMovieBinding
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.google.gson.Gson
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class MenuMovieAdapter2(private val viewModel: MenuViewModel,
                        val widthItem: Int, val heightItem: Int) :
        ListAdapter<BMovie, MenuMovieAdapter2.ViewHolder>(MovieDiffCallback()) {

    var onFocusListener: ((View) -> Unit)? = null
    var viewMenuFocus: View? = null
    var lastFocus: Int = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemMenuMovieBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuViewModel, item: BMovie, widthItem: Int, heightItem: Int) {

            binding.viewmodel = viewModel
            item.score = String.format("%.1f", (item.score?:"0").toFloat())
            binding.movie = item
            binding.executePendingBindings()
            binding.main.layoutParams.width = widthItem
            binding.main.layoutParams.height = heightItem

            // update movie label
            val label = DisplayAdaptive.getScriptByLabel(item.billing
                    ?: "", item.position_updated_at)
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
                val binding = ItemMenuMovieBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, widthItem, heightItem)
        holder.binding.main.tag = position
        holder.binding.main.id = 7777 + position
        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            if (k) {
                lastFocus = v.tag as Int
                onFocusListener?.invoke(v)
            }
        }

        // Lock end list only
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusRightId = holder.binding.main.id
        } else {
            holder.binding.main.nextFocusRightId = -1

        }


        val row = ceil((itemCount / 5f).toDouble())
        if (ceil((position / 5f).toDouble()) == row || itemCount <= 5) {
            holder.binding.main.nextFocusDownId = holder.binding.main.id
        } else {
            holder.binding.main.nextFocusDownId = holder.binding.main.id
            Handler().postDelayed({
                holder.binding.main.nextFocusDownId = 7777 + position+5
            },1000)

        }

//        holder.binding.main.nextFocusDownId = 7777 + position+5
        if (position == 0 || position % 5 == 0) {
            holder.binding.main.nextFocusLeftId = (viewModel.mMenuRecyclerView?.adapter as MenuAdapter2).idSelected
                    ?: -1

        } else {
            holder.binding.main.nextFocusLeftId = -1
        }

        if (lastFocus == position) {
            holder.binding.main.requestFocus()
        }


    }
}
