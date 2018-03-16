package com.example.android.softballstatkeeper.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Created by Eddie on 3/12/2018.
 */

public class MyEditText extends AppCompatEditText {


    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

//    @Override
//    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        return AppCompatHintHelper.onCreateInputConnection(super.onCreateInputConnection(outAttrs),
//                outAttrs, this);
//    }
}
