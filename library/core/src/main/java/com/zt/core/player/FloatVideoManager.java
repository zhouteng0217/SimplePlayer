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

import com.zt.core.base.BasePlayer;
import com.zt.core.base.IRenderView;
import com.zt.core.base.ITinyVideoView;
import com.zt.core.base.IVideoView;
import com.zt.core.base.RenderContainerView;
import com.zt.core.util.VideoUtils;

import java.lang.ref.WeakReference;

/**
 * Created by zhouteng on 2019-9-10
 * <p>
 * 悬浮视频管理
 */
public class FloatVideoManager implements ITinyVideoView.TinyVideoViewListenr {

    public static final int REQUEST_DRAWOVERLAYS_CODE = 10000;

    private WeakReference<ITinyVideoView> tinyVideoViewWeakReference;
    private WeakReference<IVideoView> videoViewWeakReference;

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

    public void startFloatVideo(ITinyVideoView tinyVideoView, IVideoView videoView) {
        tinyVideoViewWeakReference = new WeakReference<>(tinyVideoView);
        videoViewWeakReference = new WeakReference<>(videoView);

        videoLayoutParams = tinyVideoView.getVideoLayoutParams();
        createFloatVideo();
    }

    private void createFloatVideo() {
        ITinyVideoView tinyVideoView = tinyVideoViewWeakReference.get();
        if (tinyVideoView == null) {
            return;
        }
        tinyVideoView.setTinyVideoViewListener(this);

        Context context = tinyVideoView.getPlayView().getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                VideoUtils.getActivity(context).startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
                return;
            }
        }

        ViewParent viewParent = tinyVideoView.getPlayView().getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(tinyVideoView.getPlayView());
        }

        IVideoView videoView = videoViewWeakReference.get();
        if (videoView != null && videoView.getRenderContainerView() != null) {
            ViewParent renderParent = videoView.getRenderContainerView().getParent();
            ((ViewGroup) renderParent).removeView(videoView.getRenderContainerView());
            tinyVideoView.addRenderContainer(videoView.getRenderContainerView());
        }

        windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

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

        windowManager.addView(tinyVideoView.getPlayView(), wmParams);
        windowManager.updateViewLayout(tinyVideoView.getPlayView(), wmParams);
    }

    @Override
    public void closeVideoView() {
        ITinyVideoView videoView = tinyVideoViewWeakReference.get();
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
        yInScreen = event.getRawY() - VideoUtils.getStatusBarHeight(tinyVideoViewWeakReference.get().getPlayView().getContext());

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
        windowManager.updateViewLayout(tinyVideoViewWeakReference.get().getPlayView(), wmParams);
    }

    //endregion
}
