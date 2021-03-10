package com.example.android.architecture.blueprints.beetv.helpers
import com.example.android.architecture.blueprints.beetv.SingleLiveEvent
import com.example.android.architecture.blueprints.beetv.data.models.BFavorite

class AppEvent {
    val refreshRemoveFavoriteList = SingleLiveEvent<String>()
    val refreshAddFavoriteList = SingleLiveEvent<BFavorite>()
}