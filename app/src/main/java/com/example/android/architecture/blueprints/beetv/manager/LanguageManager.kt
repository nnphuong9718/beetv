package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import java.util.*


class LanguageManager {


    private var mLanguage: String? = null

    companion object {
        private var mInstance: LanguageManager? = null
        fun getInstance(): LanguageManager {

            if (mInstance == null) {
                mInstance = LanguageManager()
            }
            return mInstance!!
        }
    }

    fun setData(language: String) {
        this.mLanguage = language
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("Language", language)
            commit()
        }
        setAppLocale(language)

    }

    private fun setAppLocale(localeCode: String) {
        val resources: Resources = BeeTVApplication.context.resources
        val dm: DisplayMetrics = resources.displayMetrics
        val config: Configuration = resources.configuration
        config.setLocale(Locale(localeCode))
        resources.updateConfiguration(config, dm)
    }

    fun getData(context: Context): String {
        if (mLanguage == null) {
            val sharedPref = context.getSharedPreferences(
                    "BeeTV", Context.MODE_PRIVATE)
                    ?: return if (Locale.getDefault().language != "en"
                            && Locale.getDefault().language != "ko") {
                        "English"
                    } else {
                        Locale.getDefault().language
                    }

            val json = sharedPref.getString("Language", null)

            mLanguage = json

        }
        return mLanguage ?: if (Locale.getDefault().language != "en"
                && Locale.getDefault().language != "ko") {
            "English"
        } else {
            Locale.getDefault().language
        }
    }


}