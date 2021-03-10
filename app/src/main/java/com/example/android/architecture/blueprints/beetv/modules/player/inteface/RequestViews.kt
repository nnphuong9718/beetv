package com.example.android.architecture.blueprints.beetv.modules.player.inteface

import com.example.android.architecture.blueprints.beetv.modules.player.models.VideoInfo
import retrofit2.Call

/**
 * 　　    ()  　　  ()
 * 　　   ( ) 　　　( )
 * 　　   ( ) 　　　( )
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　┻　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * Created by HQ on 2017/11/17.
 */
interface RequestViews {
    //    void onResponse(Call call, Bean bean, int id);
    fun onFailure(call: Call<*>?, throwable: Throwable?, id: Int)
    fun onVideoList(videoInfos: List<VideoInfo?>?)
}