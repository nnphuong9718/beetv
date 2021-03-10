package com.example.android.architecture.blueprints.beetv.data.models

import com.example.android.architecture.blueprints.beetv.util.Constants

data class BFavorite(
    val id: String,
    val device_id: String,
    val live_id: String,
    val movie_id: String,
    val type: String,
    val created_at_unix_timestamp: String,
    val status: Boolean,
    val created_at: String,
    val updated_at: String,
    var movie: BMovie?,
    var live: BLive?,
    val position_updated_at: Long
) {}