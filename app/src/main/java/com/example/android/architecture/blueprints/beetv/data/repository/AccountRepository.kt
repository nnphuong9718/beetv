package com.example.android.architecture.blueprints.beetv.data.repository

import com.example.android.architecture.blueprints.beetv.data.api.ApiHelper
import com.example.android.architecture.blueprints.beetv.data.models.BBilling
import com.example.android.architecture.blueprints.beetv.data.models.BFavoriteLiveRequest
import com.example.android.architecture.blueprints.beetv.data.models.BFavoriteMovieRequest
import com.example.android.architecture.blueprints.beetv.data.models.BUpdateProfileRequest
import com.example.android.architecture.blueprints.beetv.manager.AccountManager

class AccountRepository(private val apiHelper: ApiHelper) {

    // Todo please get deviceId as singleton when user logged-in

    suspend fun login(id: String, password: String) = apiHelper.login(id, password)

    suspend fun updateProfile(id: String, request: BUpdateProfileRequest) = apiHelper.updateProfile(id, request)
    suspend fun getProfile() = apiHelper.getProfile()
    suspend fun getBillings(deviceId: String) = apiHelper.getBillings(deviceId)

    // Todo Logged-in is required. Please get device Id from profile
    // Please check type field <MOVIE|LIVE> to load correct GUI
    suspend fun getFavorites() = apiHelper.getFavorites()
    suspend fun likeMovie(movieId: String) = apiHelper.setMovieFavorite(
            BFavoriteMovieRequest(movieId)
    )
    suspend fun unlikeMovie(movieId: String) = apiHelper.unsetMovieFavorite(
            BFavoriteMovieRequest(movieId)
    )
    suspend fun likeLive(movieId: String) = apiHelper.setLiveFavorite(
            BFavoriteLiveRequest(movieId)
    )
    suspend fun unlikeLive(movieId: String) = apiHelper.unsetLiveFavorite(
            BFavoriteLiveRequest(movieId)
    )

    suspend fun getAds() = apiHelper.getAds()
    suspend fun getNotices() = apiHelper.getNotices()
    suspend fun getPersonalNotices(deviceId: String) = apiHelper.getPersonalNotices(deviceId)

    suspend fun getWatchHistories() = apiHelper.getWatchHistories()
    suspend fun saveWatched(movieId: String) = apiHelper.saveWatched(movieId)
    suspend fun watchMovie(movieId: String) = apiHelper.watchMovie(movieId)

    suspend fun removeAllWatchHistory() = apiHelper.removeAllWatchHistory()
    suspend fun removeAllFavorite() = apiHelper.removeAllFavorite()
    suspend fun getVersion() = apiHelper.getVersion()
}