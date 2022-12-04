package com.JRead;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

/*
 * 计算键盘高度
 */
public class HeightProvider extends PopupWindow implements ViewTreeObserver.OnGlobalLayoutListener {
    private final Activity mActivity;
    private final View rootView;
    private HeightListener listener;
    private int heightMax; // 记录popup内容区的最大高度

    public HeightProvider(Activity activity) {
        super(activity);
        this.mActivity = activity;

        // 基础配置
        rootView = new View(activity);
        setContentView(rootView);

        // 监听全局Layout变化
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        setBackgroundDrawable(new ColorDrawable(0));

        // 设置宽度为0，高度为全屏
        setWidth(0);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        // 设置键盘弹出方式
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
    }

    public HeightProvider init() {
        if (!isShowing()) {
            final View view = mActivity.getWindow().getDecorView();
            // 延迟加载popup window，如果不加延迟就会报错
            view.post(() -> showAtLocation(view, Gravity.NO_GRAVITY, 0, 0));
        }
        return this;
    }

    public void setHeightListener(HeightListener listener) {
        this.listener = listener;
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        if (rect.bottom > heightMax) {
            heightMax = rect.bottom;
        }

        // 两者的差值就是键盘的高度
        int keyboardHeight = heightMax - rect.bottom;
        if (listener != null) {
            listener.onHeightChanged(keyboardHeight);
        }
    }

    public interface HeightListener {
        void onHeightChanged(int height);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Log.e("keyboard height provider","onDestroy");
        dismiss();
    }
}

