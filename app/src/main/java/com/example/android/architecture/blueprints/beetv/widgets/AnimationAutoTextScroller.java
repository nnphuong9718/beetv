package com.example.android.architecture.blueprints.beetv.widgets;

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class AnimationAutoTextScroller {
    Animation animator;
    TextView scrollingTextView;
    boolean shouldStartAnimation = true;


    public AnimationAutoTextScroller(TextView tv, float screenwidth, int duration, Animation.AnimationListener listener,int textWidth, boolean isFirst ) {
        if (textWidth < screenwidth) {
            shouldStartAnimation = false;
            return;
        }


        this.scrollingTextView = tv;

        if (isFirst) {
            this.animator =  new TranslateAnimation(0, -textWidth, 0f, 0f);
        } else {
            this.animator =  new TranslateAnimation(screenwidth, -textWidth, 0f, 0f);
            float velocity = (float) textWidth / duration;
            duration = Math.round((screenwidth + textWidth) / velocity);
        }
        this.animator.setInterpolator(new LinearInterpolator());
        this.animator.setDuration(duration);
//        this.animator.setFillAfter(true);
        this.animator.setRepeatMode(Animation.RESTART);
//        this.animator.setRepeatCount(Animation.);
        animator.setAnimationListener(listener);
    }


    public void setScrollingText(String text) {
        if (shouldStartAnimation) {
            this.scrollingTextView.setText(text);
        }
    }

    public void start() {
        if (shouldStartAnimation) {
            this.scrollingTextView.setSelected(true);
            this.scrollingTextView.startAnimation(this.animator);
        }
    }
}
