package com.transcend.otg.Browser;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by henry_hsu on 2017/2/23.
 */

public class PagerSwipeRefreshLayout extends SwipeRefreshLayout {

    private float mPrevx;
    private float mPrevy;

    public PagerSwipeRefreshLayout(Context context) {
        super(context);
    }

    public PagerSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevx = MotionEvent.obtain(ev).getX();
                mPrevy = MotionEvent.obtain(ev).getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float evX = ev.getX();
                final float evy = ev.getY();
                float xDiff = Math.abs(evX - mPrevx);
                float yDiff = Math.abs(evy - mPrevy);

                if (xDiff > yDiff) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
