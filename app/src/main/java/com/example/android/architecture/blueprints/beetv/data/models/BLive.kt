package com.example.android.architecture.blueprints.beetv.data.models

import java.io.Serializable
import java.util.*

data class BLive(
        val id: String,
        val name: String,
        val channel_code: String,
        val address: String,
        val position: Int,
        val logo: String,
        val status: Boolean,
        val created_at: String,
        val updated_at: String,
        val description :String?,
        val channel_number:String?,
        var is_favorite : Boolean ?= false,
        val category_lives : MutableList<BCategory>
) : Serializable {}

data class BLiveNestCategory(
        val live: BLive
){}


data class BLiveTime(
        val id: String,
        val title: String,
        val start_time: Long,
        val end_time: Long,
        val address: String?,
        val recording_plan: Boolean,
        val live_id: String,
        val status: Boolean,
        val created_at: String,
        val updated_at: String,
        val is_exist: Boolean
):Serializable{

    fun bestTitle(): String {
        if (title.startsWith("방송중")) {
            return title.substring(4).trim()
        }
        return title
    }
}