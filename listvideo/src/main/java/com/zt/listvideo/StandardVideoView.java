package com.zt.listvideo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 标准的视频播放控件
 */

public class StandardVideoView extends BaseVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private boolean isShowMobileDataDialog = false;

    private FrameLayout surfaceContainer;
    private ImageView thumbView;
    private ViewGroup bottomLayout;
    private ViewGroup topLayout;
    private TextView currentTimeText;
    private TextView totalTimeText;
    protected SeekBar seekBar;
    private ImageView fullScreen;
    protected ImageView back;
    protected TextView title;
    private ProgressBar loadingProgressBar;
    private ImageView start;
    private ViewGroup failedLayout;
    private ViewGroup replayLayout;

    private Timer updateProgressTimer;
    private ProgressTimerTask mProgressTimerTask;

    private Timer controlViewTimer;
    private ControlViewTimerTask controlViewTimerTask;

    private boolean isFullScreen = false;

    private int mSystemUiVisibility;

    private ViewParent viewParent;

    protected int originWidth;
    protected int originHeight;

    private OnFullScreenChangeListener onFullScreenChangeListener;

    public StandardVideoView(@NonNull Context context) {
        this(context, null);
    }

    public StandardVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        surfaceContainer = findViewById(R.id.surface_container);
        surfaceContainer.setOnClickListener(this);

        thumbView = findViewById(R.id.thumb);
        bottomLayout = findViewById(R.id.bottom_layout);
        topLayout = findViewById(R.id.top_layout);
        currentTimeText = findViewById(R.id.current);
        totalTimeText = findViewById(R.id.total);

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        fullScreen = findViewById(R.id.fullscreen);
        fullScreen.setOnClickListener(this);

        back = findViewById(R.id.back);
        back.setOnClickListener(this);

        title = findViewById(R.id.title);
        loadingProgressBar = findViewById(R.id.loading);

        failedLayout = findViewById(R.id.failed_layout);
        replayLayout = findViewById(R.id.reply_layout);
        replayLayout.setOnClickListener(this);

        start = findViewById(R.id.start);
        start.setOnClickListener(this);
    }

    @Override
    protected ViewGroup getSurfaceContainer() {
        return surfaceContainer;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.list_video_layout;
    }

    @Override
    public void onStateChange(int state) {
        Log.d("zhouteng", "state=" + state);
        switch (state) {
            case BasePlayer.STATE_IDLE:
                changeUIWithIdle();
                break;
            case BasePlayer.STATE_PREPARING:
                changeUIWithPreparing();
                break;
            case BasePlayer.STATE_PREPARED:
                changeUIWithPrepared();
                break;
            case BasePlayer.STATE_PLAYING:
                changeUIWithPlaying();
                break;
            case BasePlayer.STATE_PAUSED:
                changeUIWithPause();
                break;
            case BasePlayer.STATE_COMPLETED:
                changeUIWithComplete();
                break;
            case BasePlayer.STATE_ERROR:
                changeUIWithError();
                break;
            case BasePlayer.STATE_BUFFERING_START:
                changeUiWithBufferingStart();
                break;
            case BasePlayer.STATE_BUFFERING_END:
                changeUiWithBufferingEnd();
                break;
        }
    }

    protected void changeUiWithBufferingStart() {
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    protected void changeUiWithBufferingEnd() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    protected void changeUIWithPlaying() {
        updatePlayIcon(BasePlayer.STATE_PLAYING);
        startProgressTimer();
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    protected void changeUIWithPause() {
        updatePlayIcon(BasePlayer.STATE_PAUSED);
        cancelProgressTimer();
        cancelControlViewTimer();
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    protected void changeUIWithError() {
        updatePlayIcon(BasePlayer.STATE_ERROR);
        cancelProgressTimer();
        setViewsVisible(View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
    }

    protected void changeUIWithComplete() {
        updatePlayIcon(BasePlayer.STATE_COMPLETED);
        cancelProgressTimer();
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
        seekBar.setProgress(100);
        currentTimeText.setText(totalTimeText.getText());
    }


    private void updatePlayIcon(int state) {
        if (state == BasePlayer.STATE_PLAYING) {
            setPlayingIcon();
        } else if (state == BasePlayer.STATE_ERROR) {
            setPausedIcon();
        } else {
            setPausedIcon();
        }
    }

    private void setPlayingIcon() {
        start.setImageResource(R.drawable.ic_pause);
    }

    private void setPausedIcon() {
        start.setImageResource(R.drawable.ic_play);
    }

    protected void resetProgressAndTime() {
        currentTimeText.setText(VideoUtils.stringForTime(0));
        totalTimeText.setText(VideoUtils.stringForTime(0));
    }

    protected void changeUIWithIdle() {
        cancelProgressTimer();
        updatePlayIcon(BasePlayer.STATE_IDLE);
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
    }

    protected void changeUIWithPreparing() {
        resetProgressAndTime();
        setViewsVisible(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    protected void changeUIWithPrepared() {

        int visible = View.VISIBLE;

        if (player.getDuration() <= 0) {
            //表示直播类的视频，没有进度条
            visible = View.INVISIBLE;
        }

        startControlViewTimer();

        currentTimeText.setVisibility(visible);
        totalTimeText.setVisibility(visible);
        seekBar.setVisibility(visible);

        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    private void setViewsVisible(int topLayoutVisi, int bottomLayoutVisi, int failedLayoutVisi, int loadingVisi, int thumbVisi, int replayLayoutVisi) {
        topLayout.setVisibility(topLayoutVisi);
        bottomLayout.setVisibility(bottomLayoutVisi);
        failedLayout.setVisibility(failedLayoutVisi);
        loadingProgressBar.setVisibility(loadingVisi);
        thumbView.setVisibility(thumbVisi);
        replayLayout.setVisibility(replayLayoutVisi);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            start();
        } else if (v.getId() == R.id.reply_layout) {
            replay();
        } else if (v.getId() == R.id.surface_container) {
            surfaceContainerClick();
        } else if (v.getId() == R.id.fullscreen) {
            handleFullScreen();
        } else if (v.getId() == R.id.back) {
            handleBack();
        }
    }

    private void handleBack() {
        if (isFullScreen) {
            exitFullscreen();
        } else {
            player.destroy();
            VideoUtils.getActivity(getContext()).finish();
        }
    }

    private void handleFullScreen() {
        if (isFullScreen) {
            exitFullscreen();
        } else {
            startFullScreen();
        }
    }

    public boolean onBackKeyPressed() {
        if (isFullScreen) {
            exitFullscreen();
            return true;
        }
        return false;
    }

    private void startFullScreen() {
        isFullScreen = true;

        Activity activity = VideoUtils.getActivity(getContext());

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        VideoUtils.setFullScreenFlag(activity);
        VideoUtils.hideSupportActionBar(activity, true, true);
        VideoUtils.hideNavKey(activity);

        changeToFullScreen();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(true);
        }
    }

    protected void changeToFullScreen() {

        originWidth = getWidth();
        originHeight = getHeight();

        viewParent = getParent();

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    private ViewGroup getRootViewGroup() {
        Activity activity = (Activity) getContext();
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    private void removePlayerFromParent() {
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    private void exitFullscreen() {
        isFullScreen = false;

        Activity activity = VideoUtils.getActivity(getContext());

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        VideoUtils.setFullScreenFlag(activity);
        VideoUtils.showSupportActionBar(activity, true, false);   //根据需要是否显示actionbar和状态栏

        activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);

        changeToNormalScreen();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(false);
        }
    }

    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) this.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this);
        }
    }

    protected void surfaceContainerClick() {
        if (player == null || !player.isInPlaybackState()) {
            return;
        }
        toggleControlView();
        startControlViewTimer();
    }

    private void toggleControlView() {
        bottomLayout.setVisibility(bottomLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        topLayout.setVisibility(bottomLayout.getVisibility());
    }

    public void start() {
        if (!player.getUrl().startsWith("file") && !VideoUtils.isWifiConnected(getContext()) && !isShowMobileDataDialog) {
            showMobileDataDialog();
            return;
        }
        startVideo();
    }

    public void showMobileDataDialog() {
        if (isShowMobileDataDialog) {
            return;
        }
        isShowMobileDataDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(getResources().getString(R.string.mobile_data_tips));
        builder.setPositiveButton(getResources().getString(R.string.contine_playing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startVideo();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.stop_play), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    protected boolean isInPlaybackState() {
        return player != null && player.isInPlaybackState();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int time = progress * player.getDuration() / 100;
            seekBar.setProgress(progress);
            player.seekTo(time);
            if (player.getBufferedPercentage() < progress) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            } else {
                loadingProgressBar.setVisibility(View.GONE);
            }
        } else {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        startProgressTimer();
    }

    public void setOnFullScreenChangeListener(OnFullScreenChangeListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    //top,bottom这些控制按钮，隐藏，消失任务
    private class ControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    if (player.isInPlaybackState()) {
                        topLayout.setVisibility(View.GONE);
                        bottomLayout.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void startControlViewTimer() {
        cancelControlViewTimer();
        controlViewTimer = new Timer();
        controlViewTimerTask = new ControlViewTimerTask();
        controlViewTimer.schedule(controlViewTimerTask, 2500);
    }

    private void cancelControlViewTimer() {
        if (controlViewTimerTask != null) {
            controlViewTimerTask.cancel();
        }
        if (controlViewTimerTask != null) {
            controlViewTimerTask.cancel();
        }
    }

    //进度条更新任务类
    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    if (isInPlaybackState()) {
                        setProgressAndText();
                    }
                }
            });
        }
    }

    protected void startProgressTimer() {
        if (player.getDuration() <= 0) {
            return;
        }
        cancelProgressTimer();
        updateProgressTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProgressTimer.schedule(mProgressTimerTask, 0, 300);
    }

    protected void cancelProgressTimer() {
        if (updateProgressTimer != null) {
            updateProgressTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    protected void setProgressAndText() {
        int position = player.getCurrentPosition();
        int duration = player.getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        if (progress != 0) {
            seekBar.setProgress(progress);
        }
        currentTimeText.setText(VideoUtils.stringForTime(position));
        totalTimeText.setText(VideoUtils.stringForTime(duration));
    }

    protected void setTitle(String titleText) {
        title.setText(titleText);
    }

    public void release() {
        if (player != null) {
            player.release();
        }
        seekBar.setProgress(0);
    }

    public void resetSurface() {
        if (player != null) {
            player.resetSurface();
        }
    }

    private void replay() {
        release();
        resetSurface();
        start();
    }

    public void destroy() {
        if (player != null) {
            player.destroy();
        }
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }
}
