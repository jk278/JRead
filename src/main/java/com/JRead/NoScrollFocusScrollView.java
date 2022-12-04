package com.JRead;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

public class NoScrollFocusScrollView extends NestedScrollView {
    public NoScrollFocusScrollView(Context context) {
        super(context);
    }
    public NoScrollFocusScrollView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }
    public NoScrollFocusScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }
    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }
}
