package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import java.util.*

class TimeZoneManager {


    private var mTimeZone: TimeZone? = null
    private var mTimeZoneName: String? = null

    companion object {
        private var mInstance: TimeZoneManager? = null
        fun getInstance(): TimeZoneManager {

            if (mInstance == null) {
                mInstance = TimeZoneManager()
            }
            return mInstance!!
        }
    }

    fun setData(timeZone: TimeZone, timezoneName: String?) {
        this.mTimeZone = timeZone
        this.mTimeZoneName = timezoneName
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("TimeZone", timeZone.id)
            putString("TimeZoneName", timezoneName)
            commit()
        }
    }

    fun getData(): TimeZone {
        if (mTimeZone == null) {
            val sharedPref = BeeTVApplication.context.getSharedPreferences(
                    "BeeTV", Context.MODE_PRIVATE) ?: return TimeZone.getDefault()

            val json = sharedPref.getString("TimeZone", null)

            if (json != null) {
                mTimeZone = TimeZone.getTimeZone(json)
            }

        }
        return mTimeZone ?: TimeZone.getDefault()
    }

    fun getTimezoneName(): String {
        if (mTimeZone == null) {
            val sharedPref = BeeTVApplication.context.getSharedPreferences(
                    "BeeTV", Context.MODE_PRIVATE)
                    ?: return BeeTVApplication.context.resources.getString(R.string.sytem_time)

            val timezone = sharedPref.getString("TimeZone", null)
            if (timezone != null) {
                mTimeZone = TimeZone.getTimeZone(timezone)
            }

        }
        val timezoneName = mTimeZone?.id?.replace("/", "_")?.replace("+", "_")
        var resID : Int = BeeTVApplication.context.resources.getIdentifier("sytem_time", "string", BeeTVApplication.context.packageName)
        if (timezoneName != null) {
            resID = BeeTVApplication.context.resources.getIdentifier(timezoneName, "string", BeeTVApplication.context.packageName)
        }

        return if (mTimeZone != null && mTimeZone != TimeZone.getDefault()) BeeTVApplication.context.resources.getString(resID) else BeeTVApplication.context.resources.getString(R.string.sytem_time)
    }

    fun setTimeZoneDefault() {
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("TimeZoneDefault", TimeZone.getDefault().id)
            commit()
        }

    }

    fun getTimeZoneDefault(): TimeZone {
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        return TimeZone.getTimeZone(sharedPref.getString("TimeZoneDefault", TimeZone.getDefault().id))
    }

}