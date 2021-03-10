package com.example.android.architecture.blueprints.beetv.data.models

data class BNode(
   val id: String,
   val node_ip: String,
   val title: String,
   val created_at_unix_timestamp: String,
   val status: Boolean,
   val created_at: String,
   val updated_at: String
) {}