package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
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
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.Category
import com.example.android.architecture.blueprints.beetv.databinding.ItemMenuBinding
import com.example.android.architecture.blueprints.beetv.databinding.ItemTopMovieBinding
import com.example.android.architecture.blueprints.beetv.manager.LiveManager
import com.example.android.architecture.blueprints.beetv.modules.home.HomeViewModel
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.google.gson.Gson

class MenuAdapter2(private val viewModel: MenuViewModel, private val context: Context) :
        ListAdapter<BCategory, MenuAdapter2.ViewHolder>(MenuDiffCallback()) {

    //    var positionFocus = 0
    var onFocusListener: ((Int, BCategory, View) -> Unit)? = null

    var viewFocus: View? = null
    var viewSelected: ViewGroup? = null
    var idSelected: Int? = null
    var type = Constants.TYPE_CATEGORY.MOVIE.type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ItemMenuBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: MenuViewModel, item: BCategory) {
            binding.viewmodel = viewModel
            binding.category = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMenuBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
        holder.binding.main.tag = position to item
        holder.binding.main.id = 23456 + position

        holder.binding.main.nextFocusLeftId = holder.binding.main.id
        if (position == itemCount - 1) {
            holder.binding.main.nextFocusDownId = holder.binding.main.id
        } else {
            holder.binding.main.nextFocusDownId = -1
        }
        if (position == 0 && type == Constants.TYPE_CATEGORY.TV.type) {
            holder.binding.main.nextFocusUpId = holder.binding.main.id
            holder.binding.tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_scrap, 0, 0, 0)
        } else {
            holder.binding.main.nextFocusUpId = -1

            holder.binding.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }



        holder.binding.main.onFocusChangeListener = View.OnFocusChangeListener { v, k ->
            val category = (v.tag as Pair<Int, BCategory>).second
            if (k) {
                viewFocus = v
                val positionView = (v.tag as Pair<Int, BCategory>).first
                if (type == Constants.TYPE_CATEGORY.TV.type)
                    viewFocus?.setBackgroundResource(R.drawable.border_color_red)
                else {
                    viewFocus?.setBackgroundResource(R.drawable.border_color_red)
                    if (positionView + 23456 == idSelected) {
                        ((viewFocus as? LinearLayout)?.getChildAt(0) as? TextView)?.setTextColor(
                                ContextCompat.getColor(context, R.color.sunsetOrange)
                        )
                    } else {
                        ((viewFocus as? LinearLayout)?.getChildAt(0) as? TextView)?.setTextColor(
                                ContextCompat.getColor(context, R.color.alto)
                        )
                    }
                }
                viewModel.hideMenu(true)
                if (type == Constants.TYPE_CATEGORY.TV.type && viewSelected != null)
                    onFocusListener?.invoke(position, item, viewSelected!!)

            } else {

                val positionView = (v.tag as Pair<Int, BCategory>).first
                if (positionView + 23456 == idSelected) {
                    if (type == Constants.TYPE_CATEGORY.TV.type) {
                        v?.setBackgroundColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    } else {
                        v?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                        ((viewFocus as? LinearLayout)?.getChildAt(0) as? TextView)?.setTextColor(
                                ContextCompat.getColor(context, R.color.sunsetOrange)
                        )
                    }

                } else {
                    v?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    ((v as? LinearLayout)?.getChildAt(0) as? TextView)?.setTextColor(
                            ContextCompat.getColor(context, R.color.alto)
                    )
                }
                viewFocus = null
                viewModel.hideMenu(false)

            }
        }

        if (type == Constants.TYPE_CATEGORY.TV.type) {
            val categorySelected = LiveManager.getInstance().getData().first
            if ((categorySelected == null && position == 0) || (categorySelected != null && categorySelected.id == item.id)) {
                viewSelected = holder.binding.main
                idSelected = 23456 + position
                holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, R.color.sunsetOrange))

                viewModel.loadLive(item)
                holder.binding.main.requestFocus()

            } else {
                holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        } else {
            if (position == 0) {
                if (idSelected == null) {
                    viewSelected = holder.binding.main
                    idSelected = 23456 + position
                    holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    if (holder.binding.main.getChildAt(0) is TextView) {
                        (holder.binding.main.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
                    }
                    viewModel.loadMovieByCategory(position, item)

                }
            } else {
                holder.binding.main.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                if (holder.binding.main.getChildAt(0) is TextView) {
                    (holder.binding.main.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
                }
            }

        }

        holder.binding.main.setOnClickListener {
            viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            if (viewSelected?.getChildAt(0) is TextView && type != Constants.TYPE_CATEGORY.TV.type) {
                (viewSelected?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.alto))
            }

            viewSelected = it as ViewGroup?
            idSelected = 23456 + (it.tag as Pair<Int, BCategory>).first
            viewSelected?.setBackgroundColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            if (holder.binding.main.getChildAt(0) is TextView && type != Constants.TYPE_CATEGORY.TV.type) {
//                viewSelected?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                viewFocus?.setBackgroundResource(R.drawable.border_color_red)
                (holder.binding.main.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.sunsetOrange))
            }
            if (type == Constants.TYPE_CATEGORY.TV.type) {
                viewModel.loadLive(item)
            } else {
                viewModel.loadMovieByCategory(position, item)
            }
        }
    }
}

class MenuDiffCallback : DiffUtil.ItemCallback<BCategory>() {
    override fun areItemsTheSame(oldItem: BCategory, newItem: BCategory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BCategory, newItem: BCategory): Boolean {
        return oldItem == newItem
    }
}