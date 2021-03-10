package com.example.android.architecture.blueprints.beetv.manager

import android.content.Context
import android.util.Log
import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.data.models.BChapterSeekInfo
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.data.models.BMovieSeekInfo
import com.google.gson.Gson
import java.util.*

class MovieManager {

    private val mTopMovie = mutableListOf<BMovie>()
    //String: id category, Int: number page
    private val mMovieCacheList = hashMapOf<String, Pair<Pair<Int,Int>, List<BMovie>>>()

    private var mMovieSeekList = mutableListOf<BMovieSeekInfo>()

    companion object {
        private var mInstance: MovieManager? = null
        fun getInstance(): MovieManager {

            if (mInstance == null) {
                mInstance = MovieManager()
            }
            return mInstance!!
        }
    }

    fun setData(movies: List<BMovie>?) {
        mTopMovie.clear()
        if (movies != null)
            mTopMovie.addAll(movies)
//            mTopMovie.addAll(movies.sortedBy { it -> it.topPosition })
    }

    fun getData(): MutableList<BMovie> = mTopMovie

    fun addMovieList(idCategory: String, page: Int, totalPage : Int, movies: List<BMovie>) {
        mMovieCacheList[idCategory] = (page to totalPage) to movies
    }

    fun getMovieListAndPage(idCategory: String) : Pair<Pair<Int,Int>,List<BMovie>>? =
            if (mMovieCacheList.containsKey(idCategory)){
                mMovieCacheList[idCategory]
            } else null


    fun findMovieSeekInfo(movieId: String?) : BMovieSeekInfo? {
        if (mMovieSeekList.isEmpty()) mMovieSeekList = getSeekList()
        return mMovieSeekList.find { it -> it.id == movieId }
    }

    fun cacheSeekInfo(movieId: String, chapterId: String, seek: Long) {
        var movieSeek = findMovieSeekInfo(movieId)
        if (movieSeek != null) {
            movieSeek.addChapterSeek(chapterId, seek)
//            movieSeek.chapterId = chapterId
//            movieSeek.seekTime = seek
        } else {
            mMovieSeekList.add(BMovieSeekInfo(
                    movieId,
                    listOf(BChapterSeekInfo(chapterId, seek, Calendar.getInstance().timeInMillis))
            ))
        }
        saveSeekList(Gson().toJson(mMovieSeekList))
    }

    fun resetSeekList() {
        mMovieSeekList.clear()
        saveSeekList("")
    }

    fun saveSeekList(jsData: String) {
        Log.d("BeeTV", "Token $jsData")
        val sharedPref = BeeTVApplication.context.getSharedPreferences(
                "BeeTV", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("seekList", jsData)
            commit()
        }
    }

    fun getSeekList(): MutableList<BMovieSeekInfo> {
        val sharedPref = BeeTVApplication.context.getSharedPreferences(
                "BeeTV", Context.MODE_PRIVATE)
        val jsData = sharedPref.getString("seekList", null)
        var list = mutableListOf<BMovieSeekInfo>()
        if (jsData?.isNotBlank() == true) {
            list = Gson().fromJson(jsData, Array<BMovieSeekInfo>::class.java).asList().toMutableList()
        }
        return list
    }

}