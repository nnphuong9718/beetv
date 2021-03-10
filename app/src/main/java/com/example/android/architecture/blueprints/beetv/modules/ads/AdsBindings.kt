package com.example.android.architecture.blueprints.beetv.modules.ads

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.android.architecture.blueprints.beetv.BeeTVApplication.Companion.context
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.SlideAdapter
import com.example.android.architecture.blueprints.beetv.data.models.BAds

@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BAds>?) {
    items?.let {
        (listView.adapter as SlideAdapter).submitList(items)
    }
}

@BindingAdapter("app:load")
fun loadImage(view: ImageView, url : String?) {
    if (url.isNullOrEmpty()){
        view.setImageResource(R.drawable.no_picture)
    }else

    Glide.with(context).load(url).error(R.drawable.no_picture).listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//            holder.progressBar.hide()
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//            holder.progressBar.hide()
            return false
        }

    }).into(view)
}
