package com.example.android.architecture.blueprints.beetv.common.basegui

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.example.android.architecture.blueprints.beetv.manager.LanguageManager
import java.util.*

open class BaseActivity : AppCompatActivity(){

    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.getConfiguration()
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.getDisplayMetrics())
        return context
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun updateResourcesLocale(context: Context, locale: Locale) : Context {
        val configuration =  Configuration(context.resources.configuration)
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }


    private fun updateBaseContextLocale(context: Context): Context? {

        val locale = Locale(LanguageManager.getInstance().getData(context))
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else updateResourcesLocaleLegacy(context, locale)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(updateBaseContextLocale(base!!))
    }

}