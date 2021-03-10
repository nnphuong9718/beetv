package com.example.android.architecture.blueprints.beetv.modules.player.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.manager.MediaPlayerManager
import com.example.android.architecture.blueprints.beetv.modules.player.models.VideoInfo
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.Utils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.DiscontinuityReason
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.drm.ExoMediaCrypto
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import java.util.*

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
 * Created by HQ on 2017/12/03.
 */
class ExoVideoView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {
    /****日志 */
    private val TAG = "ExoVideoView"
    /**********************************控件相关 */
    /***播放显示 */
    private var ivPlayIcon: ImageView? = null

    /***加载 */
    private var progressBar: ProgressBar? = null

    private var movieLoadingIndicator: ImageView? = null

    private var liveVideoLoadingIndicator: ImageView? = null

    /***渲染 */
    private var textureview: TextureView? = null

    /***标题 */
    private var exoTitle: TextView? = null

    /***当前进度 */
    private var exoPosition: TextView? = null

    /***进度条 */
    private var exoProgress: TimeBar? = null

    /***总时长 */
    private var exoDuration: TextView? = null

    private var isLoaded = false

    /***显示时间进度的根布局 */
    private var exoTime: View? = null
    private var livePlayer: View? = null
    /**********************************控件相关 */
    /****播放器核心 */
    private var mExoPlayer: SimpleExoPlayer? = null
    //    private AspectRatioFrameLayout mAspectRatioLayout;
    /***状态监听器 */
    private var componentListener: ComponentListener? = null
    private var completeListener: CompleteListener? = null
    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())

    /****事件日志输出 */
    private var eventLogger: EventLogger? = null

    /***渲染器，默认有四个渲染器 */
    private var trackSelector: MappingTrackSelector? = null
    private val hashMap = HashMap<String, VideoInfo>()

    /***显示的时间 */
    private val showTimeoutMs = 15000
    private val rewindMs = 5000
    private val fastForwardMs = 15000
    private var duration: Long = 0
    private var isFocus = true
    private var isPause = false
    var isShowProgressBar = true
    private var progressVod: ProgressBar? = null
    private val hideAction = Runnable { hide() }
    var updateProgress = Runnable { updateProgress() }
    var hideProgress = Runnable { ivPlayIcon!!.visibility = GONE }
    private fun initPlayer() {
        componentListener = ComponentListener()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        eventLogger = EventLogger(trackSelector)
        //        mExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(AppOtt.mContext, null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON), trackSelector);
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector!!)
        mExoPlayer!!.setVideoTextureView(textureview)
        mExoPlayer!!.addListener(componentListener!!)
        mExoPlayer!!.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
                Utils.print(width.toString() + "x" + height + ", ratio:" + pixelWidthHeightRatio)
                var orgWidth = textureview!!.width.toFloat()
                var orgHeight = textureview!!.height.toFloat()
                var viewWidth = orgWidth
                var viewHeight = orgHeight
                var scaleX = 1.0f
                var scaleY = 1.0f

                when (MediaPlayerManager.getInstance().getResolution(context)) {
                    Constants.MediaResolution.R16P9 -> {
                        // Todo scale full width
                        scaleY = (viewWidth / width) / (viewHeight / height)
                        scaleX = (viewHeight / height) / (viewWidth / width)
                        if (scaleX > scaleY) {
                            scaleX = 1.0f
                        } else {
                            scaleY = 1.0f
                        }
                    }

                    Constants.MediaResolution.R4P3 -> {
                        // Todo scale full height
                        viewWidth = orgHeight * 4 / 3
                        scaleX = (orgHeight * 4 / 3) / orgWidth
                        scaleY = (viewWidth / width) / (viewHeight / height)
                    }

                    Constants.MediaResolution.AUTOMATION -> {
                        // Todo scale all
                        if (width > viewWidth && height > viewHeight) {
                            scaleX = width / viewWidth;
                            scaleY = height / viewHeight;
                        } else if (width < viewWidth && height < viewHeight) {
                            scaleY = viewWidth / width;
                            scaleX = viewHeight / height;
                        } else if (viewWidth > width) {
                            scaleY = (viewWidth / width) / (viewHeight / height);
                        } else if (viewHeight > height) {
                            scaleX = (viewHeight / height) / (viewWidth / width);
                        }
                    }
                }

                val pivotPointX = (orgWidth / 2).toInt()
                val pivotPointY = (orgHeight / 2).toInt()
                val matrix = Matrix()
                matrix.setScale(scaleX, scaleY, pivotPointX.toFloat(), pivotPointY.toFloat())
                textureview!!.setTransform(matrix)
            }
        })
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.exo_videoview, this)
        setBackgroundColor(Color.BLACK)
        ivPlayIcon = findViewById<View>(R.id.iv_play_icon) as ImageView
        progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        movieLoadingIndicator = findViewById<View>(R.id.iv_movie_indicator) as ImageView
        liveVideoLoadingIndicator = findViewById<View>(R.id.iv_live_video_indicator) as ImageView
        textureview = findViewById<View>(R.id.textureview) as TextureView
        exoTitle = findViewById<View>(R.id.exo_title) as TextView
        exoPosition = findViewById<View>(R.id.exo_position) as TextView
        exoProgress = findViewById<View>(R.id.exo_progress) as TimeBar
        exoDuration = findViewById<View>(R.id.exo_duration) as TextView
        progressVod = findViewById<View>(R.id.progress_vod) as ProgressBar
        exoTime = findViewById(R.id.exo_time)
        livePlayer = findViewById<View>(R.id.live_control_layout)
        //        mAspectRatioLayout = (AspectRatioFrameLayout) findViewById(R.id.espect_ratio);
    }

    fun setFocusButton(isFocus: Boolean) {
        this.isFocus = isFocus
        ivPlayIcon?.isFocusable = isFocus
    }

    fun play(videoInfo: VideoInfo, isPlay: Boolean = true) {
        hashMap[videoInfo.playURL!!] = videoInfo
        duration = videoInfo.duration
        if (isPlay)
            play(buildMediaSource(Uri.parse(videoInfo.playURL), null))
        exoTitle!!.text = videoInfo.title
    }

    fun requestZoomRatio() {
//        if (player != null) {
//            mAspectRatioLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//            textureview.setAspectRatio(16, 9);
//            mExoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//        }
    }

    fun requestAutoRatio() {
//        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
    }

    /**
     * 播放单个视频
     */
    fun play(mediaSource: MediaSource?) {
        if (mExoPlayer != null) {
            mExoPlayer!!.playWhenReady = true
            mExoPlayer!!.prepare(mediaSource!!)
        }
        show(true)
        ivPlayIcon!!.visibility = GONE
    }

    /**
     * 设置播放器
     *
     * @param player
     */
    fun setPlayer(player: SimpleExoPlayer?) {
        if (mExoPlayer === player) {
            return
        }
        if (mExoPlayer != null) {
            mExoPlayer!!.removeListener(componentListener!!)
            mExoPlayer!!.clearVideoTextureView(textureview)
        }
        mExoPlayer = player
        if (player != null) {
            player.setVideoTextureView(textureview)
            player.addListener(componentListener!!)
        }
    }

    fun getExoPlayer(): SimpleExoPlayer? {
        return mExoPlayer
    }

    /***
     * 显示控制和播放列表
     */
    fun show(isHide: Boolean) {
        removeCallbacks(hideAction)
        updateProgress()
        showAnim()
        if (isHide) hideAfterTimeout()
    }

    /**
     * 显示的动画
     */
    private fun showAnim() {
        exoTime!!.visibility = VISIBLE
        exoTitle!!.visibility = VISIBLE

    }

    /**
     * 隐藏控制和播放列表
     */
    fun hide() {
        removeCallbacks(updateProgress)
        hideAnim()
    }

    /***
     * 更新播放进度
     */
    fun updateProgress() {
        if (mExoPlayer == null) return // Todo this block was called after exit movie
        val contentPosition = mExoPlayer!!.contentPosition
        val duration = if (mExoPlayer!!.duration < 0) 0 else mExoPlayer!!.duration
        val bufferPosition = mExoPlayer!!.bufferedPosition
        val contentTime = getStringForTime(formatBuilder, formatter, contentPosition)
        val durationTime = getStringForTime(formatBuilder, formatter, duration)
        exoProgress!!.setPosition(contentPosition)
        exoProgress!!.setBufferedPosition(bufferPosition)
        exoProgress!!.setDuration(duration)

        if (duration > 0) {
            progressVod!!.progress = ((contentPosition.toDouble() / duration) * 100).toInt()
        }
        exoPosition!!.text = if (contentTime?.length == 5) "00:$contentTime" else contentTime
        exoDuration!!.text = if (durationTime?.length == 5) "00:$durationTime" else durationTime
        val playbackState = if (mExoPlayer == null) Player.STATE_IDLE else mExoPlayer!!.playbackState
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            postDelayed(updateProgress, 1000)
        }
    }

    /**
     * 隐藏动画
     */
    private fun hideAnim() {
        exoTime!!.visibility = GONE
        exoTitle!!.visibility = GONE
    }

    /**
     * 显示多长时间隐藏
     */
    private fun hideAfterTimeout() {
        if (mExoPlayer?.playWhenReady == true) {
            removeCallbacks(hideAction)
            if (showTimeoutMs > 0) {
                postDelayed(hideAction, showTimeoutMs.toLong())
            }
        }
    }

    /***
     * 构建媒体资源
     * @param uri
     * @param overrideExtension
     * @return
     */
    fun buildMediaSource(uri: Uri, overrideExtension: String?): MediaSource {
        val type = Util.inferContentType(if (TextUtils.isEmpty(overrideExtension)) ".$overrideExtension" else uri.lastPathSegment)
        return when (type) {
            C.TYPE_OTHER -> {
                val drmSessionManager = DrmSessionManager.getDummyDrmSessionManager<ExoMediaCrypto>()
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "BeeTv"))
                val mediaSource: MediaSource
                mediaSource = if (uri.toString().startsWith("rtmp://")) {
                    ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                            .setDrmSessionManager(drmSessionManager)
                            .createMediaSource(uri)
                } else {
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                            .setDrmSessionManager(drmSessionManager)
                            .setContinueLoadingCheckIntervalBytes(5 * 1024 * 1024)
                            .createMediaSource(uri)
                }
                mediaSource
            }
            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    /**
     * 构建数据工厂
     *
     * @param useBandwidthMeter 是否添加流量统计
     * @return
     */
    private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
        return buildDataSourceFactory(if (useBandwidthMeter) BANDWIDTH_METER else null)
    }

    /**
     * 构建数据工厂
     *
     * @param bandwidthMeter 流量统计
     * @return
     */
    fun buildDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): DataSource.Factory {
        return DefaultDataSourceFactory(context.applicationContext, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter))
    }

    /**
     * 构建http数据工厂
     *
     * @param bandwidthMeter 流量统计
     * @return
     */
    fun buildHttpDataSourceFactory(bandwidthMeter: DefaultBandwidthMeter?): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(Util.getUserAgent(context.applicationContext, TAG), bandwidthMeter)
    }

    private inner class ComponentListener : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updateProgress()
        }

        /**
         * 轨道改变
         *
         * @param trackGroups
         * @param trackSelections
         */
        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}

        /**
         * 加载状态改变
         *
         * @param isLoading
         */
        override fun onLoadingChanged(isLoading: Boolean) {}

        /**
         * 播放状态的改变
         *
         * @param playWhenReady
         * @param playbackState
         */
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            updateProgress()
            Log.d(TAG, "onPlayerStateChanged: $playWhenReady----$playbackState")
            if (playbackState == Player.STATE_READY) {
                progressBar!!.visibility = GONE

                hideAfterTimeout()

                if (livePlayer?.visibility == View.VISIBLE) {
                    liveVideoLoadingIndicator!!.visibility = GONE
                    liveVideoLoadingIndicator!!.clearAnimation()
                } else {
                    movieLoadingIndicator!!.visibility = GONE
                    movieLoadingIndicator!!.clearAnimation()
                }
                isLoaded = true

                //FIXME this is a workaround to wait until backend fixed the root cause of this issue: https://trello.com/c/m8xBbdq8
                //FIXME we have to remove this code later
                // If we are seeking this player (the playing time is > 5 second)
//                if (getExoPlayer() != null && getExoPlayer()?.currentPosition!! > 5000) {
//                    //1. mute
//                    val oldVolume = getExoPlayer()?.volume
//                    getExoPlayer()?.volume = 0f
//
//                    //2. settimeout 6s then unmute
//                    Handler().postDelayed({
//                        getExoPlayer()?.volume = oldVolume as Float
//                    }, 6000)
//                }
            } else {
                progressBar!!.visibility = GONE //Client wants this

                if (livePlayer?.visibility == View.VISIBLE) {
                    liveVideoLoadingIndicator!!.visibility = VISIBLE
                    liveVideoLoadingIndicator!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.marquee))
                } else {
                    if (!isPause) {
                        movieLoadingIndicator!!.visibility = VISIBLE
                        movieLoadingIndicator!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.marquee))
                    }
                }
            }

            if (playbackState == Player.STATE_ENDED && completeListener != null) {
                completeListener!!.onComplete()
            }
        }

        /**
         * 播放模式的改变
         *
         * @param repeatMode
         */
        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

        /**
         * 播放错误
         *
         * @param error
         */
        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onPositionDiscontinuity(@DiscontinuityReason reason: Int) {
            updateProgress()
        }

        /**
         * 播放参数的改变
         *
         * @param playbackParameters
         */
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
        override fun onSeekProcessed() {}
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mExoPlayer!!.volume = leftVolume // setVolume(leftVolume,rightVolume);
    }

    fun fastForward() {
        if (fastForwardMs <= 0) {
            return
        }
        val durationMs = mExoPlayer!!.duration
        var seekPositionMs = mExoPlayer!!.currentPosition + fastForwardMs
        if (durationMs != C.TIME_UNSET) {
            seekPositionMs = Math.min(seekPositionMs, durationMs)
        }
        mExoPlayer!!.seekTo(seekPositionMs)
    }

    fun rewind() {
        if (rewindMs <= 0) {
            return
        }
        mExoPlayer!!.seekTo(Math.max(mExoPlayer!!.currentPosition - rewindMs, 0))
    }

    //    public boolean isPlaying() {
    //        return mExoPlayer.getPlaybackState() == Player.STATE_READY && mExoPlayer.getPlayWhenReady();
    //    }
    fun pause() {
        if (mExoPlayer == null) {
            return
        }
        if (mExoPlayer!!.playWhenReady) {
            mExoPlayer!!.playWhenReady = false
        }
    }

    fun isPause() : Boolean{
        if (mExoPlayer == null) {
            return false
        }
        return mExoPlayer?.playWhenReady ?: false
    }

    fun stop() {
        if (mExoPlayer == null) {
            return
        }
        mExoPlayer!!.stop(true)

    }

    fun resume() {
        if (mExoPlayer == null) {
            return
        }
        if (!mExoPlayer!!.playWhenReady) {
            mExoPlayer!!.playWhenReady = true
        }
    }


    fun release() {
        if (mExoPlayer == null) {
            return
        }
        mExoPlayer!!.release()
    }

    fun destroy() {
        if (mExoPlayer != null) {
            removeCallbacks(updateProgress)
            removeCallbacks(hideAction)
            removeCallbacks(hideProgress)
            mExoPlayer!!.release()
            mExoPlayer = null
            trackSelector = null
            eventLogger = null
        }
    }

    fun updatePlayerButton() {
        if (mExoPlayer!!.playWhenReady) {
            removeCallbacks(hideProgress)
            ivPlayIcon!!.visibility = VISIBLE
            show(false)
            movieLoadingIndicator!!.visibility = GONE
            movieLoadingIndicator!!.clearAnimation()
            isPause = true
        } else {
            postDelayed(hideProgress, 100)
            if (!isLoaded) {
                movieLoadingIndicator!!.visibility = VISIBLE
                movieLoadingIndicator!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.marquee))
            }
            isPause = false
        }
        mExoPlayer!!.playWhenReady = !mExoPlayer!!.playWhenReady
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isFocus) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                show(true)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                updatePlayerButton()
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                show(true)
                fastForward()
                return true
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                show(true)
                rewind()
                return true
            }

        }
        return super.onKeyDown(keyCode, event)

    }

    fun setCompleteListener(completeListener: CompleteListener?) {
        this.completeListener = completeListener
    }

    interface CompleteListener {
        fun onComplete()
    }

    companion object {
        /**
         * 流量统计
         */
        private val BANDWIDTH_METER = DefaultBandwidthMeter()

        @SuppressLint("InlinedApi")
        fun isHandledMediaKey(keyCode: Int): Boolean {
            return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }
    }

    init {
        init() //初始化
        initPlayer() //初始化播放器
    }

    fun getStringForTime(builder: java.lang.StringBuilder, formatter: Formatter, timeMs: Long): String? {
        var timeMs = timeMs
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0
        }
        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        builder.setLength(0)
        return if (hours > 0) formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString() else formatter.format("%02d:%02d", minutes, seconds).toString()
    }
}