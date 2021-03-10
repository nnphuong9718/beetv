package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.Movie
import com.example.android.architecture.blueprints.beetv.util.DisplayAdaptive
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.show
import com.example.android.architecture.blueprints.beetv.widgets.metro.CornerVew
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout

class TopMovieAdapter(private val movieList: MutableList<Movie>, val context : Context,
                      val widthItem : Int, val heightItem : Int) : RecyclerView.Adapter<TopMovieAdapter.ViewHolder>() {
    var mOnItemClickListener : ((Movie) -> Unit) ?= null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_top_movie, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = movieList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = movieList.get(position)
        Glide.with(context).load(item.coverImage).into(holder.ivIcon)
        holder.tvName.text = item.name
        holder.main.layoutParams.width = widthItem
        holder.main.layoutParams.height = heightItem
        holder.itemView.setOnClickListener { mOnItemClickListener?.invoke(item) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon = view.findViewById<ImageView>(R.id.iv_cover)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val main = view.findViewById<MetroItemFrameLayout>(R.id.main)
    }
}