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
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.parseTime
import com.example.android.architecture.blueprints.beetv.util.parseTimeLocal
import com.example.android.architecture.blueprints.beetv.util.show
import java.text.SimpleDateFormat
import java.util.*

class LiveTimeAdapter(private val viewModel: MenuViewModel, private val context: Context, private val bLive: BLive) :
        ListAdapter<BLiveTime, LiveTimeAdapter.ViewHolder>(LiveTimeDiffCallback()) {

    var viewFocus: View? = null
    var viewLiveFocus: View? = null
    var viewSelected: View? = null
    var positionSelected = -1
    var viewList = mutableListOf<View>()
    var onFocusListener: ((View, Int, BLiveTime) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemProgramBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuViewModel, item: BLiveTime, live: BLive, context: Context) {
            binding.viewmodel = viewModel
            binding.liveTime = item
            binding.live = live
            binding.executePendingBindings()
            binding.tvTime.text = item.start_time.parseTime()

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemProgramBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item, bLive, context)
        holder.itemView.tag = position to item

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        holder.binding.main.id = 1120 + position

        holder.binding.main.nextFocusLeftId = (viewModel.mLiveRecyclerView as? LiveAdapter2)?.positonRequest
                ?: -1

        if (position == 0) {
            holder.binding.main.nextFocusUpId = 1120 + position
        } else {
            holder.binding.main.nextFocusUpId = -1
        }
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusDownId = 1120 + position
        } else {
            holder.binding.main.nextFocusDownId = -1
        }
        if ((viewModel.mCalendarRecyclerView?.adapter as? CalendarAdapter)?.idSelected ?: -1 > -1) {
            holder.binding.main.nextFocusRightId = (viewModel.mCalendarRecyclerView?.adapter as? CalendarAdapter)!!.idSelected

            Log.d("yenyen", "position $position nextFocusRightId ${(viewModel.mCalendarRecyclerView?.adapter as? CalendarAdapter)!!.idSelected}")
        } else {
            holder.binding.main.nextFocusRightId = -1
            Log.d("yenyen", "position $position nextFocusRightId -1")
        }
        viewList.add(holder.binding.main)
        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
            val now = c.timeInMillis

            if (k) {

                viewFocus = v

                val positionTag = (viewFocus?.tag as Pair<Int, BLiveTime>).first

                if (positionTag == positionSelected) {

                    val childView = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)

                    (childView.getChildAt(0) as TextView).isSelected = true
                    (childView.getChildAt(1) as TextView).isSelected = true
                } else {

                    viewFocus?.setBackgroundResource(R.drawable.border_color_red)

                    val childView = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    (childView.getChildAt(0) as TextView).isSelected = true
                    (childView.getChildAt(1) as TextView).isSelected = true
                }
                viewModel.hideMenu(true)

                onFocusListener?.invoke(v!!, position, item)
            } else {
                val positionTag = (v?.tag as Pair<Int, BLiveTime>).first
                if (positionTag == positionSelected) {
                    if (v is LinearLayout) {
                        val childView = ((v as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))

                        // Todo should disable
                        (childView.getChildAt(0) as TextView).isSelected = false
                        (childView.getChildAt(1) as TextView).isSelected = false
                    }
                    v.setBackgroundColor(Color.parseColor("#33FFFFFF"))


                } else {
                    if (viewFocus is LinearLayout) {
                        val itemLiveTime = (viewFocus?.tag as Pair<Int, BLiveTime>).second
                        val childView = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        if ((now >= itemLiveTime.start_time.parseTimeLocal() && now <= itemLiveTime.end_time.parseTimeLocal()) || itemLiveTime.is_exist) {
                            (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                            (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                        } else {
                            (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.gray))
                            (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.gray))
                        }

                        // Todo should disable
                        (childView.getChildAt(0) as TextView).isSelected = false
                        (childView.getChildAt(1) as TextView).isSelected = false
                    }
                    viewFocus?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))


                }
                viewModel.hideMenu(false)
                Log.d("yenyen2", "nextFocusTopId of focus ${v.nextFocusUpId}")
                viewFocus = null

            }
        }
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))



        val now = c.timeInMillis
        if (now >= item.start_time.parseTimeLocal() && now <= item.end_time.parseTimeLocal()) {
            holder.binding.ivPlaying.show()
            holder.binding.ivPlaying.setImageResource(R.drawable.ic_live_playing)
            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.main.setOnClickListener {
                if (viewSelected is LinearLayout) {
                    val childView = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                    (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))

                }
                viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                positionSelected = position
                if (it is LinearLayout) {
                    val childView = ((it as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))

                    (childView.getChildAt(0) as TextView).isSelected = true
                    (childView.getChildAt(1) as TextView).isSelected = true
                }
                viewSelected = it
                it.setBackgroundResource(R.drawable.bg_white_stroke_red)
                viewModel.openLiveTime(bLive, item)
            }
        } else if (now > item.end_time.parseTimeLocal()) {
            if (item.is_exist) {
                holder.binding.ivPlaying.show()
                holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.ivPlaying.setImageResource(R.drawable.ic_player)
                holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                holder.binding.main.setOnClickListener {
                    if (viewSelected is LinearLayout) {
                        val childView = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))

                    }
                    viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

                    positionSelected = position
                    viewSelected = it
                    if (it is LinearLayout) {
                        val childView = ((it as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        (childView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        (childView.getChildAt(1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))

                    }

                    it.setBackgroundResource(R.drawable.bg_white_stroke_red)
                    viewModel.openLiveTimeWithRecord(bLive, item, item.start_time)
                    Log.d("yenyen4", "time click select ${item.start_time.parseTimeLocal()}")
                }
            } else {
                holder.binding.ivPlaying.hide()
                holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.binding.main.setOnClickListener {
                }
                holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        } else {
            holder.binding.ivPlaying.hide()
            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.binding.tvTime.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.binding.main.setOnClickListener {
            }
            holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }

        val today = simpleDateFormat.format(now)
        val startDate = simpleDateFormat.format(item.start_time.parseTimeLocal())

        if (viewModel.isPlayRecord) {
            val startTimeRecord = simpleDateFormat.format(viewModel.timeStartRecordSelect.parseTimeLocal())
            if (startTimeRecord != startDate) {
                if (today != startDate) {
                    if (position == 0) {
                        positionSelected = -1
                        holder.binding.main.requestFocus()
                    }
                } else
                    if ((now >= item.start_time.parseTimeLocal() && now <= item.end_time.parseTimeLocal())) {
                        holder.binding.main.requestFocus()
                    }


            } else {
                if (viewModel.isLiveLastSelected) {
                    if (viewModel.timeStartRecordSelect.parseTimeLocal() == item.start_time.parseTimeLocal()) {
                        positionSelected = position
                        viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                        holder.binding.main.requestFocus()
                        viewSelected = holder.binding.main
                        viewSelected?.setBackgroundResource(R.drawable.bg_white_stroke_red)
                        Log.d("yenyen4", "focus vao time select record")
                    }
                } else {
                    if ((now >= item.start_time.parseTimeLocal() && now <= item.end_time.parseTimeLocal())) {
                        positionSelected = position
                        viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                        holder.binding.main.requestFocus()
                        viewSelected = holder.binding.main
                        viewSelected?.setBackgroundResource(R.drawable.bg_white_stroke_red)
                    }
                    if (today != startDate) {
                        if (position == 0) {
                            positionSelected = -1
                            holder.binding.main.requestFocus()
                        }
                    }

                }

            }
        } else {
            if ((now >= item.start_time.parseTimeLocal() && now <= item.end_time.parseTimeLocal())) {
                positionSelected = position
                viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                holder.binding.main.requestFocus()
                viewSelected = holder.binding.main
                viewSelected?.setBackgroundResource(R.drawable.bg_white_stroke_red)
            }

            if (today != startDate) {
                if (position == 0) {
                    positionSelected = -1
                    holder.binding.main.requestFocus()
                }
            }
        }

    }
}

class LiveTimeDiffCallback : DiffUtil.ItemCallback<BLiveTime>() {
    override fun areItemsTheSame(oldItem: BLiveTime, newItem: BLiveTime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BLiveTime, newItem: BLiveTime): Boolean {
        return oldItem == newItem
    }
}