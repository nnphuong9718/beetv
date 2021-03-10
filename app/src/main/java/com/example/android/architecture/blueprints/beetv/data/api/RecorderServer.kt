package com.example.android.architecture.blueprints.beetv.data.api

import android.util.Log
import com.example.android.architecture.blueprints.beetv.data.models.BLive
import com.example.android.architecture.blueprints.beetv.data.models.BLiveTime
import com.example.android.architecture.blueprints.beetv.data.models.Record
import com.example.android.architecture.blueprints.beetv.modules.menu.MenuFragment
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class RecorderServer {

    private val RECORDER_SERVER_IP = "http://23.237.182.178:8888"

    private val client = OkHttpClient()

    companion object {
        val instance = RecorderServer()
    }

    fun checkRecorderExists(fileName: String, result: ((Record) -> Unit)?) {
        var url = "$RECORDER_SERVER_IP/api/v1/live/exist/$fileName.mp4"
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val gson = Gson().fromJson(res, Record::class.java)
                result?.invoke(gson)
            }
        })
    }
}