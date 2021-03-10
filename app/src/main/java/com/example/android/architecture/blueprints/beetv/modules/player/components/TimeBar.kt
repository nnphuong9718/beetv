/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.beetv.modules.player.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.example.android.architecture.blueprints.beetv.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.util.Util
import java.util.*

class TimeBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val seekBounds: Rect
    private val progressBar: Rect
    private val bufferedBar: Rect
    private val scrubberBar: Rect
    private val playedPaint: Paint
    private val bufferedPaint: Paint
    private val unplayedPaint: Paint
    private val scrubberPaint: Paint
    private var barHeight = 0
    private var touchTargetHeight = 0
    private var scrubberEnabledSize = 100
    private val scrubberPadding: Int
    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private var duration: Long
    private var position: Long = 0
    private var bufferedPosition: Long = 0
    fun setPosition(position: Long) {
        this.position = position
        contentDescription = progressText
        update()
    }

    fun setBufferedPosition(bufferedPosition: Long) {
        this.bufferedPosition = bufferedPosition
        update()
    }

    fun setDuration(duration: Long) {
        this.duration = duration
        update()
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.save()
        drawTimeBar(canvas)
        drawPlayhead(canvas)
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = if (heightMode == MeasureSpec.UNSPECIFIED) touchTargetHeight else if (heightMode == MeasureSpec.EXACTLY) heightSize else Math.min(touchTargetHeight, heightSize)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        val barY = (height - touchTargetHeight) / 2
        val seekLeft = paddingLeft
        val seekRight = width - paddingRight
        val progressY = barY + (touchTargetHeight - barHeight) / 2
        seekBounds[seekLeft, barY, seekRight] = barY + touchTargetHeight
        progressBar[seekBounds.left + scrubberPadding, progressY, seekBounds.right - scrubberPadding] = progressY + barHeight
        update()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
    }

    private fun update() {
        bufferedBar.set(progressBar)
        scrubberBar.set(progressBar)
        val newScrubberTime = position
        if (duration > 0) {
            val bufferedPixelWidth = (progressBar.width() * bufferedPosition / duration).toInt()
            bufferedBar.right = Math.min(progressBar.left + bufferedPixelWidth, progressBar.right)
            val scrubberPixelPosition = (progressBar.width() * newScrubberTime / duration).toInt()
            scrubberBar.right = Math.min(progressBar.left + scrubberPixelPosition, progressBar.right)
        } else {
            bufferedBar.right = progressBar.left
            scrubberBar.right = progressBar.left
        }
        invalidate(seekBounds)
    }

    private fun drawTimeBar(canvas: Canvas) {
        val progressBarHeight = progressBar.height()
        val barTop = progressBar.centerY() - progressBarHeight / 2
        val barBottom = barTop + progressBarHeight
        if (duration <= 0) {
            canvas.drawRect(progressBar.left.toFloat(), barTop.toFloat(), progressBar.right.toFloat(), barBottom.toFloat(), unplayedPaint)
            return
        }
        var bufferedLeft = bufferedBar.left
        val bufferedRight = bufferedBar.right
        val progressLeft = Math.max(Math.max(progressBar.left, bufferedRight), scrubberBar.right)
        if (progressLeft < progressBar.right) {
            canvas.drawRect(progressLeft.toFloat(), barTop.toFloat(), progressBar.right.toFloat(), barBottom.toFloat(), unplayedPaint)
        }
        bufferedLeft = Math.max(bufferedLeft, scrubberBar.right)
        if (bufferedRight > bufferedLeft) {
            canvas.drawRect(bufferedLeft.toFloat(), barTop.toFloat(), bufferedRight.toFloat(), barBottom.toFloat(), bufferedPaint)
        }
        if (scrubberBar.width() > 0) {
            canvas.drawRect(scrubberBar.left.toFloat(), barTop.toFloat(), scrubberBar.right.toFloat(), barBottom.toFloat(), playedPaint)
        }
    }

    private fun drawPlayhead(canvas: Canvas) {
        if (duration <= 0) {
            return
        }
        val progressBarHeight = progressBar.height()
        val barTop = progressBar.centerY() - progressBarHeight / 2
        val barBottom = barTop + progressBarHeight
        val scrubberSize = scrubberEnabledSize
        val playheadRadius = scrubberSize / 2
        val playheadCenter = Util.constrainValue(scrubberBar.right, scrubberBar.left, progressBar.right)
//        canvas.drawCircle(playheadCenter.toFloat(), scrubberBar.centerY().toFloat(), playheadRadius.toFloat(), scrubberPaint)
        canvas.drawRect(playheadCenter.toFloat(), 0f, playheadCenter.toFloat() + 8f, progressBarHeight.toFloat(), scrubberPaint)
//        canvas.drawRect(playheadCenter - scrubberSize / 2.toFloat(), barTop.toFloat(), playheadCenter + scrubberSize / 2.toFloat(), barTop.toFloat(), scrubberPaint)
    }

    private val progressText: String
        private get() = Util.getStringForTime(formatBuilder, formatter, position)

    companion object {
        private const val DEFAULT_BAR_HEIGHT = 80
        private const val DEFAULT_TOUCH_TARGET_HEIGHT = 80
        private const val DEFAULT_PLAYED_COLOR = 0x33FF0000
        private const val DEFAULT_SCRUBBER_COLOR = 0xFFFF0000
        private const val DEFAULT_SCRUBBER_ENABLED_SIZE = 100
        private fun dpToPx(displayMetrics: DisplayMetrics, dps: Int): Int {
            return (dps * displayMetrics.density + 0.5f).toInt()
        }

        private fun getDefaultScrubberColor(color: Int): Int {
//            return -0x10000 or playedColor
            return color
        }

        private fun getDefaultUnplayedColor(playedColor: Int): Long {
            return 0xCC000000
//            return -0x78000000 // | (playedColor & 0x00FFFFFF);
        }

        private fun getDefaultBufferedColor(color: Int): Int {
            return 0xCC0000 or (color and 0x00FFFFFF)
        }
    }

    init {
        seekBounds = Rect() //总体进度
        progressBar = Rect() //播放进度条
        bufferedBar = Rect() //缓冲
        scrubberBar = Rect() //指示器
        playedPaint = Paint() //播放画笔
        bufferedPaint = Paint() //缓冲画笔
        unplayedPaint = Paint() //没有播放画笔
        scrubberPaint = Paint() //指示器画笔
        scrubberPaint.isAntiAlias = true
        val res = context.resources
        val displayMetrics = res.displayMetrics
        val defaultBarHeight = dpToPx(displayMetrics, DEFAULT_BAR_HEIGHT)
        val defaultTouchTargetHeight = dpToPx(displayMetrics, DEFAULT_TOUCH_TARGET_HEIGHT)
        val defaultScrubberEnabledSize = dpToPx(displayMetrics, DEFAULT_SCRUBBER_ENABLED_SIZE)
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.TimeBar, 0,
                    0)
            try {
                barHeight = a.getDimensionPixelSize(R.styleable.TimeBar_bar_height, defaultBarHeight)
                touchTargetHeight = a.getDimensionPixelSize(R.styleable.TimeBar_touch_target_height, defaultTouchTargetHeight)
                scrubberEnabledSize = a.getDimensionPixelSize(R.styleable.TimeBar_scrubber_enabled_size, defaultScrubberEnabledSize)
//                val playedColor = a.getInt(R.styleable.TimeBar_played_color, DEFAULT_PLAYED_COLOR)
//                val scrubberColor = a.getInt(R.styleable.TimeBar_scrubber_color, getDefaultScrubberColor(DEFAULT_SCRUBBER_COLOR))
//                val bufferedColor = a.getInt(R.styleable.TimeBar_buffered_color, getDefaultBufferedColor(playedColor))
//                val unplayedColor = a.getInt(R.styleable.TimeBar_unplayed_color, getDefaultUnplayedColor(playedColor))

                val playedColor = a.getInt(R.styleable.TimeBar_played_color, DEFAULT_PLAYED_COLOR)
//                val scrubberColor = a.getInt(R.styleable.TimeBar_scrubber_color, getDefaultScrubberColor(DEFAULT_SCRUBBER_COLOR))
                val bufferedColor = getDefaultBufferedColor(playedColor)
//                val unplayedColor = getDefaultUnplayedColor(playedColor)
                playedPaint.color = Color.parseColor("#33FF0000")
                scrubberPaint.color = Color.parseColor("#FFFF513D")
                bufferedPaint.color = bufferedColor
                unplayedPaint.color = Color.parseColor("#CC000000")
            } finally {
                a.recycle()
            }
        } else {
            barHeight = defaultBarHeight
            touchTargetHeight = defaultTouchTargetHeight
            scrubberEnabledSize = defaultScrubberEnabledSize
            playedPaint.color = Color.parseColor("#33FF0000")
            scrubberPaint.color = Color.parseColor("#FFFF513D") // getDefaultScrubberColor(DEFAULT_SCRUBBER_COLOR)
            bufferedPaint.color = getDefaultBufferedColor(DEFAULT_PLAYED_COLOR)
            unplayedPaint.color = Color.parseColor("#CC000000") // getDefaultUnplayedColor(DEFAULT_PLAYED_COLOR)
        }
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        scrubberPadding = 0 // scrubberEnabledSize / 2
        duration = C.TIME_UNSET
        isFocusable = true
        setPosition(0)
    }
}