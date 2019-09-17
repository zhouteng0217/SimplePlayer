package com.zt.core.player;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.zt.core.base.ITinyVideoView;
import com.zt.core.util.VideoUtils;

/**
 * Created by zhouteng on 2019-9-10
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager implements ITinyVideoView.TinyVideoViewListenr {

    private ITinyVideoView videoView;

    private WindowManager windowManager;

    private ITinyVideoView.LayoutParams layoutParams;

    private static FloatVideoManager instance;
    private WindowManager.LayoutParams wmParams;

    public static FloatVideoManager getInstance() {
        if (instance == null) {
            instance = new FloatVideoManager();
        }
        return instance;
    }

    public void startFloatVideo(ITinyVideoView videoView) {
        this.videoView = videoView;
        layoutParams = videoView.getFloatVideoLayoutParams();
        createFloatVideo();
    }

    private void createFloatVideo() {
        if (videoView == null) {
            return;
        }
        videoView.setTinyVideoViewListener(this);

        Context context = videoView.getPlayView().getContext();

        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT | Gravity.START;
        wmParams.width = layoutParams.width;
        wmParams.height = layoutParams.height;
        wmParams.x = layoutParams.x;
        wmParams.y = layoutParams.y;

        windowManager.addView(videoView.getPlayView(), wmParams);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }

    @Override
    public void closeVideoView() {
        if (videoView != null) {
            windowManager.removeViewImmediate(videoView.getPlayView());
        }
        videoView.getPlayView().destroy();
    }

    @Override
    public void backToNormalView() {

    }

    //region  移动小窗体

    private float xInScreen;
    private float yInScreen;

    private float xInView;
    private float yInView;

    @Override
    public boolean onTouch(MotionEvent event) {

        xInScreen = event.getRawX();
        yInScreen = event.getRawY() - VideoUtils.getStatusBarHeight(videoView.getPlayView().getContext());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void updateViewPosition() {

        wmParams.y = (int) (yInScreen - yInView);
        wmParams.x = (int) (xInScreen - xInView);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }

    //endregion
}
