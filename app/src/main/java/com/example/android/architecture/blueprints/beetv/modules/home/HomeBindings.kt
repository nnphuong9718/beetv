package com.example.android.architecture.blueprints.beetv.modules.home

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.adapter.SlideAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter2
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BMovie


@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BMovie>?) {
    items?.let {
        (listView.adapter as TopMovieAdapter2).submitList(items)
    }
}
