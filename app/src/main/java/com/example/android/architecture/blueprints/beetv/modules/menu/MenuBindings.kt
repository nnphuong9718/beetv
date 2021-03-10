package com.example.android.architecture.blueprints.beetv.modules.menu

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.adapter.*
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BCategory
import com.example.android.architecture.blueprints.beetv.data.models.BMovie


@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BCategory>?) {
    items?.let {
        (listView.adapter as MenuAdapter2).submitList(items)
    }
}

@BindingAdapter("app:movies")
fun setItemsMovie(listView: RecyclerView, movies: List<BMovie>?) {
    movies?.let {
        (listView.adapter as MenuMovieAdapter2).submitList(movies)
    }
}

