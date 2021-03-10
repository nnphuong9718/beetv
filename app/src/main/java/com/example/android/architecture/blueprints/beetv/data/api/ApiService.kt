package com.example.android.architecture.blueprints.beetv.data.api

import com.example.android.architecture.blueprints.beetv.data.models.*
import retrofit2.http.*

interface ApiService {

    // Media group

    @GET("category")
    suspend fun getCategoryMedia(@Query("fields") fields: String, @Query("limit") limit: Int): BaseResponse<BCategory>

    @GET("movie")
    suspend fun getTopMovies(@Query("fields") fields: String, @Query("filter") filter: String, @Query("order") order: String, @Query("limit") limit: Int): BaseResponse<BMovie>

    @GET("movie")
    suspend fun getMovies(): BaseResponse<BMovie>

    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") id: String, @Query("fields") fields: String): BaseFindOneResponse<BMovie>

    @GET("category_movie")
    suspend fun getMoviesByCategoryId(@Query("fields") fields: String,
                                      @Query("filter") filter: String,
                                      @Query("page") page: Int,
                                      @Query("limit") limit: Int,
                                      @Query("order") order: String): BaseResponse<BMovieNestCategory>

    @GET("movie/get_movie_by_category/{id}")
    suspend fun getMoviesByCategoryIdWithOrder(
            @Path("id") id: String,
            @Query("fields") fields: String,
            @Query("filter") filter: String,
            @Query("order") order: String,
            @Query("page") page: Int,
            @Query("limit") limit: Int): BaseResponse<BMovie>

    @GET("movie/get_movie_by_category/{id}")
    suspend fun getMoviesBySubCategoryIdWithOrder(
            @Path("id") id: String,
            @Query("fields") fields: String,
            @Query("filter") filter: String,
            @Query("order") order: String,
            @Query("page") page: Int,
            @Query("limit") limit: Int,
            @Query("isSub") isSub: Boolean): BaseResponse<BMovie>


    @GET("live")
    suspend fun getLives(@Query("fields") fields: String): BaseResponse<BLive>

    @GET("live/{id}")
    suspend fun getLiveById(@Path("id") liveId: String, @Query("fields") fields: String): BaseFindOneResponse<BLive>

    @GET("category_live")
    suspend fun getLivesByCategoryId(@Query("fields") fields: String,
                                     @Query("filter") filter: String,
                                     @Query("page") page: Int,
                                     @Query("limit") limit: Int): BaseResponse<BLiveNestCategory>

    @GET("favorite/my_favorite")
    suspend fun getFavorites(@Query("fields") fields: String): BaseResponse<BFavorite>

    @GET("favorite/my_favorite")
    suspend fun getFavoritesWithPaging(@Query("fields") fields: String,
                                       @Query("filter") filter: String,
                                       @Query("page") page: Int,
                                       @Query("limit") limit: Int): BaseResponse<BFavorite>


    @GET("favorite")
    suspend fun getAllMyFavorites(@Query("fields") fields: String,
                                  @Query("filter") filter: String,
                                  @Query("page") page: Int,
                                  @Query("limit") limit: Int): BaseResponse<BFavorite>

    @GET("favorite")
    suspend fun getAllMyFavorites(@Query("fields") fields: String,
                                  @Query("filter") filter: String): BaseResponse<BFavorite>

    @POST("favorite/like")
    suspend fun makeMovieFavored(@Body request: BFavoriteMovieRequest): BaseFindOneResponse<BFavorite>

    @HTTP(method = "DELETE", path = "favorite/unlike", hasBody = true)
    suspend fun makeMovieNoneFavored(@Body request: BFavoriteMovieRequest): BaseResponseConfirm

    @POST("favorite/like")
    suspend fun makeLiveFavored(@Body request: BFavoriteLiveRequest): BaseFindOneResponse<BFavorite>

    @HTTP(method = "DELETE", path = "favorite/unlike", hasBody = true)
    suspend fun makeLiveNoneFavored(@Body request: BFavoriteLiveRequest): BaseResponseConfirm

    @GET("live_calendar/schedule/{channel}/{date}")
    suspend fun getLiveTimeTable(
            @Tag domain: String, // make this tag for change to recorder URL
            @Path("channel") channel: String,
            @Path("date") date: String): BaseResponse<BLiveTime>

//    @GET("movie")
//    suspend fun searchMovie(@Query("fields") fields: String, @Query("filter") filter: String,
//                            @Query("page") page: Int, @Query("limit") limit: Int): BaseResponse<BMovie>

    @GET("movie")
    suspend fun searchMovie(@Query("fields") fields: String, @Query("filter") filter: String,
                            @Query("page") page: Int,
                            @Query("limit") limit: Int): BaseResponse<BMovie>


    // Account group
    @POST("auth/login")
    suspend fun login(@Body request: BLoginRequest): BaseResponse<BAccount>

    @PUT("device/{id}")
    suspend fun updateProfile(@Path("id") id: String, @Body request: BUpdateProfileRequest): BaseResponse<BAccount>

    @GET("device/profile")
    suspend fun getProfile(@Query("fields") fields: String): BaseFindOneResponse<BAccount>

    @GET("recharge_card")
    suspend fun getBillings(@Query("fields") fields: String): BaseResponse<BBilling>

    // Notice & Ads group

    @GET("carousel")
    suspend fun getAds(@Query("fields") fields: String, @Query("filter") filter: String): BaseResponse<BAds>

    @GET("notice")
    suspend fun getNotices(@Query("fields") fields: String, @Query("filter") filter: String): BaseResponse<BNotice>

    @GET("notice")
    suspend fun getPersonalNotices(@Query("fields") fields: String, @Query("filter") filter: String): BaseResponse<BNotice>

    // Watch history
    @GET("watch_history/my_watch_history")
    suspend fun getWatchHistories(@Query("fields") fields: String): BaseResponse<BWatchHistory>

    @GET("watch_history")
    suspend fun getAllMyWatchHistory(@Query("fields") fields: String,
                                     @Query("filter") filter: String,
                                     @Query("order") order: String,
                                     @Query("page") page: Int,
                                     @Query("limit") limit: Int): BaseResponse<BWatchHistory>

    @GET("watch_history/my_watch_history")
    suspend fun getWatchHistoryWithPaging(@Query("fields") fields: String,
                                          @Query("filter") filter: String,
                                          @Query("order") order: String,
                                          @Query("page") page: Int,
                                          @Query("limit") limit: Int): BaseResponse<BWatchHistory>

    @POST("watch_history/add")
    suspend fun saveWatched(@Body request: BWatchedMovieRequest): BaseFindOneResponse<BWatchHistory>

    @GET("movie/watch_movie/{id}")
    suspend fun watchMovie(@Path("id") id: String): BaseResponseConfirm

    @DELETE("watch_history/delete_my_watch_history/MOVIE")
    suspend fun removeAllWatchHistory(): BaseResponseConfirm

    @DELETE("favorite/delete_my_favorite/MOVIE")
    suspend fun removeAllFavorite(): BaseResponseConfirm

    @GET("version")
    suspend fun getVersion(@Query("fields") fields: String,
                           @Query("filter") filter: String,
                           @Query("order") order: String): BaseResponse<BVersion>

    @GET("live?fields=[\"\$all\", {\"category_lives\": [\"id\", {\"category\": [\"id\"]}] }]")
    suspend fun getAllLive(): BaseResponse<BLive>
}