package com.example.android.architecture.blueprints.beetv.widgets

import android.R.attr
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.marginLeft
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.android.architecture.blueprints.beetv.BuildConfig
import com.example.android.architecture.blueprints.beetv.R


open class ZoomSelectionThumbLayout : FrameLayout {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }


    var mFocusAnimation: Animation? = null
    var mUnFocusAnimation: Animation? = null

    private fun init(attrs: AttributeSet?) {
        clipChildren = false
        if (BuildConfig.FLAVOR.equals("beetv")) {
            mFocusAnimation = AnimationUtils.loadAnimation(context, R.anim.focus_animation)
            mUnFocusAnimation = AnimationUtils.loadAnimation(context, R.anim.unfocus_animation)
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (BuildConfig.FLAVOR.equals("beetv")) {
            if (gainFocus) {
                if (getChildAt(0) is ImageView) {
                    val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    lp.setMargins(0)
                    (getChildAt(0) as ImageView).layoutParams = lp

                }
                setPadding(6)
                setBackgroundResource(R.drawable.border_movie_red)
                startAnimation(mFocusAnimation)
                mFocusAnimation?.fillAfter = true
            } else {
                if (getChildAt(0) is ImageView) {
                    val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    lp.setMargins(4)
                    (getChildAt(0) as ImageView).layoutParams = lp

                }
                setPadding(2)
                setBackgroundResource(R.drawable.border_color)
                startAnimation(mUnFocusAnimation)
                mUnFocusAnimation?.fillAfter = true
            }
        }
    }
}