package com.zt.core.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import java.lang.ref.WeakReference;

/**
 * 辅助实现重力感应下视频的旋转操作
 */
public class OrientationHelper {

    private IVideoView videoView;
    private OrientationEventListener orientationEventListener;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private int lastScreenType = -1;

    public OrientationHelper(IVideoView videoView) {
        this.videoView = videoView;
        init();
    }

    private void init() {
        Context context = videoView.getPlayView().getContext();
        orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int rotation) {

                final boolean autoRotateOn = (Settings.System.getInt(videoView.getPlayView().getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);

                //设置了跟随系统，系统方向锁定的话，不自动旋转方向
                if (!autoRotateOn && videoView.rotateWithSystem()) {
                    return;
                }

                if (rotation > 45 && rotation < 135) {
                    screenType = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (rotation > 135 && rotation < 225) {
                    screenType = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                } else if (rotation > 225 && rotation < 315) {
                    screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else if ((rotation > 315 && rotation < 360) || (rotation > 0 && rotation < 45)) {
                    screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }

                if (lastScreenType == screenType) {
                    return;
                }

                lastScreenType = screenType;
                videoView.getPlayView().post(new OrientationRunnable(videoView, screenType));
            }
        };
    }

    public void start() {
        if (videoView.supportSensorRotate()) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

    private static class OrientationRunnable implements Runnable {

        private WeakReference<IVideoView> weakReference;
        private int screenType;

        private OrientationRunnable(IVideoView videoView, int screenType) {
            weakReference = new WeakReference<>(videoView);
            this.screenType = screenType;
        }

        @Override
        public void run() {
            IVideoView videoView = weakReference.get();
            if (videoView == null) {
                return;
            }
            if (isFullScreenType() && !videoView.isFullScreen()) {
                videoView.startFullscreenWithOrientation(screenType);
            } else if (!isFullScreenType() && videoView.isFullScreen()) {
                videoView.exitFullscreenWithOrientation(screenType);
            }
        }

        private boolean isFullScreenType() {
            return screenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || screenType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
    }

    public void setOrientationEnable(boolean enable) {
        if (enable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }
}
