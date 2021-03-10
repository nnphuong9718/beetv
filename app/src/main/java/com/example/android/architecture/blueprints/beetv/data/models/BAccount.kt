package com.example.android.architecture.blueprints.beetv.data.models

import com.example.android.architecture.blueprints.beetv.util.formatID

data class BAccount(
        val id: String, // user id in DB
        val username: String,
        val fullname: String,
        val birthday: String,
        val user_type: String,
        val email: String,
        val mac: String,
        val batch_id: String,
        val node_id: String,
        val logical_id: String, // display user id
        val created_at_unix_timestamp: String,
        val status: Boolean,
        val created_at: String,
        val updated_at: String,
        val trial_type: String,
        val trial_exp_time: String,
        val role: String,
        val recharge_cards: List<BBilling>,
        val expiration_date: Long,
        val node: BNode,
        val agent: BAgent
) {

    fun getRawID():  String {
        return logical_id
    }

    fun getUserIdentify(): String {
        return logical_id?.formatID()
    }
}

data class BLoginRequest(
        val logical_id: String,
        val password: String,
        val physical_id: String
) {}


data class BUpdateProfileRequest(
        val mac: String,
        val latest_access_time: Long,
        val latitude: Double,
        val longitude: Double,
        val ip_address: String,
        val appVersion: String
){}