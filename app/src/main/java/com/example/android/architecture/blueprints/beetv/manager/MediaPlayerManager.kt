package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.util.Constants
import java.util.*


class MediaPlayerManager {

    private val RESOLUTION_KEY = "Resolution"
    private var mResolution: Constants.MediaResolution? = null

    companion object {
        private var mInstance: MediaPlayerManager? = null
        fun getInstance(): MediaPlayerManager {

            if (mInstance == null) {
                mInstance = MediaPlayerManager()
            }
            return mInstance!!
        }
    }

    fun setResolution(resolution: Constants.MediaResolution) {
        mResolution = resolution
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(RESOLUTION_KEY, resolution.toString())
            commit()
        }
    }

    fun getResolution(context: Context): Constants.MediaResolution {
        if (mResolution == null) {
            val sharedPref = context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
            val raw = sharedPref.getString(RESOLUTION_KEY, "R16P9")
            mResolution = Constants.MediaResolution.valueOf(raw)
        }
        return mResolution!!
    }

}