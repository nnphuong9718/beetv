package com.example.android.architecture.blueprints.beetv.modules.player.models

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
 * Created by HQ on 2017/12/3.
 */
class VideoInfo : Comparable<Int> {
    var resCode //请求码
            : String? = null
    var position //列表中的位置
            = 0
    var title //标题
            : String? = null
    var playURL //播放地址
            : String? = null
    var coverURL //缩略图地址
            : String? = null
    var isPlay //可不可以播放
            = false
    var currentPosition //播放进度
            : Long = 0
    var duration //总时长
            : Long = 0
    var videoType //视频类型
            : String? = null
    var playCount //播放次数
            = 0

    override fun compareTo(integer: Int): Int {
        return position - integer
    }
}