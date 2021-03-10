package com.example.android.architecture.blueprints.beetv.data.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import com.example.android.architecture.blueprints.beetv.util.formatOffset
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import org.xmlpull.v1.XmlPullParser
import java.util.*

class TimeZoneAdapter(val context: Context, val list: MutableList<TimeZone>, val nameList: MutableList<String?>) : RecyclerView.Adapter<TimeZoneAdapter.ViewHolder>() {

    var onItemClickListener: ((TimeZone, String?) -> Unit)? = null
    var mViewList = arrayListOf<View>()
    var viewFocus: View? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timezone, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list.get(position)
        val name = nameList.get(position)
        holder.tvTimeZone.text = name
        holder.itemView.tag = item
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item, name)
        }
        holder.itemView.onFocusChangeListener = View.OnFocusChangeListener { v, k ->

            if (k) {

                if (viewFocus is MetroItemFrameLayout) {
                    (viewFocus as MetroItemFrameLayout).setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    if ((viewFocus as MetroItemFrameLayout).getChildAt(0) is TextView) {
                        ((viewFocus as MetroItemFrameLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }

                viewFocus = v
                if (viewFocus is MetroItemFrameLayout) {
                    (viewFocus as MetroItemFrameLayout).setBackgroundColor(ContextCompat.getColor(context, R.color.button_text_focus))
                    if ((viewFocus as MetroItemFrameLayout).getChildAt(0) is TextView) {
                        ((viewFocus as MetroItemFrameLayout).getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                }

            }

        }
        mViewList.add(holder.itemView)

        if (item == TimeZoneManager.getInstance().getData()){
            holder.itemView.requestFocus()
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTimeZone = view.findViewById<TextView>(R.id.tv_time)
        val main = view.findViewById<MetroItemFrameLayout>(R.id.main)
    }
}