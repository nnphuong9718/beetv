package com.example.android.architecture.blueprints.beetv.modules.dialogs

import android.app.Dialog
import android.os.Bundle
import android.system.Os.open
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.adapter.TimeZoneAdapter
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import com.example.android.architecture.blueprints.beetv.widgets.metro.DrawingOrderRelativeLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import org.xml.sax.XMLReader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.*
import kotlin.Comparator

class TimeZonesDialog : DialogFragment() {
    var onTimeZoneSelectedListener : ((TimeZone) -> Unit) ?= null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_timezones)
        val rvTimeZones = dialog.findViewById<RecyclerView>(R.id.rv_timezone)
        val main = dialog.findViewById<DrawingOrderRelativeLayout>(R.id.main)
        val ids = ArrayList<String?>()
        val names = mutableListOf<String?>()
        names.add(BeeTVApplication.context.resources.getString(R.string.sytem_time))
        try {
            val parser = resources.getXml(R.xml.timezone)
            var timezoneId: String? = null
            var timezoneName: String? = null
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    timezoneId = parser.getAttributeValue(null, "id")
                    if (timezoneId != null) {
                        ids.add(timezoneId)
                    }
                } else if (event == XmlPullParser.TEXT) {
                    timezoneName = parser.text
                    names.add(timezoneName)
                }
                event = parser.next()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }

        val zones = mutableListOf<TimeZone>()
        for (id : String? in ids) {
            zones.add(TimeZone.getTimeZone(id));
        }
        zones.sortWith(Comparator { timeZone: TimeZone, timeZone1: TimeZone -> timeZone.rawOffset - timeZone1.rawOffset })
        zones.add(0, TimeZone.getDefault())
        val adapter = TimeZoneAdapter(requireContext(), zones, names)
        rvTimeZones.adapter = adapter
        adapter.onItemClickListener = { timeZone: TimeZone, name: String? ->
            TimeZoneManager.getInstance().setData(timeZone, name)
            onTimeZoneSelectedListener?.invoke(timeZone)
            dismiss()
        }


        rvTimeZones.scrollToPosition(zones.indexOf(TimeZoneManager.getInstance().getData()))
        dialog.show()


        return dialog


    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {

        if (oldView != null) {

            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }

        if (newView != null) {
            if (newView is MetroItemFrameLayout) {
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alto))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }


    }


}