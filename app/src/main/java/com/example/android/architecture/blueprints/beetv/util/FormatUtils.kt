package com.example.android.architecture.blueprints.beetv.util

import android.content.Context
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import java.util.*

object FormatUtils {
    fun getDate(context: Context) :String{
        val c: Calendar = Calendar.getInstance(TimeZoneManager.getInstance().getData())
        val dayOfWeek: Int = c.get(Calendar.DAY_OF_WEEK)

        if (Calendar.MONDAY == dayOfWeek) {
           return context.getString(R.string.monday)
        } else if (Calendar.TUESDAY == dayOfWeek) {
            return context.getString(R.string.tuesday)
        } else if (Calendar.WEDNESDAY == dayOfWeek) {
            return context.getString(R.string.wednesday)
        } else if (Calendar.THURSDAY == dayOfWeek) {
            return context.getString(R.string.thursday)
        } else if (Calendar.FRIDAY == dayOfWeek) {
            return context.getString(R.string.friday)
        } else if (Calendar.SATURDAY == dayOfWeek) {
            return context.getString(R.string.saturday)
        } else if (Calendar.SUNDAY == dayOfWeek) {
            return context.getString(R.string.sunday)
        }else return ""
    }
    fun getDateFromTime(context: Context, time :Long) :String{
        val c: Calendar = Calendar.getInstance(TimeZoneManager.getInstance().getData())
        c.timeInMillis = time
        val dayOfWeek: Int = c.get(Calendar.DAY_OF_WEEK)

        if (Calendar.MONDAY == dayOfWeek) {
            return context.getString(R.string.monday)
        } else if (Calendar.TUESDAY == dayOfWeek) {
            return context.getString(R.string.tuesday)
        } else if (Calendar.WEDNESDAY == dayOfWeek) {
            return context.getString(R.string.wednesday)
        } else if (Calendar.THURSDAY == dayOfWeek) {
            return context.getString(R.string.thursday)
        } else if (Calendar.FRIDAY == dayOfWeek) {
            return context.getString(R.string.friday)
        } else if (Calendar.SATURDAY == dayOfWeek) {
            return context.getString(R.string.saturday)
        } else if (Calendar.SUNDAY == dayOfWeek) {
            return context.getString(R.string.sunday)
        }else return ""
    }

}