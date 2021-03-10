package com.example.android.architecture.blueprints.beetv.data.models

data class BBilling(
        val id: String,
        val num: String,
        val pwd: String,
        val activated_date: String, // timestamp 1599891274000
        val expired_date: Long, // timestamp
        val media: String, // [M,L,ML] mean Movie/Live/Movie and Live
        val created_at_unix_timestamp: String,
        val type: String,
        val enable: Boolean,
        val status: Boolean,
        val created_at: String, // utc format 2020-09-12T13:12:31.968Z
        val updated_at: String // utc format
) {
}