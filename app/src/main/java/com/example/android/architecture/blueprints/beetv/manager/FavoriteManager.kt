package com.example.android.architecture.blueprints.beetv.manager

import android.util.Log
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.google.gson.Gson

class FavoriteManager {

    private val mFavorites = mutableListOf<BFavorite>()

    companion object {
        private var mInstance: FavoriteManager? = null
        fun getInstance(): FavoriteManager {

            if (mInstance == null) {
                mInstance = FavoriteManager()
            }
            return mInstance!!
        }
    }

    fun setData(favorites: List<BFavorite>?) {
        mFavorites.clear()
        if (favorites != null)
            mFavorites.addAll(favorites)

        Log.d("yenyen", Gson().toJson(mFavorites))
    }

    fun getData(): MutableList<BFavorite> = mFavorites

    fun getDataByType(type : String) :MutableList<BFavorite>{
        val list = mutableListOf<BFavorite>()
        mFavorites.forEach {
            if (it.type == type) {
                list.add(it)
            }
        }
        return list
    }

    fun checkMovieIsFavorite(movieID: String): Boolean {
        mFavorites.forEach {
            if (it.movie_id == movieID) {
                return true
            }
        }
        return false
    }


    fun checkLiveIsFavorite(liveID: String): Boolean {
        mFavorites.forEach {
            if (it.live_id == liveID) {
                return true
            }
        }
        return false
    }

    fun removeFavoriteMovie(mediaID: String) {
        var position = -1

        mFavorites.forEachIndexed { index, bFavorite ->
            if (bFavorite.movie_id == mediaID) {
                position = index
                return@forEachIndexed
            }
        }
        if (position > -1)
            mFavorites.removeAt(position)
    }

    fun removeAllFavoriteMovie() {
        val favoriteList = getDataByType(Constants.TYPE_FILE.MOVIE.toString())
        favoriteList.forEachIndexed { index, bFavorite ->
         mFavorites.removeAt(mFavorites.indexOf(bFavorite))
        }
    }
    fun removeFavoriteLive(mediaID: String) : Int {
        var position = -1

        mFavorites.forEachIndexed { index, bFavorite ->
            if (bFavorite.live_id == mediaID) {
                position = index
                return@forEachIndexed
            }
        }
        if (position > -1)
            mFavorites.removeAt(position)
        return FavoriteManager.getInstance().getDataByType(Constants.TYPE_FILE.LIVE.toString()).size
    }
    fun addFavorite(favorite: BFavorite) {
        mFavorites.add(favorite)
    }

    fun searchMovieByName(keyword: String): MutableList<BFavorite> {
        val list = mutableListOf<BFavorite>()
        mFavorites.forEach {
            if (it.movie?.name?.toLowerCase()?.contains(keyword.toLowerCase()) == true && it.type == Constants.TYPE_FILE.MOVIE.type) {
                list.add(it)
            }
        }
        return list
    }

}