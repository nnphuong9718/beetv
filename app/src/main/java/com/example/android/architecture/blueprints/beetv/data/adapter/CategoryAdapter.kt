package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.Category
import com.example.android.architecture.blueprints.beetv.databinding.ItemChannelBinding
import com.example.android.architecture.blueprints.beetv.databinding.ItemMenuBinding
import com.example.android.architecture.blueprints.beetv.databinding.ItemProgramBinding
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuViewModel
import com.example.android.architecture.blueprints.beetv.util.Constants

class CategoryAdapter(val viewModel: MenuViewModel, val type: Constants.TYPE_MENU, val context: Context) :
        ListAdapter<BCategory, CategoryAdapter.ViewHolder>(MenuDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor( val menuBinding: ItemMenuBinding) :
            RecyclerView.ViewHolder( menuBinding.root) {

        fun bind(viewModel: MenuViewModel, item: BCategory) {
            menuBinding.viewmodel = viewModel
            menuBinding.category = item
            menuBinding.executePendingBindings()



        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                    val binding = ItemMenuBinding.inflate(layoutInflater, parent, false)
                    return ViewHolder(menuBinding = binding)




            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
        holder.itemView.tag = position to item

    }
}