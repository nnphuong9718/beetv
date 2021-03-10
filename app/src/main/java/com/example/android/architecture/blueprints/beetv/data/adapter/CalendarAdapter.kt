package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.example.android.architecture.blueprints.beetv.databinding.*
import com.example.android.architecture.blueprints.beetv.modules.home.HomeViewModel
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.modules.movie_detail.MovieDetailViewModel
import com.example.android.architecture.blueprints.beetv.util.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class CalendarAdapter(private val viewModel: MenuViewModel, private val context: Context, private val categoryId: String, private val bLive: BLive) :
        ListAdapter<Long, CalendarAdapter.ViewHolder>(CalendarDiffCallback()) {

    var viewFocus: View? = null
    var viewTimeLiveFocus: View? = null
    var viewSelected: View? = null
    var idSelected: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemCalendarBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuViewModel, item: Long, context: Context) {

            binding.executePendingBindings()
            binding.tvName.text = item.formatDay(context)


        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCalendarBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, context)

        holder.binding.main.id = 2130 + position
        holder.itemView.tag = position
        holder.binding.main.nextFocusRightId = 2130 + position

        if (position == 0) {
            holder.binding.main.nextFocusUpId = 2130 + position
        } else {
            holder.binding.main.nextFocusUpId = -1
        }
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusDownId = 2130 + position
        } else {

            holder.binding.main.nextFocusDownId = -1
        }

        holder.binding.main.nextFocusLeftId = viewTimeLiveFocus?.id ?: (viewModel.mLiveRecyclerView as LiveAdapter2).positonRequest!!
        if (2130 + position == idSelected) {
            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            holder.binding.main.setBackgroundColor(Color.parseColor("#33FFFFFF"))
        } else {
            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.alto))
            holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

        }

        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            if (k) {
                viewFocus = v

                val viewPosition = viewFocus?.tag as Int
                if (viewPosition + 2130 == idSelected) {
                    val childView = (viewFocus as LinearLayout).getChildAt(0) as TextView
                    childView.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)
                } else {
                    viewFocus?.setBackgroundResource(R.drawable.border_color_red)
                }
                viewModel.hideMenu(true)
            } else {
                val viewPosition = v.tag as Int
                if (viewPosition + 2130 == idSelected) {
                    ((v as LinearLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    v.setBackgroundColor(Color.parseColor("#33FFFFFF"))
                } else
                    if (v is LinearLayout) {
                        (v.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                        v.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

                    }
                viewFocus = null
                viewModel.hideMenu(false)

            }
        }


        holder.binding.main.setOnClickListener {

            val viewLast = viewSelected
            if (viewLast is LinearLayout) {
                val childView = (viewLast as LinearLayout).getChildAt(0) as TextView
                childView.setTextColor(ContextCompat.getColor(context, R.color.alto))
            }
            viewLast?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            idSelected = 2130 + it.tag as Int
            viewSelected = it
            val childView = (it as LinearLayout).getChildAt(0) as TextView
            childView.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            it.setBackgroundResource(R.drawable.bg_white_stroke_red)

            viewModel.loadTimeLiveWithDate(categoryId, bLive, item)

        }


        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        simpleDateFormat.timeZone =  TimeZone.getTimeZone("Asia/Seoul")

        if (viewModel.isPlayRecord && viewModel.isLiveLastSelected ) {
            val timeSelected = simpleDateFormat.format(viewModel.timeRecordSelect.parseTimeLocal())
            val timeCurrent = simpleDateFormat.format(item)
            if (timeSelected == timeCurrent && idSelected == -1) {
                idSelected = 2130 + position
                viewSelected = holder.binding.main
                val childView = holder.binding.main.getChildAt(0) as TextView
                childView.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                holder.binding.main.setBackgroundResource(R.drawable.bg_white_stroke_red)
//                if (position == itemCount - 1) {
                    viewModel.refreshLiveTime(viewSelected!!)
//                } else {
//                    viewModel.loadTimeLiveWithDate(categoryId, bLive, item)
//                }
            }
        } else {
            if (idSelected == -1 && position == itemCount - 1) {
                idSelected = 2130 + position
                viewSelected = holder.binding.main
                val childView = holder.binding.main.getChildAt(0) as TextView
                childView.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                holder.binding.main.setBackgroundResource(R.drawable.bg_white_stroke_red)
                viewModel.refreshLiveTime(viewSelected!!)
            }


        }

    }

    fun requestFocusViewSelected(){
        viewSelected?.requestFocus()
    }
}

class CalendarDiffCallback : DiffUtil.ItemCallback<Long>() {
    override fun areItemsTheSame(oldItem: Long, newItem: Long): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Long, newItem: Long): Boolean {
        return oldItem == newItem
    }
}