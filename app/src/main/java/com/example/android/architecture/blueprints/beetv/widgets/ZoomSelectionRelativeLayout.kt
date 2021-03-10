package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.android.architecture.blueprints.beetv.BuildConfig
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.util.setContentView
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout


open class ZoomSelectionRelativeLayout : RelativeLayout {

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
                startAnimation(mFocusAnimation)
                mFocusAnimation?.fillAfter = true
            } else {
                startAnimation(mUnFocusAnimation)
                mUnFocusAnimation?.fillAfter = true
            }
        }
    }
}