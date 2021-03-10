package com.example.android.architecture.blueprints.beetv.data.repository

import com.example.android.architecture.blueprints.beetv.data.api.ApiHelper
import com.example.android.architecture.blueprints.beetv.data.models.BMovie
import com.example.android.architecture.blueprints.beetv.util.Constants

class MediaRepository(private val apiHelper: ApiHelper) {
    suspend fun getCategories() = apiHelper.getCategories()
    suspend fun getTopMovies() = apiHelper.getTopMovies()
    suspend fun getMovieDetail(id: String) = apiHelper.getMovieDetail(id)
    suspend fun getMovies() = apiHelper.getMovies()
    suspend fun getMoviesByCategoryId(categoryId: String, page : Int, limit : Int)
            = apiHelper.getMoviesByCategoryId(categoryId, page,limit)
    suspend fun getMoviesByCategoryIdAndSortUpdate(categoryId: String, page : Int, limit : Int)
            = apiHelper.getMoviesByCategoryIdAndSortUpdate(categoryId, page,limit)
    suspend fun getMoviesByCategoryIdAndSortView(categoryId: String, page : Int, limit : Int)
            = apiHelper.getMoviesByCategoryIdAndSortView(categoryId, page,limit)
    suspend fun getMoviesBySubCategoryIdAndSortUpdate(categoryId: String, page : Int, limit : Int)
            = apiHelper.getMoviesBySubCategoryIdAndSortUpdate(categoryId, page,limit)
    suspend fun searchMovie(keyword: String,page : Int) = apiHelper.searchMovie(keyword, page,16)
    suspend fun getLives() = apiHelper.getLives()
    suspend fun getLiveById(liveId: String) = apiHelper.getLiveByID(liveId)
    suspend fun getLivesByCategoryId(categoryId: String,page : Int, limit : Int)
            = apiHelper.getLivesByCategoryId(categoryId, page,limit)

    suspend fun getAllMyFavorites(deviceID: String,page : Int, limit : Int)
            = apiHelper.getAllMyFavorites(deviceID, page,limit)
    suspend fun getAllMyLiveFavorites(deviceID: String)
            = apiHelper.getAllMyLiveFavorites(deviceID)


    suspend fun getFavoritesWithPaging(categoryIdList: String,page : Int, limit : Int)
            = apiHelper.getFavoritesWithPaging(categoryIdList, page,limit)

    suspend fun getWatchHistoryWithPaging(categoryIdList: String,page : Int, limit : Int)
            = apiHelper.getWatchHistoryWithPaging(categoryIdList, page,limit)
    suspend fun getAllMyWatchHistory(deviceID: String,page : Int, limit : Int)
            = apiHelper.getAllMyWatchHistory(deviceID, page,limit)
    suspend fun getLiveTimeTable(live: String, date: String)
            = apiHelper.getLiveTimeTable(live, date)

    suspend fun getAllLive() = apiHelper.getAllLive()
}