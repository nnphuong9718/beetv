package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.google.gson.Gson
import kotlin.collections.HashMap

class LiveManager {

    private var mLive : BLive ?= null
    private var mCategory  : BCategory ?= null

    private var mLiveTimeCaches = hashMapOf<String,HashMap<BLive,List<BLiveTime>>>()
    private var mLiveCaches = hashMapOf<String,List<BLive>>()
    companion object {
        private var mInstance: LiveManager? = null
        fun getInstance(): LiveManager {

            if (mInstance == null) {
                mInstance = LiveManager()
            }
            return mInstance!!
        }
    }

    fun setData(live : BLive,category : BCategory){
        mLive = live
        mCategory = category
        val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("live", Gson().toJson(live))
            putString("category", Gson().toJson(category))
            commit()
        }
    }

    fun getData() : Pair<BCategory?,BLive?>{
        if (mLive == null){
            val sharedPref = BeeTVApplication.context.getSharedPreferences("BeeTV", Context.MODE_PRIVATE)
            val liveJson = sharedPref.getString("live", null)
            if (liveJson != null){
                mLive = Gson().fromJson(liveJson, BLive::class.java)
            }

            val categoryJson = sharedPref.getString("category", null)
            if (liveJson != null){
                mCategory = Gson().fromJson(categoryJson, BCategory::class.java)
            }
        }

        return mCategory to mLive
    }
    fun addLiveTimeList(idCategory: String, live: BLive, liveTimes: List<BLiveTime>) {
        var liveHashMap = hashMapOf<BLive, List<BLiveTime>>()
        if (mLiveTimeCaches.containsKey(idCategory)){
            liveHashMap = mLiveTimeCaches[idCategory]!!
        }
        liveHashMap[live] = liveTimes
        mLiveTimeCaches[idCategory] = liveHashMap
    }

    fun addLiveList(idCategory: String, lives: List<BLive>) {

        mLiveCaches[idCategory] = lives
    }


    fun getLiveAndTimeList(idCategory: String, bLive: BLive) : List<BLiveTime>? =
            if (mLiveTimeCaches.containsKey(idCategory)){
                mLiveTimeCaches[idCategory]?.get(bLive)
            }else null

    fun getLiveList(idCategory: String) : List<BLive>? =
            if (mLiveCaches.containsKey(idCategory)){
                mLiveCaches[idCategory]
            }else null
}