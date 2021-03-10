package com.example.android.architecture.blueprints.beetv.data.models



data class BNotice(
   val id: String,
   val title: String,
   val description: String,
   val start_time: String,
   val end_time: String,
   val status: Boolean,
   val created_at: String,
   val updated_at: String,
   val device_id: String
) {}