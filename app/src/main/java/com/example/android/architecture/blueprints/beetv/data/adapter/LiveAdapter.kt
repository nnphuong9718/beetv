package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BLive
import com.example.android.architecture.blueprints.beetv.databinding.ItemChannelBinding
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.widgets.AnimationAutoTextScroller

class LiveAdapter(private val viewModel: MenuViewModel, private val context: Context, val bCategoryID: String) :
        ListAdapter<BLive, LiveAdapter.ViewHolder>(LiveDiffCallback()) {

    var positonRequest: Int? = null
    var viewFocus: View? = null
    var viewMenuFocus: View? = null
    var viewSelected: View? = null

    var mViewList = mutableListOf<View>()
    var onFavoriteClickListener: ((Int, BLive) -> Unit)? = null
    var onFocusListener: ((Int, BLive) -> Unit)? = null

    init {
//        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemChannelBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuViewModel, item: BLive) {
            binding.viewmodel = viewModel
            binding.live = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChannelBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
        holder.itemView.tag = position to item


        holder.binding.main.id = 21110 + position
        holder.binding.main.nextFocusLeftId = (viewModel.mMenuRecyclerView?.adapter as? MenuAdapter2)?.idSelected
                ?: -1

        holder.binding.main.nextFocusRightId = -1

        if (position == 0) {
            holder.binding.main.nextFocusUpId = 21110 + position
        } else {
            holder.binding.main.nextFocusUpId = -1
        }
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusDownId = 21110 + position
        } else {
            holder.binding.main.nextFocusDownId = -1
        }

        if (item.description?.startsWith("방송중") == true) {
            holder.binding.tvContent.text = item.description.substring(4).trim()
        } else holder.binding.tvContent.text = "item.description aaaaaaa"
        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            if (k) {
                viewFocus = v
                val viewPosition = (v.tag as Pair<Int, BLive>).first
                val viewChildMain = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                if (21110 + viewPosition == positonRequest) {

                    if (viewChildMain.getChildAt(0) is LinearLayout) {
                        val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                        (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                        (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    }
                    viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)
                } else
                    viewFocus?.setBackgroundResource(R.drawable.border_color_red)
                if (viewChildMain.getChildAt(0) is LinearLayout) {
                    val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                    startAnimation(320,((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView))
                 }
                onFocusListener?.invoke(position, item)

                viewModel.hideMenu(true)
            } else {
                val viewPosition = (v.tag as Pair<Int, BLive>).first
                if (21110 + viewPosition == positonRequest) {
                    val viewChildMain = ((v as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                    if (viewChildMain.getChildAt(0) is LinearLayout) {
                        val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                        (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                        (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                        ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).clearAnimation()
                    }

                    v?.setBackgroundColor(Color.parseColor("#33FFFFFF"))
                } else {
                    if (v is LinearLayout) {
                        val viewChildMain = ((v as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                        if (viewChildMain.getChildAt(0) is LinearLayout) {
                            val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                            (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                            val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                            (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                            ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                            ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).clearAnimation()
                        }

                    }

                    v?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                }
                viewModel.hideMenu(false)

                viewFocus = null
            }
        }
        mViewList.add(holder.binding.main)

        if (positonRequest == position + 21110) {
            viewSelected = holder.binding.main
            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            holder.binding.tvId.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            holder.binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))

            holder.binding.main.setBackgroundColor(Color.parseColor("#33FFFFFF"))
            (viewModel.mMenuRecyclerView!!.adapter as MenuAdapter2).viewSelected?.nextFocusRightId = viewSelected?.id
                    ?: -1
            if (viewModel.isPlayRecord && viewModel.isLiveLastSelected) {
                if (positonRequest != null)
                    viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, positonRequest!! - 21110)
                else viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, 0)
            }
            if (!viewModel.isPlayRecord) {
                viewSelected?.requestFocus()
            }
        } else {

            holder.binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.alto))
            holder.binding.tvId.setTextColor(ContextCompat.getColor(context, R.color.alto))
            holder.binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.alto))

            holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

        }

        holder.itemView.setOnLongClickListener {
            onFavoriteClickListener?.invoke(position, item)
            true
        }


        holder.binding.main.setOnClickListener {
            positonRequest = 21110 + (it.tag as Pair<Int, BLive>).first
            if (viewSelected is LinearLayout) {
                val viewChildMain = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                if (viewChildMain.getChildAt(0) is LinearLayout) {
                    val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                    (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                    val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                    (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                    ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                }
                viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

            }
            viewSelected = it
            val viewChildMain = ((viewSelected as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
            if (viewChildMain.getChildAt(0) is LinearLayout) {
                val viewChildOne = viewChildMain.getChildAt(0) as LinearLayout
                (viewChildOne.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                (viewChildTwo.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            }
            viewFocus?.setBackgroundResource(R.drawable.bg_white_stroke_red)

//            viewModel.loadTimeLive(bCategoryID, item, viewSelected!!, position)
            viewModel.openCategory(item)
        }


    }

    fun updateFavorite(position: Int, isFavorite: Boolean) {
        currentList[position].is_favorite = isFavorite
        getItem(position).is_favorite = isFavorite
        notifyItemChanged(position)
    }

    fun startAnimation(witdh : Int,v: TextView) {
        var textWidth = v.paint.measureText(v.text.toString()).toInt()
        Log.d("textWidth", textWidth.toString())
        if (textWidth < witdh) textWidth = witdh
        val textscroller = AnimationAutoTextScroller(v, witdh.toFloat(), textWidth*20 ,
                object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        if (viewFocus is LinearLayout) {
                            val viewChildMain = ((viewFocus as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
                            if (viewChildMain.getChildAt(0) is LinearLayout) {
                                  val viewChildTwo = viewChildMain.getChildAt(1) as LinearLayout
                                ((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView).clearAnimation()
                                startAnimation( witdh,((viewChildTwo.getChildAt(1) as HorizontalScrollView).getChildAt(0) as TextView))
                            }

                        }
                    }

                    override fun onAnimationStart(p0: Animation?) {

                    }
                }, v.paint.measureText(v.text.toString()).toInt(), true)
        textscroller.setScrollingText(v.text.toString())
        textscroller.start()
    }

}


class LiveDiffCallback : DiffUtil.ItemCallback<BLive>() {
    override fun areItemsTheSame(oldItem: BLive, newItem: BLive): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BLive, newItem: BLive): Boolean {
        return oldItem == newItem
    }
}