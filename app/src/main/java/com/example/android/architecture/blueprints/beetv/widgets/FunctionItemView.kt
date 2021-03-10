package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.util.setContentView

class FunctionItemView : ZoomSelectionRelativeLayout {
    private var mName: String? = null
    private var mIcon: Int = 0
    private lateinit var mIvIcon : ImageView
    private lateinit var mTvName : TextView

    private lateinit var mMain : View
    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }


    private fun init(attrs: AttributeSet?) {
       val view =  setContentView(R.layout.item_function)
        mIvIcon = view.findViewById(R.id.iv_icon)
        mTvName = view.findViewById(R.id.tv_name)
        mMain = view.findViewById(R.id.main)
        loadAttrs(attrs)
        setupViews()
    }

    private fun loadAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        val types = context.obtainStyledAttributes(attrs, R.styleable.FuctionItemView)
        mName = types.getString(R.styleable.FuctionItemView_functionName)
        mIcon = types.getResourceId(R.styleable.FuctionItemView_functionIcon, 0)

        types.recycle()
    }

    private fun setupViews(){
        mIvIcon.setImageResource(mIcon)
        mTvName.setText(mName)
    }


    public fun setColor(resId : Int){
        if (resId == R.color.mineShaft){
            mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.alto), android.graphics.PorterDuff.Mode.MULTIPLY)
            mTvName.setTextColor(ContextCompat.getColor(context,  R.color.alto))
            mMain.setBackgroundResource(R.drawable.bg_mineshaft_radius_30)
        }else{
            mIvIcon.setColorFilter(ContextCompat.getColor(context,  R.color.mineShaft), android.graphics.PorterDuff.Mode.MULTIPLY)
            mTvName.setTextColor(ContextCompat.getColor(context,  R.color.mineShaft))
            mMain.setBackgroundResource(R.drawable.bg_alto_radius_30)
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (gainFocus) {
            mMain.setBackgroundResource(R.drawable.bg_mineshaft_radius_30_focus)
            mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.im_icon_focus))
            mTvName.setTextColor(ContextCompat.getColor(context, R.color.button_text_focus))
        } else {
            mMain.setBackgroundResource(R.drawable.bg_mineshaft_radius_30)
            mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.im_icon_unfocus))
            mTvName.setTextColor(ContextCompat.getColor(context, R.color.button_text_unfocus))
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }
}