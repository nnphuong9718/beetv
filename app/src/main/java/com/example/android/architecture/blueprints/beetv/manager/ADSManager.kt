package com.example.android.architecture.blueprints.beetv.manager

import android.util.Log
import com.example.android.architecture.blueprints.beetv.data.models.BAds
import com.example.android.architecture.blueprints.beetv.data.models.BNotice

class ADSManager {

    private val mADSList = mutableListOf<BAds>()
    companion object {
        private var mInstance: ADSManager? = null
        fun getInstance(): ADSManager {

            if (mInstance == null) {
                mInstance = ADSManager()
            }
            return mInstance!!
        }
    }

    fun hasAds(): Boolean {
        return mADSList.size > 0
    }

    fun setData(adsList : List<BAds>?){
        mADSList.clear()
        if (adsList != null)
            mADSList.addAll(adsList.filter { it.status }.toList())
    }

    fun getData() : MutableList<BAds> {
        Log.d("yenyen","lay ds ne")
     return mADSList
    }


}