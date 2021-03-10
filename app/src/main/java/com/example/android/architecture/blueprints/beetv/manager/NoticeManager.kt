package com.example.android.architecture.blueprints.beetv.manager

import androidx.lifecycle.MutableLiveData
import com.example.android.architecture.blueprints.beetv.data.models.BNotice
import com.example.android.architecture.blueprints.beetv.util.toMillisecond

class NoticeManager {

//    private var mNotices = mutableListOf<BNotice>()
    val noticeList =  MutableLiveData<List<BNotice>>()

    companion object {
        private var mInstance: NoticeManager? = null
        fun getInstance(): NoticeManager {

            if (mInstance == null) {
                mInstance = NoticeManager()
            }
            return mInstance!!
        }
    }

    fun setData(notices: List<BNotice>?) {
//        mNotices = notices?.toMutableList()?: mutableListOf()
        noticeList.value = notices?.sortedByDescending { it.updated_at.toMillisecond() } ?.toMutableList() ?: mutableListOf()
    }

    fun getData(): MutableList<BNotice> {
        return noticeList.value?.toMutableList() ?: mutableListOf()
    }

    fun clearPersonalNotices() {
        var items = noticeList.value?.toMutableList() ?: mutableListOf()
        var personalItems = items.filter{ it.device_id?.isNotEmpty() == true }.toList()
        items.removeAll(personalItems)
        noticeList.value = items
    }

}