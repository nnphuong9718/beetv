package com.example.android.architecture.blueprints.beetv.manager

import com.example.android.architecture.blueprints.beetv.data.models.BWatchHistory
import com.example.android.architecture.blueprints.beetv.util.Constants

class WatchHistoryManager {

    private val mWatchHistories = mutableListOf<BWatchHistory>()

    companion object {
        private var mInstance: WatchHistoryManager? = null
        fun getInstance(): WatchHistoryManager {

            if (mInstance == null) {
                mInstance = WatchHistoryManager()
            }
            return mInstance!!
        }
    }

    fun setData(watchHistories: List<BWatchHistory>?) {
        mWatchHistories.clear()
        if (watchHistories != null)
            mWatchHistories.addAll(watchHistories)

    }

    fun getData(): MutableList<BWatchHistory> = mWatchHistories

    fun getDataByType(type: String): MutableList<BWatchHistory> {
        val list = mutableListOf<BWatchHistory>()
        mWatchHistories.forEach {
            if (it.type == type) {
                list.add(it)
            }
        }
        return list
    }


    fun addWatchHistory(history: BWatchHistory) {
        val removeItems = mWatchHistories.filter { it.movie_id == history.movie_id }
        mWatchHistories.removeAll(removeItems)
        mWatchHistories.add(0, history)

//        val list = mWatchHistories
//        mWatchHistories.clear()
//        mWatchHistories.add(history)
//        var position = -1
//        list.forEachIndexed { i: Int, bWatchHistory: BWatchHistory ->
//            if (bWatchHistory.movie_id == history.movie_id){
//                position = i
//                return@forEachIndexed
//            }
//        }
//
//        if (position != -1)
//        list. removeAt(position)
//        mWatchHistories.addAll(list)
    }

    fun searchMovieByName(keyword: String): MutableList<BWatchHistory> {
        val list = mutableListOf<BWatchHistory>()
        mWatchHistories.forEach {
            if (it.movie.name.toLowerCase().contains(keyword.toLowerCase()) ) {
                list.add(it)
            }
        }
        return list
    }

    fun removeAllWatchHistory(){
        mWatchHistories.clear()
    }

}