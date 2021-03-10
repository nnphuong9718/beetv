package com.example.android.architecture.blueprints.beetv.modules.player.inteface

import retrofit2.Call

interface RequestView {
    //    void onResponse(Call call, Bean bean, int id);
    fun onFailure(call: Call<*>?, throwable: Throwable?, id: Int)
}