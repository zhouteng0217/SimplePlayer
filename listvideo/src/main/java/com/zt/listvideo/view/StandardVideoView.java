package com.zt.listvideo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zt.listvideo.listener.OnFullScreenChangeListener;
import com.zt.listvideo.R;
import com.zt.listvideo.util.VideoUtils;
import com.zt.listvideo.base.BasePlayer;
import com.zt.listvideo.base.BaseVideoView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 标准的视频播放控件
 */

public class StandardVideoView extends BaseVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

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

    private ViewGroup lockStatusLayout;
    private ImageView lockStatus;

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

    protected boolean isLiveVideo = false; // 表示是直播类的视频，没有播放进度

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
        surfaceContainer.setOnTouchListener(this);

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

        lockStatus = findViewById(R.id.lock_status);
        lockStatusLayout = findViewById(R.id.lock_status_layout);
        lockStatusLayout.setOnClickListener(this);
    }

    @Override
    protected ViewGroup getSurfaceContainer() {
        return surfaceContainer;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.standard_video_layout;
    }

    //region 根据状态更新UI

    @Override
    public void onStateChange(int state) {
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
            case BasePlayer.STATE_SEEK_START:
                changeUiWithBufferingStart();
                break;
            case BasePlayer.STATE_BUFFERING_END:
            case BasePlayer.STATE_SEEK_END:
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

            isLiveVideo = true;
        }

        startControlViewTimer();

        currentTimeText.setVisibility(visible);
        totalTimeText.setVisibility(visible);
        seekBar.setVisibility(visible);

        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
    }

    private void setViewsVisible(int topLayoutVisi, int bottomLayoutVisi, int failedLayoutVisi, int loadingVisi, int thumbVisi, int replayLayoutVisi) {

        setTopVisi(topLayoutVisi);
        setBottomVisi(bottomLayoutVisi);

        failedLayout.setVisibility(failedLayoutVisi);
        loadingProgressBar.setVisibility(loadingVisi);
        thumbView.setVisibility(thumbVisi);
        replayLayout.setVisibility(replayLayoutVisi);
    }
    //endregion

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
        } else if (v.getId() == R.id.lock_status_layout) {
            toggleVideoLockStatus();
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

    public void setTitle(String titleText) {
        title.setText(titleText);
    }


    //region 全屏处理

    private void startFullScreen() {
        isFullScreen = true;

        resetLockStatus();

        Activity activity = VideoUtils.getActivity(getContext());

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        VideoUtils.hideSupportActionBar(activity, true);
        VideoUtils.addFullScreenFlag(activity);
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

        resetLockStatus();

        Activity activity = VideoUtils.getActivity(getContext());

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        VideoUtils.showSupportActionBar(activity, true);   //根据需要是否显示actionbar和状态栏
        VideoUtils.clearFullScreenFlag(activity);

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

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setOnFullScreenChangeListener(OnFullScreenChangeListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    //endregion

    //region 点击屏幕，显示隐藏控制栏
    protected void surfaceContainerClick() {
        if (player == null || !player.isInPlaybackState()) {
            return;
        }
        toggleControlView();
        startControlViewTimer();
    }

    private void toggleControlView() {
        setVideoLockLayoutVisi(lockStatusLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        if (isLocked) {
            return;
        }
        int visi = bottomLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        setTopBottomVisi(visi);
    }

    private void setTopBottomVisi(int visi) {
        setBottomVisi(visi);
        setTopVisi(visi);
    }

    protected void setBottomVisi(int visi) {
        bottomLayout.setVisibility(isLocked ? View.GONE : visi);
    }

    protected void setTopVisi(int visi) {
        topLayout.setVisibility(isLocked ? View.GONE : visi);
    }

    //endregion

    //region top,bottom控制栏隐藏任务
    private class ControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    if (player.isInPlaybackState()) {
                        setTopBottomVisi(View.GONE);
                        setVideoLockLayoutVisi(View.INVISIBLE);
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
    //endregion

    //region 进度条更新任务
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
        if (isLiveVideo) {
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
        if (progress != 0 && !touchScreen) {
            seekBar.setProgress(progress);
        }
        currentTimeText.setText(VideoUtils.stringForTime(position));
        totalTimeText.setText(VideoUtils.stringForTime(duration));
    }
    //endregion

    //region 播放控制

    protected boolean isInPlaybackState() {
        return player != null && player.isInPlaybackState();
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

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }
    //endregion

    //region 锁定屏幕

    protected boolean isLocked = false; //是否处于锁定屏幕状态

    //默认全屏支持锁定屏幕
    protected boolean isSupportLock() {
        return isFullScreen;
    }

    private void toggleVideoLockStatus() {
        isLocked = !isLocked;
        lockStatus.setImageResource(isLocked ? R.drawable.ic_locked : R.drawable.ic_unlocked);
        setTopBottomVisi(isLocked ? View.GONE : View.VISIBLE);
    }

    private void setVideoLockLayoutVisi(int visi) {
        if (isSupportLock()) {
            lockStatusLayout.setVisibility(visi);
        } else {
            lockStatusLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void resetLockStatus() {
        isLocked = false;
        setVideoLockLayoutVisi(bottomLayout.getVisibility());
    }

    //endregion

    //region 音量，亮度，进度调整

    protected boolean isSupportVolume = true;
    protected boolean isSupportBrightness = true;
    protected boolean isSupportSeek = true;

    protected VolumeDialog volumeDialog;
    protected BrightnessDialog brightnessDialog;
    protected SeekDialog seekDialog;

    protected boolean touchScreen;

    private float downX;
    private float downY;

    private int downVolume;  //触摸屏幕时的当前音量
    private float downBrightness;  //触摸屏幕时的当前亮度

    private int downVideoPosition; //触摸屏幕时的当前播放进度
    private int newVideoPosition; //手势操作拖动后的新的进度

    private boolean isChangedProgress;  //是否手势操作拖动了进度条

    private boolean isSeekGesture = false; //是否触发了进度条拖拽的手势
    private boolean isVolumeGesture = false; //是否触发了音量调整的手势
    private boolean isBrightnessGesture = false; //是否触发了亮度调整的手势

    private static final int MINI_GESTURE_DISTANCE = 60; // 手势的最小触发范围;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (player == null || !player.isInPlaybackState()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchScreen = true;
                downX = event.getX();
                downY = event.getY();
                downVolume = player.getStreamVolume();
                downBrightness = VideoUtils.getScreenBrightness(getContext());
                downVideoPosition = player.getCurrentPosition();
                isChangedProgress = false;

                isSeekGesture = false;
                isVolumeGesture = false;
                isBrightnessGesture = false;

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                touchMove(dx, dy, event.getX());
                break;
            case MotionEvent.ACTION_UP:
                touchScreen = false;
                hideVolumeDialog();
                hideBrightnewssDialog();
                hideSeekDialog();
                seekToNewVideoPosition();
                break;
        }
        return false;
    }

    //音量，亮度，播放进度等手势判断
    private void touchMove(float dx, float dy, float x) {

        if (isLocked) {
            return;
        }

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        int distance = getWidth();

        if (!isSeekGesture && !isVolumeGesture && !isBrightnessGesture) {

            //触发了播放进度拖拽手势
            if (absDx > absDy && absDx >= MINI_GESTURE_DISTANCE) {
                isSeekGesture = true;
            }
            //触发了亮度调节手势
            else if (absDy > absDx && x <= distance / 2 && absDy >= MINI_GESTURE_DISTANCE) {
                isBrightnessGesture = true;
            }
            //触发了音量调节手势
            else if (absDy > absDx && x > distance / 2 && absDy >= MINI_GESTURE_DISTANCE) {
                isVolumeGesture = true;
            }
        }

        if (isSeekGesture && !isLiveVideo && isSupportSeek) {
            changeProgress(dx);
            return;
        }

        if (isBrightnessGesture && isSupportBrightness) {
            changeBrightness(dy);
            return;
        }

        if (isVolumeGesture && isSupportVolume) {
            changeVolume(dy);
        }
    }

    //region 播放进度手势处理
    private void seekToNewVideoPosition() {
        if (isChangedProgress) {
            player.seekTo(newVideoPosition);
            isChangedProgress = false;

            startProgressTimer();
        }
    }

    private void changeProgress(float dx) {
        cancelProgressTimer();

        int distance = getWidth();
        int videoDuration = player.getDuration();
        newVideoPosition = downVideoPosition + (int) (dx / distance * videoDuration);
        if (newVideoPosition >= videoDuration) {
            newVideoPosition = videoDuration;
        }
        String progressText = VideoUtils.stringForTime(newVideoPosition) + "/" + VideoUtils.stringForTime(videoDuration);
        int progress = (int) ((float) newVideoPosition / videoDuration * 100);
        showSeekDialog(progressText, progress);
        isChangedProgress = true;
    }

    private void showSeekDialog(String progressText, int seekBarProgress) {
        if (seekDialog == null) {
            seekDialog = new SeekDialog(getContext(), R.style.volume_brightness_theme);
        }
        seekDialog.showSeekDialog(progressText, this);
        setProgressTextWithTouch(seekBarProgress);
    }

    private void hideSeekDialog() {
        if (seekDialog != null) {
            seekDialog.dismiss();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        cancelControlViewTimer();
        downVideoPosition = player.getCurrentPosition();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int videoDuration = player.getDuration();
            newVideoPosition = progress * videoDuration / 100;
            String progressText = VideoUtils.stringForTime(newVideoPosition) + "/" + VideoUtils.stringForTime(videoDuration);
            showSeekDialog(progressText, progress);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekTo(newVideoPosition);
        hideSeekDialog();
        startProgressTimer();
        startControlViewTimer();
    }

    private void setProgressTextWithTouch(int progress) {
        currentTimeText.setText(VideoUtils.stringForTime(newVideoPosition));
        seekBar.setProgress(progress);
    }

    //endregion

    //region 亮度手势操作处理
    private void changeBrightness(float dy) {
        //屏幕亮度区间0.0 ~ 1.0
        int distance = getHeight();

        float newBrightness = downBrightness - dy / distance;
        if (newBrightness < 0.0f) {
            newBrightness = 0.0f;
        }
        if (newBrightness > 1.0f) {
            newBrightness = 1.0f;
        }
        VideoUtils.setScreenBrightness(getContext(), newBrightness);
        showBrightnewssDialog((int) (newBrightness * 100));
    }

    private void showBrightnewssDialog(int volumeProgress) {
        if (brightnessDialog == null) {
            brightnessDialog = new BrightnessDialog(getContext(), R.style.volume_brightness_theme);
        }
        brightnessDialog.showBrightnewssDialog(volumeProgress, this);
    }

    private void hideBrightnewssDialog() {
        if (brightnessDialog != null) {
            brightnessDialog.dismiss();
        }
    }
    //endregion

    //region 音量手势操作处理
    private void changeVolume(float dy) {

        int maxVolume = player.getStreamMaxVolume();

        int distance = getHeight();

        float newVolume = downVolume - dy / distance * maxVolume;

        if (newVolume < 0) {
            newVolume = 0;
        }
        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        }

        player.setStreamVolume((int) newVolume);

        showVolumeDialog((int) (newVolume / maxVolume * 100));
    }

    private void showVolumeDialog(int volumeProgress) {
        if (volumeDialog == null) {
            volumeDialog = new VolumeDialog(getContext(), R.style.volume_brightness_theme);
        }
        volumeDialog.showVolumeDialog(volumeProgress, this);
    }

    private void hideVolumeDialog() {
        if (volumeDialog != null) {
            volumeDialog.dismiss();
        }
    }
    //endregion

    //endregion

}
