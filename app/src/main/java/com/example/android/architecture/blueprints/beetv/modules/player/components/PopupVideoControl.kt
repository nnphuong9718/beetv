package com.example.android.architecture.blueprints.beetv.modules.player.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.example.android.architecture.blueprints.beetv.R

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
 * Created by HQ on 2017/12/17.
 */
class PopupVideoControl @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : PopupWindow(context, attrs, defStyleAttr), View.OnClickListener {
    private var pause: TextView? = null
    private var previousSong: TextView? = null
    private var nextSong: TextView? = null
    private var soundEffect: TextView? = null
    private var yuanBan: TextView? = null
    private var songList: TextView? = null
    private var mPopView: View? = null
    private var mListener: OnItemClickListener? = null

    /**
     * 初始化
     *
     * @param context
     */
    private fun init(context: Context) {
        mPopView = LayoutInflater.from(context).inflate(R.layout.pop_control, null)
        pause = mPopView?.findViewById<View>(R.id.pause) as TextView
        nextSong = mPopView?.findViewById<View>(R.id.next_song) as TextView
        previousSong = mPopView?.findViewById<View>(R.id.previous_song) as TextView
        soundEffect = mPopView?.findViewById<View>(R.id.sound_effect) as TextView
        yuanBan = mPopView?.findViewById<View>(R.id.yuan_ban) as TextView
        songList = mPopView?.findViewById<View>(R.id.song_list) as TextView
        pause!!.setOnClickListener(this)
        nextSong!!.setOnClickListener(this)
        previousSong!!.setOnClickListener(this)
        soundEffect!!.setOnClickListener(this)
        yuanBan!!.setOnClickListener(this)
        songList!!.setOnClickListener(this)
    }

    /**
     * 设置窗口的相关属性
     */
    private fun setPopupWindow() {
        this.contentView = mPopView // 设置View
        this.width = ViewGroup.LayoutParams.MATCH_PARENT // 设置弹出窗口的宽
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT // 设置弹出窗口的高
        this.isFocusable = true // 设置弹出窗口可
        this.animationStyle = R.style.popwindow_anim_style // 设置动画
        setBackgroundDrawable(ColorDrawable(0x00000000)) // 设置背景透明
        this.isOutsideTouchable = true
    }

    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    interface OnItemClickListener {
        fun setOnItemClick(v: View?)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    override fun onClick(v: View) {
        if (mListener != null) {
            mListener!!.setOnItemClick(v)
        }
    }

    init {
        init(context)
        setPopupWindow()
    }

    fun setEnableNextButton(isEnable : Boolean){
        nextSong?.isEnabled = isEnable
    }

    fun setEnablePreviousButton(isEnable : Boolean){
        previousSong?.isEnabled = isEnable
    }

    fun setRequestFocus(isFocus : Boolean){
//        nextSong?.isFocusable = isFocus
//        previousSong?.isFocusable = isFocus
//        pause?.isFocusable = isFocus
////        soundEffect?.isFocusable = isFocus
////        yuanBan?.isFocusable = isFocus
////        songList?.isFocusable = isFocus
//        mPopView?.isFocusable = isFocus
    }
}