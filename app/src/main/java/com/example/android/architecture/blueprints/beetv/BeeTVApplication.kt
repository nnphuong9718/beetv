/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.beetv

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.multidex.MultiDexApplication
import com.example.android.architecture.blueprints.beetv.data.api.ApiHelper
import com.example.android.architecture.blueprints.beetv.data.api.RetrofitBuilder
import com.example.android.architecture.blueprints.beetv.data.repository.AccountRepository
import com.example.android.architecture.blueprints.beetv.data.repository.MediaRepository
import com.example.android.architecture.blueprints.beetv.data.source.TasksRepository
import com.example.android.architecture.blueprints.beetv.manager.LanguageManager
import com.example.android.architecture.blueprints.beetv.manager.TimeZoneManager
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*


/**
 * An application that lazily provides a repository. Note that this Service Locator pattern is
 * used to simplify the sample. Consider a Dependency Injection framework.
 *
 * Also, sets up Timber in the DEBUG BuildConfig. Read Timber's documentation for production setups.
 */
class BeeTVApplication : MultiDexApplication() {
    // Depends on the flavor,

    val taskRepository: TasksRepository get() = ServiceLocator.provideTasksRepository(this)
    val movieRepository: MediaRepository get() = MediaRepository(ApiHelper(RetrofitBuilder.apiService))
    val accountRepository: AccountRepository get() = AccountRepository(ApiHelper(RetrofitBuilder.apiService))
//    val myLocation: MyLocation get() = MyLocation.newInstance(this)

    override fun onCreate() {
        super.onCreate()
        mainHandler = Handler(Looper.getMainLooper()) //主线程的handler
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())
        context = applicationContext
        TimeZoneManager.getInstance().setTimeZoneDefault()
    }

    companion object {
        lateinit var mainHandler : Handler
        lateinit var context: Context
        var isShowPopup = false
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(updateBaseContextLocale(base!!))
    }


    private fun updateBaseContextLocale(context: Context): Context? {

        val locale = Locale(LanguageManager.getInstance().getData(context))
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else updateResourcesLocaleLegacy(context, locale)
    }
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
}
