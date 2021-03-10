package com.example.android.architecture.blueprints.beetv.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.util.setContentView
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout

class KeyboardItemView : ConstraintLayout{
    private var mCharacter: String? = null
    private lateinit var mTvCharacter : TextView
    private lateinit var mBtCharacter : MetroItemFrameLayout
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
       val view =  setContentView(R.layout.layout_keyboard_item)
        mTvCharacter = view.findViewById(R.id.tv_character)
        mBtCharacter = view.findViewById(R.id.bt_character)
        loadAttrs(attrs)
        setupViews()
    }

    private fun loadAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        val types = context.obtainStyledAttributes(attrs, R.styleable.KeyboardItemView)
        mCharacter = types.getString(R.styleable.KeyboardItemView_character)

        types.recycle()
    }

    private fun setupViews(){
        mTvCharacter.setText(mCharacter)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            setColor(R.color.sunsetOrange)
        } else {
            setColor(R.color.button_text_focus)
        }
    }

    public fun setColor(resId : Int){

        mBtCharacter.setBackgroundColor(ContextCompat.getColor(context, resId))

        if (resId == R.color.white){
           mTvCharacter.setTextColor(ContextCompat.getColor(context,  R.color.black))
        }else{
            mTvCharacter.setTextColor(ContextCompat.getColor(context,  R.color.white))
        }
    }

    fun getCharacter() : String = mTvCharacter.text.toString()

    fun setCharacter(text :String ){
        mTvCharacter.text = text
    }
    fun onClick(view: View) {

    }
}