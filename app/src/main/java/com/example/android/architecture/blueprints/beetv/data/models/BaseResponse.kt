package com.example.android.architecture.blueprints.beetv.data.models

import com.google.gson.annotations.SerializedName

data class BaseResponseConfirm(
        val code: String,
        val type: String,
        val message: String, // this is error message
        val results: Any
){}

data class BaseFindOneResponse<T>(
        val code: String,
        val type: String,
        val message: String,
        val results: ResultFindOneResponse<T>
) {}

data class ResultFindOneResponse<T>(
        @SerializedName("object") val one: T
){}

data class BaseResponse<T>(
        val code: String,
        val type: String,
        val message: String,
        val results: ResultsResponse<T>?,
        val pagination: PagingResponse
) {}

data class ResultsResponse<T>(
    val objects: ObjectListResponse<T>,
    @SerializedName("object") val one: ObjectResponse<T>
){}

data class ObjectResponse<T>(
        val result: T,
        val rows: List<T>,
        val token: String
) {}

data class ObjectListResponse<T>(
   val count: Int,
   val rows: List<T>,
   val result: T,
   val token: String
) {}

data class PagingResponse(
        val current_page: Int,
        val total: Int,
        val next_page: Int,
        val prev_page: Int,
        val limit: Int
) {}