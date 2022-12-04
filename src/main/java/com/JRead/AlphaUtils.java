package com.JRead;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/*
 * 调节透明度
 */
public class AlphaUtils {
    public AlphaUtils(Window window, boolean down, int time, float unit) {
        gradualAlpha(window, down, time, unit);
    }

    // 设置屏幕透明度
    public void backgroundAlpha(Window window, float bgAlpha) {
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha= bgAlpha; // 0.0~1.0
        // 没这行会穿透整个应用，FLAG_DIM_BEHIND 表示当前背景变暗！！！！！！
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(lp);
    }
    // 渐变透明度
    private void gradualAlpha(Window window, boolean down, int time, float unit) {
        float alphaValue = 0.5f;

        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    backgroundAlpha(window,(float) msg.obj);
                }
            }
        };

        List<Float> list = new ArrayList<>();
        if(down) for(float e = 1-unit; e > alphaValue; e -= unit) list.add(e);
        else for(float e = alphaValue+unit; e < 1; e += unit) list.add(e);

        double[] arr = list.stream().mapToDouble(x -> x).toArray();
        new Thread(() -> {
            for (double alpha : arr) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = (float)alpha;
                mHandler.sendMessage(msg);
            }
        }).start();
    }
}
