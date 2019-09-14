package com.zt.core.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.zt.core.base.IRenderView;
import com.zt.core.base.ITinyVideoView;
import com.zt.core.util.VideoUtils;

import java.lang.ref.WeakReference;

/**
 * Created by zhouteng on 2019-9-10
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager implements ITinyVideoView.TinyVideoViewListenr {

    public static final int REQUEST_DRAWOVERLAYS_CODE = 10000;

    private WeakReference<ITinyVideoView> videoViewWeakReference;
    private WeakReference<IRenderView> renderViewWeakReference;

    private WindowManager windowManager;

    private ITinyVideoView.VideoLayoutParams videoLayoutParams;

    private static FloatVideoManager instance;
    private WindowManager.LayoutParams wmParams;

    public static FloatVideoManager getInstance() {
        if (instance == null) {
            instance = new FloatVideoManager();
        }
        return instance;
    }

    public void startFloatVideo(ITinyVideoView videoView, IRenderView renderView) {
        videoViewWeakReference = new WeakReference<>(videoView);
        renderViewWeakReference = new WeakReference<>(renderView);

        videoLayoutParams = videoView.getVideoLayoutParams();
        createFloatVideo();
    }

    private void createFloatVideo() {
        ITinyVideoView videoView = videoViewWeakReference.get();
        if (videoView == null) {
            return;
        }
        videoView.setTinyVideoViewListener(this);

        Context context = videoView.getPlayView().getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                VideoUtils.getActivity(context).startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
                return;
            }
        }

        ViewParent viewParent = videoView.getPlayView().getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(videoView.getPlayView());
        }

        IRenderView renderView = renderViewWeakReference.get();
        if (renderView != null && renderView.getRenderView() != null) {

        }

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        wmParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT | Gravity.START;
        wmParams.width = videoLayoutParams.width;
        wmParams.height = videoLayoutParams.height;
        wmParams.x = videoLayoutParams.x;
        wmParams.y = videoLayoutParams.y;

        windowManager.addView(videoView.getPlayView(), wmParams);
        windowManager.updateViewLayout(videoView.getPlayView(), wmParams);
    }

    @Override
    public void closeVideoView() {
        ITinyVideoView videoView = videoViewWeakReference.get();
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
        yInScreen = event.getRawY() - VideoUtils.getStatusBarHeight(videoViewWeakReference.get().getPlayView().getContext());

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
        windowManager.updateViewLayout(videoViewWeakReference.get().getPlayView(), wmParams);
    }

    //endregion
}
