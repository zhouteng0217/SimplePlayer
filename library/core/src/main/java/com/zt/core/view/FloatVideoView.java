package com.zt.core.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.zt.core.R;
import com.zt.core.base.BasePlayer;
import com.zt.core.base.BaseVideoView;
import com.zt.core.base.IFloatView;
import com.zt.core.util.VideoUtils;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhouteng on 2019-09-13
 * <p>
 * 小窗口视频UI界面
 */
public class FloatVideoView extends BaseVideoView implements IFloatView, View.OnClickListener, View.OnTouchListener {

    private IFloatView.LayoutParams videoLayoutParams;
    private FloatViewListener listener;
    private ImageView playBtn;
    private View replayLayout;
    private View topLayout;

    protected Timer controlViewTimer;
    protected ControlViewTimerTask controlViewTimerTask;

    public FloatVideoView(@NonNull Context context) {
        this(context, null);
    }

    public FloatVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        videoLayoutParams = new IFloatView.LayoutParams();

        //默认位置和大小
        videoLayoutParams.x = 0;
        videoLayoutParams.y = 0;
        videoLayoutParams.width = VideoUtils.dp2px(context, 200);
        videoLayoutParams.height = VideoUtils.dp2px(context, 112);

        View closeView = findViewById(R.id.close);
        closeView.setOnClickListener(this);

        View fullscreenView = findViewById(R.id.fullscreen);
        fullscreenView.setOnClickListener(this);

        surfaceContainer.setOnClickListener(this);
        surfaceContainer.setOnTouchListener(this);

        topLayout = findViewById(R.id.top_layout);

        playBtn = findViewById(R.id.start);
        playBtn.setOnClickListener(this);

        replayLayout = findViewById(R.id.reply_layout);
        replayLayout.setOnClickListener(this);
    }

    //禁止重力感应旋转
    @Override
    public boolean supportSensorRotate() {
        return false;
    }

    @Override
    protected int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.float_video_layout;
    }

    @Override
    public boolean onBackKeyPressed() {
        return false;
    }

    @Override
    public void setFloatVideoLayoutParams(IFloatView.LayoutParams layoutParams) {
        if (layoutParams != null) {
            videoLayoutParams = layoutParams;
        }
    }

    @Override
    public IFloatView.LayoutParams getFloatVideoLayoutParams() {
        return videoLayoutParams;
    }

    @Override
    public void setFloatVideoViewListener(FloatViewListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (listener != null) {
            return listener.onTouch(event);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.close) {
            if (listener != null) {
                listener.closeVideoView();
            }
        } else if (viewId == R.id.fullscreen) {
            if (listener != null) {
                listener.backToNormalView();
            }
        } else if (viewId == R.id.start) {
            start();
        } else if (viewId == R.id.reply_layout) {
            replay();
        } else if (viewId == getSurfaceContainerId()) {
            if (!isInPlaybackState()) {
                return;
            }
            toggleControlView();
            startControlViewTimer();
        }
    }

    protected void toggleControlView() {
        int visi = topLayout.getVisibility() == VISIBLE ? GONE : VISIBLE;
        topLayout.setVisibility(visi);
        playBtn.setVisibility(visi);
    }

    protected void startControlViewTimer() {
        cancelControlViewTimer();
        controlViewTimer = new Timer();
        controlViewTimerTask = new ControlViewTimerTask(this);
        controlViewTimer.schedule(controlViewTimerTask, 2500);
    }

    protected void cancelControlViewTimer() {
        if (controlViewTimer != null) {
            controlViewTimer.cancel();
        }
        if (controlViewTimerTask != null) {
            controlViewTimerTask.cancel();
        }
    }

    protected static class ControlViewTimerTask extends TimerTask {

        private WeakReference<FloatVideoView> weakReference;

        private ControlViewTimerTask(FloatVideoView videoView) {
            weakReference = new WeakReference<>(videoView);
        }

        @Override
        public void run() {
            FloatVideoView videoView = weakReference.get();
            if (videoView != null) {
                videoView.post(new ControlViewRunnable(videoView));
            }
        }
    }

    private static class ControlViewRunnable implements Runnable {

        private final WeakReference<FloatVideoView> weakReference;

        private ControlViewRunnable(FloatVideoView videoView) {
            weakReference = new WeakReference<>(videoView);
        }

        @Override
        public void run() {
            FloatVideoView videoView = weakReference.get();
            if (videoView != null) {
                videoView.hideControlView();
            }
        }
    }

    private void hideControlView() {
        topLayout.setVisibility(View.GONE);
        playBtn.setVisibility(View.GONE);
    }

    @Override
    public void onStateChange(int state) {
        super.onStateChange(state);
        if (state == BasePlayer.STATE_COMPLETED) {
            replayLayout.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.GONE);
            topLayout.setVisibility(View.GONE);
        } else {
            replayLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setPlayingIcon() {
        playBtn.setImageResource(R.drawable.ic_pause);
    }

    @Override
    protected void setPausedIcon() {
        playBtn.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void destroy() {
        super.destroy();
        destroyPlayerController();
    }

    @Override
    public void destroyPlayerController() {
        cancelControlViewTimer();
    }
}
