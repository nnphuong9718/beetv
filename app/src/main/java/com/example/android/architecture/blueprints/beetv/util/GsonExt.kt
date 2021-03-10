package com.example.android.architecture.blueprints.beetv.util

import com.google.gson.Gson

inline fun <reified T> String.toObject() = Gson().fromJson(this, T::class.java)!!

fun <E> MutableList<E>.toJson() = Gson().toJson(this)!!