package com.example.android.architecture.blueprints.beetv.modules.movie_detail

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.data.adapter.ChapterAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.SlideAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter
import com.example.android.architecture.blueprints.beetv.data.adapter.TopMovieAdapter2
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.BVideo


@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BVideo>?) {
    items?.let {
        (listView.adapter as ChapterAdapter).submitList(items)
    }
}
