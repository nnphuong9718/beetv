package com.example.android.architecture.blueprints.beetv.modules.search

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.adapter.*
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite
import com.example.android.architecture.blueprints.beetv.data.models.BMovie



@BindingAdapter("app:search")
fun setItemsSearch(listView: RecyclerView, itemSearch: List<BMovie>?) {
    itemSearch?.let {
        (listView.adapter as SearchMovieAdapter).submitList(itemSearch)
    }
}

@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BFavorite>?) {
    items?.let {
        (listView.adapter as FavoriteMovieAdapter).submitList(items)
    }
}
