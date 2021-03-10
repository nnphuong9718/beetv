package com.example.android.architecture.blueprints.beetv.data.models

data class BWatchHistory(
    val id: String,
    val device_id: String,
    val live_id: String,
    val movie_id: String,
    val type: String,
    val created_at_unix_timestamp: String,
    val status: Boolean,
    val created_at: String,
    val updated_at: String,
    var movie: BMovie,
    val position_updated_at: Long,
    val live: BLive
) {}

data class BWatchedMovieRequest(
        val movie_id: String
) {}