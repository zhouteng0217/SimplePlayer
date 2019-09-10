package com.zt.core.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

import com.zt.core.base.IVideoView;

import java.lang.ref.WeakReference;

/**
 * Created by zhouteng on 2019-9-10
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager {

    private WeakReference<IVideoView> weakReference;

    private WindowManager windowManager;

    private int originX, originY;

    private static FloatVideoManager instance;

    public FloatVideoManager getInstance() {
        if (instance == null) {
            instance = new FloatVideoManager();
        }
        return instance;
    }

    public void startFloatVideo(IVideoView videoView) {
        weakReference = new WeakReference<>(videoView);
        originX = 0;
        originY = 0;
        createFloatVideo();
    }

    private void createFloatVideo() {
        IVideoView videoView = weakReference.get();
        if (videoView == null) {
            return;
        }

        Context context = videoView.getPlayView().getContext().getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                context.startActivity(intent);
                return;
            }
        }

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT | Gravity.START;
        wmParams.width = videoView.getTinyVideoViewWidth();
        wmParams.height = videoView.getTinyVideoViewHeight();
        wmParams.x = originX;
        wmParams.y = originY;

        windowManager.addView(videoView.getPlayView(), wmParams);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }
}
