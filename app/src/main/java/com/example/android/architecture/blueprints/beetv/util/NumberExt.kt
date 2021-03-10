package com.example.android.architecture.blueprints.beetv.util

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

fun Int.formatOffset(): String {
    val sb = StringBuilder("UTC").append(if (toInt() < 0) "-" else "+")
    var secs = Math.abs(toInt()) / 1000
    val hours = secs / 3600
    secs -= hours * 3600
    val mins = secs / 60
    sb.append(if (hours < 10) "0" else "").append(hours).append(":")
    sb.append(if (mins < 10) "0" else "").append(mins)
    return sb.toString()
}

fun Int.toNumberString(): String {
    return if (this < 10) "0$this" else this.toString()
}


fun Long.toExpiredDate(): String {
    return if (this == 0.toLong()) BeeTVApplication.context.applicationContext.getString(R.string.text_service_period_expired) else String.format(BeeTVApplication.context.applicationContext.getString(R.string.service_remaining_period), this)
}

fun Long.parseTime(): String {
//    val timeZoneDefault = TimeZoneManager.getInstance().getData()
    val timeZoneKorea = TimeZone.getTimeZone("Asia/Seoul")
    val timeZoneUTC = TimeZone.getTimeZone("UTC")
    val calendarUTC = Calendar.getInstance(timeZoneUTC)
    calendarUTC.timeInMillis = this

    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
    simpleDateFormat.timeZone = timeZoneKorea
    return simpleDateFormat.format(calendarUTC.timeInMillis)
}


fun Long.parseTimeLocal(): Long {

    val timeZoneKorea = TimeZone.getTimeZone("Asia/Seoul")
    val timeZoneUTC = TimeZone.getTimeZone("UTC")
    val calendarUTC = Calendar.getInstance(timeZoneUTC)
    calendarUTC.timeInMillis = this

    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.KOREAN)
    simpleDateFormat.timeZone = timeZoneKorea

    val simpleDateFormat2 = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.KOREAN)
    simpleDateFormat2.timeZone = timeZoneKorea
    val dateString = simpleDateFormat.format(calendarUTC.timeInMillis)
    return simpleDateFormat2.parse(dateString).time





}

fun String.toMillisecond(): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.parse(this).toEpochMilli()
} else {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(this).time
}

fun Long.formatDay(context: Context): String {
    val c: Calendar = Calendar.getInstance(TimeZoneManager.getInstance().getData())
    c.timeInMillis = this
    return "${c.get(Calendar.DAY_OF_MONTH)} ${FormatUtils.getDateFromTime(context, this)}"
}

fun Long.toYMD(): String {
    return SimpleDateFormat("yyyyMMdd").format(Date(this))
}

