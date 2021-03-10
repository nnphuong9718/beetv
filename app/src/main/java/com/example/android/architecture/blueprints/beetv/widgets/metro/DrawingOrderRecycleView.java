/*
 * Copyright (C) 2016 hejunlin <hejunlin2013@gmail.com>
 * Github:https://github.com/hejunlin2013/TVSample
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
package com.example.android.architecture.blueprints.beetv.widgets.metro;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hejunlin on 2015/7/19.
 * blog: http://blog.csdn.net/hejjunlin
 */
public class DrawingOrderRecycleView extends RecyclerView {
    private int position = 0;

    public DrawingOrderRecycleView(Context context) {
        super(context);
    }

    public DrawingOrderRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setChildrenDrawingOrderEnabled(true);
    }

    public void setCurrentPosition(int pos) {
        this.position = pos;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
            View focused = findFocus();
            int pos = indexOfChild(focused);
            if (pos >= 0 && pos < getChildCount()) {
                setCurrentPosition(pos);
                postInvalidate();
            }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View v = getFocusedChild();
        int pos = indexOfChild(v);
        if (pos >= 0 && pos < childCount)
            setCurrentPosition(pos);
        if (i == childCount - 1) {
            return position < childCount ? position : childCount - 1;
        }
        if (i == position) {
            return childCount - 1;
        }
        return i;
    }

}
