package com.example.android.architecture.blueprints.beetv.data.api

import com.example.android.architecture.blueprints.beetv.common.Const
import com.example.android.architecture.blueprints.beetv.data.models.*
import com.example.android.architecture.blueprints.beetv.util.Utils

class ApiHelper(private val apiService: ApiService) {

    suspend fun login(accountId: String, password: String) = apiService
            .login(BLoginRequest(logical_id = accountId, password = password, physical_id = "${Utils.deviceID()}"))

    suspend fun updateProfile(id: String, request: BUpdateProfileRequest) = apiService
            .updateProfile(id, request)

    suspend fun getProfile() = apiService
            .getProfile(fields = "[\"\$all\", {\"recharge_cards\": [\"\$all\"]}, {\"node\": [\"\$all\"]}, {\"agent\": [\"\$all\"]}]")

    suspend fun getBillings(deviceId: String) = apiService
            .getBillings(fields = "[\"\$all\", {\"device\": [\"\$all\", {\"\$filter\": {\"id\": \"${deviceId}\"} }]}]")


    suspend fun getCategories() = apiService.getCategoryMedia(fields = "[\"\$all\"]", limit = 999)
    suspend fun getTopMovies() = apiService.getTopMovies(
            fields = "[\"\$all\", {\"videos\": [\"\$all\"]}]",
            filter = "{\"status\": true}",
            order = "[[\"position_updated_at\",\"DESC\"]]",
            limit = 6)

    suspend fun getMovieDetail(id: String) = apiService.getMovieDetail(
            id = id,
            fields = "[\"\$all\", {\"videos\": [\"\$all\"]},{\"category_movies\":[\"\$all\", {\"category\": [\"\$all\", {\"parent\": [\"\$all\"]}]}]}]"
    )

    suspend fun getMovies() = apiService.getMovies()
    suspend fun getMoviesByCategoryId(categoryId: String, page: Int, limit: Int) = apiService.getMoviesByCategoryId(
            fields = "[\"\$all\", {\"movie\": [\"\$all\", {\"\$filter\": {\"status\": true}}]} , {\"category\": [\"\$all\"]}]",
            filter = "{\"category_id\": {\"\$in\": [\"${categoryId}\"]}}",
            page = page,
            limit = limit,
            order = "[[\"movie\",\"updated_at\",\"desc\"]]"
    )

    suspend fun getMoviesByCategoryIdAndSortUpdate(categoryId: String, page: Int, limit: Int) = apiService.getMoviesByCategoryIdWithOrder(
//            fields = "[\"\$all\", {\"movie\": [\"\$all\"]} , {\"category\": [\"\$all\"]}]",
            id = categoryId,
            fields = "[\"\$all\", {\"videos\": [\"\$all\"]}]",
            filter = "{\"status\": true}",
//            filter = "{\"category_id\": {\"\$in\": [\"${categoryId}\"]},\"status\": true}",
            order = "[[\"position_updated_at\",\"DESC\"]]",
            page = page,
            limit = limit
    )

    suspend fun getMoviesBySubCategoryIdAndSortUpdate(categoryId: String, page: Int, limit: Int) = apiService.getMoviesBySubCategoryIdWithOrder(
            id = categoryId,
            fields = "[\"\$all\", {\"videos\": [\"\$all\"]}]",
            filter = "{\"status\": true}",
            order = "[[\"position_updated_at\",\"DESC\"]]",
            page = page,
            limit = limit,
            isSub = true
    )

    suspend fun getMoviesByCategoryIdAndSortView(categoryId: String, page: Int, limit: Int) = apiService.getMoviesByCategoryIdWithOrder(
            id = categoryId,
            fields = "[\"\$all\", {\"videos\": [\"\$all\"]}]",
            filter = "{\"status\": true}",
//            filter = "{\"category_id\": {\"\$in\": [\"${categoryId}\"]},\"status\": true}",
            order = "[[\"views\",\"DESC\"]]", page = page, limit = limit
    )

    suspend fun searchMovie(keyword: String, page: Int, limit: Int) = apiService.searchMovie(
            fields = "[\"\$all\"]",
            filter = "{\"\$or\":[{\"korSearchTerm\": {\"\$iLike\": \"%${keyword}%\"}}, {\"search_term\": {\"\$iLike\": \"%${keyword}%\"}}]}",
            page = page, limit = limit
    )

    suspend fun getLives() = apiService.getLives(fields = "[\"\$all\"]")
    suspend fun getLiveByID(liveId: String) = apiService.getLiveById(liveId = liveId, fields = "[\"\$all\"]")
    suspend fun getLivesByCategoryId(categoryId: String, page: Int, limit: Int) = apiService.getLivesByCategoryId(
            fields = "[\"\$all\", {\"live\": [\"\$all\"]} , {\"category\": [\"\$all\"] }]",
            filter = "{\"category_id\": {\"\$in\": [\"${categoryId}\"]},\"status\": true}", page = page, limit = limit
    )

