package com.example.android.architecture.blueprints.beetv.data.models



data class BAds(
   val id: String,
   val image: String,
   val position: Int,
   val title: String,
   val description: String,
   val start_time: String,
   val end_time: String,
   val created_at_unix_timestamp: String,
   val status: Boolean,
   val created_at: String,
   val updated_at: String
) {}