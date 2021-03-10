package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.util.hide
import com.example.android.architecture.blueprints.beetv.util.setContentView
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout

class SettingItemView : ConstraintLayout{
    private var mName: String? = null
    private var mIcon: Int = 0
    private lateinit var mTvName : TextView
    private lateinit var mTvValue : TextView
    private lateinit var mIvIcon : ImageView
    private lateinit var mMain : MetroItemFrameLayout
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
       val view =  setContentView(R.layout.layout_setting_item)
        mTvName = view.findViewById(R.id.tv_name)
        mIvIcon = view.findViewById(R.id.iv_icon)
        mTvValue = view.findViewById(R.id.tv_value)
        mMain = view.findViewById(R.id.main)
        loadAttrs(attrs)
        setupViews()
    }

    private fun loadAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        val types = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)
        mName = types.getString(R.styleable.SettingItemView_settingName)
        mIcon =  types.getResourceId(R.styleable.SettingItemView_settingIcon, 0)

        types.recycle()
    }

    private fun setupViews(){
        mTvName.setText(mName)
        mIvIcon.setImageResource(mIcon)
    }

    public fun setColor(resId : Int){

        mMain.setBackgroundColor(ContextCompat.getColor(context, resId))

        if (resId == R.color.alto){
            mTvName.setTextColor(ContextCompat.getColor(context,  R.color.black))
            mTvValue.setTextColor(ContextCompat.getColor(context,  R.color.black))
            mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)

        }else{
            mTvName.setTextColor(ContextCompat.getColor(context,  R.color.alto))
            mTvValue.setTextColor(ContextCompat.getColor(context,  R.color.boulder))
            mIvIcon.setColorFilter(ContextCompat.getColor(context, R.color.alto), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
    }


    public  fun setValue(value :String){
        if (value.length > 0) {
            mTvValue.text = value
        } else {
            mTvValue.hide()
        }
    }

}