    suspend fun getFavorites() = apiService.getFavorites(
            fields = "[\"\$all\", {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]}, {\"movie\": [\"\$all\"] }, {\"live\": [\"\$all\"]} ]")

    suspend fun getFavoritesWithPaging(categoryIDList: String, page: Int, limit: Int) = apiService.getFavoritesWithPaging(
            fields = "[\"\$all\", {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]}, {\"movie\": [\"\$all\",{\"category_movies\":[\"\$all\",{\"\$filter\":{\"category_id\":{\"\$in\":$categoryIDList}}}]}]} ]",
            filter = "{\"type\":\"MOVIE\"}",
            page = page,
            limit = limit)

    suspend fun getAllMyFavorites(deviceId: String, page: Int, limit: Int) = apiService.getAllMyFavorites(
            fields = "[\"\$all\", {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]}, {\"movie\": [\"\$all\"] }]",
            filter = "{\"type\":\"MOVIE\"}",
            page = page, limit = limit
    )

    suspend fun getAllMyLiveFavorites(deviceId: String) = apiService.getAllMyFavorites(
//            fields = "[\"\$all\", {\"live\": [\"\$all\", {\"\$filter\": {\"deleted_at\": null} }] }]",
            fields = "[\"\$all\",{\"live\": [\"\$all\"]}, {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]}]",
            filter = "{\"type\":\"LIVE\"}"
    )

    suspend fun setMovieFavorite(request: BFavoriteMovieRequest) = apiService.makeMovieFavored(request)
    suspend fun unsetMovieFavorite(request: BFavoriteMovieRequest) = apiService.makeMovieNoneFavored(request)
    suspend fun setLiveFavorite(request: BFavoriteLiveRequest) = apiService.makeLiveFavored(request)
    suspend fun unsetLiveFavorite(request: BFavoriteLiveRequest) = apiService.makeLiveNoneFavored(request)

    suspend fun getLiveTimeTable(live: String, date: String) = apiService.getLiveTimeTable(
            domain = "RECORDER",
            channel = live,
            date = date
    )

    suspend fun getAds() = apiService.getAds(fields = "[\"\$all\"]",
            filter = "{\"status\": true}")

    suspend fun getNotices() = apiService.getNotices(fields = "[\"\$all\"]",
            filter = "[{\"status\": true}, {\"device_id\": null}]")

    suspend fun getPersonalNotices(deviceId: String) = apiService.getPersonalNotices(fields = "[\"\$all\"]",
            filter = "[{\"status\": true},{\"device_id\":\"$deviceId\"}]")

    suspend fun getWatchHistories() = apiService.getWatchHistories(fields = "[\"\$all\", {\"movie\": [\"\$all\"]}]")

    suspend fun saveWatched(movieId: String) = apiService.saveWatched(BWatchedMovieRequest(movieId))

    suspend fun watchMovie(movieId: String) = apiService.watchMovie(id = movieId)

    suspend fun removeAllWatchHistory() = apiService.removeAllWatchHistory()

    suspend fun removeAllFavorite() = apiService.removeAllFavorite()


    suspend fun getAllMyWatchHistory(deviceId: String, page: Int, limit: Int) = apiService.getAllMyWatchHistory(
            fields = "[\"\$all\", {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]}, {\"movie\": [\"\$all\"] }]",
            filter = "{\"type\":\"MOVIE\"}",
            order = "[[\"updated_at\",\"desc\"]]",
            page = page,
            limit = limit
    )

    suspend fun getWatchHistoryWithPaging(categoryIDList: String, page: Int, limit: Int) = apiService.getWatchHistoryWithPaging(
            fields = "[\"\$all\", {\"physical\":[\"\$all\",{ \"\$filter\": { \"physical_id\": \"${Utils.deviceID()}\"}}]},{\"movie\": [\"\$all\",{\"category_movies\":[\"\$all\",{\"\$filter\":{\"category_id\":{\"\$in\":$categoryIDList}}}]}]} ]",
            filter = "{\"type\":\"MOVIE\"}",
            order = "[[\"updated_at\",\"desc\"]]",
            page = page,
            limit = limit)


    suspend fun getVersion() = apiService.getVersion(fields = "[\"\$all\"]", order = "[[\"created_at_unix_timestamp\",\"desc\"]]",
            filter = "{\"app_id\":\"${Const.APP_ID}\"}")

    suspend fun getAllLive() = apiService.getAllLive()

}