package xyz.sleekstats.softball.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Eddie on 2/21/2018.
 */

public class CustomViewPager extends ViewPager {

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Override of the onInterceptTouchEvent which allows swiping to be disabled when chart is selected
     *
     * @param ev The MotionEvent object
     * @return Call to super if true, otherwise returns false
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

}
