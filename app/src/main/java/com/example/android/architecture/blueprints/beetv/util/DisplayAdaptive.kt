package com.example.android.architecture.blueprints.beetv.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import android.util.DisplayMetrics
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.data.models.BAccount
import com.example.android.architecture.blueprints.beetv.manager.AccountManager
import java.util.*


class DisplayAdaptive {

    private object DisplayAdaptiveHolder {
        val INSTANCE: DisplayAdaptive = DisplayAdaptive()
    }

    private fun DisplayAdaptive() {}

    fun getInstance(): DisplayAdaptive? {
        return DisplayAdaptiveHolder.INSTANCE
    }

    fun toLocalPx(pt: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, pt, BeeTVApplication.context.getResources().getDisplayMetrics())
    }


    companion object {
        var THUMB_RATIO = 1.45

        fun getDisplayMetrics(activity: Activity): DisplayMetrics {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics
        }

        fun getBestMovieThumbWidth(activity: Activity): Float {
            return (getDisplayMetrics(activity).widthPixels - dpToPx(activity, 24f)*2 - dpToPx(activity, 7f)*6) / 6f
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    context.resources.displayMetrics
            )
        }

        fun getScriptByLabel(label: String?, timeCreate : Long): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"))
           return when {
                calendar.timeInMillis - timeCreate <= 10*60*60*1000 -> {
                    return "NEW"
                }
                label == "FREE" -> {
                    return BeeTVApplication.context.resources.getString(R.string.free)
                }
                else -> ""
            }
        }

        fun getColorByLabel(label: String?): Int {
            if (label.isNullOrEmpty()) return 0
            return when(label) {
                "무료", "FREE" -> Color.parseColor("#5D7729")
                "NEW" -> Color.parseColor("#FF513D")
                "TOP" -> Color.parseColor("#B7B327")
                "BEST" -> Color.parseColor("#0B7098")
                else -> 0
            }
        }
    }

}