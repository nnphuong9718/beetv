package com.example.android.architecture.blueprints.beetv.data.models

import com.google.gson.annotations.SerializedName

data class Record(
    val results: Results
)

data class Results(
        @SerializedName(value="object")
    val item: Object

)

data class Object(
    val exist: Boolean
)


