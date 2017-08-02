package com.zt.listvideoplayer.listvideoplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zt.listvideoplayer.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhouteng on 2017/4/7.
 */

public abstract class ListVideoPlayer extends FrameLayout implements View.OnClickListener
        , SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    public static final String TAG = "ListVideoPlayer";

    public static final int CURRENT_STATE_NORMAL = 0;
    public static final int CURRENT_STATE_PREPARING = 1;
    public static final int CURRENT_STATE_PLAYING = 2;
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    public static final int CURRENT_STATE_PAUSE = 5;
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    public static final int CURRENT_STATE_ERROR = 7;

    protected boolean isWifiTipDialogShowed = false;

    protected Timer updateProgressTimer;
    protected ProgressTimerTask mProgressTimerTask;

    protected boolean mTouchingProgressBar;

    public static boolean SAVE_PROGRESS = true;

    private static long CLICK_QUIT_FULLSCREEN_TIME = 0;
    private static final int FULL_SCREEN_NORMAL_DELAY = 300;

    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static int BACKUP_PLAYING_BUFFERING_STATE = -1;

    protected ImageView startButton;
    private ImageView fullscreenButton;
    private SeekBar progressBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    protected ViewGroup bottomContainer;
    private ViewGroup textureViewContainer;
    protected ViewGroup topContainer;
    private TextView titleTextView;
    private ImageView backButton;
    protected ImageView thumbImageView;


    private Handler mHandler;
    protected String url = "";

    protected int currentState = -1;

    private int seekToInAdvance = 0;

    protected boolean isLive = false;

    protected boolean isPrepared = false;

    public ListVideoPlayer(Context context) {
        super(context);
        init(context);
    }

    public ListVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = (ImageView) findViewById(R.id.start);
        fullscreenButton = (ImageView) findViewById(R.id.fullscreen);
        progressBar = (SeekBar) findViewById(R.id.progress);
        currentTimeTextView = (TextView) findViewById(R.id.current);
        totalTimeTextView = (TextView) findViewById(R.id.total);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        textureViewContainer = (ViewGroup) findViewById(R.id.surface_container);
        topContainer = (ViewGroup) findViewById(R.id.layout_top);
        titleTextView = (TextView) findViewById(R.id.title);
        backButton = (ImageView) findViewById(R.id.back);

        backButton.setVisibility(View.GONE);

        backButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);

        mHandler = new Handler();
    }

    protected void setUp(String url, String title) {
        this.url = url;
        titleTextView.setText(title);
        onStateAction(CURRENT_STATE_NORMAL);
    }

    public abstract int getLayoutId();

    public void setBackButtonVisibility(int visibility) {
        backButton.setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentState == CURRENT_STATE_NORMAL || currentState == CURRENT_STATE_ERROR) {
                if (!url.startsWith("file") && !ListVideoUtils.isWifiConnected(getContext()) && !isWifiTipDialogShowed) {
                    showWifiDialog();
                    return;
                }
                prepareMediaPlayer();
            } else if (currentState == CURRENT_STATE_PLAYING) {
                MediaManager.instance().mediaPlayer.pause();
                onStateAction(CURRENT_STATE_PAUSE);
            } else if (currentState == CURRENT_STATE_PAUSE) {
                MediaManager.instance().mediaPlayer.start();
                onStateAction(CURRENT_STATE_PLAYING);
            }
            if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                prepareMediaPlayer();
            }
        } else if (v.getId() == R.id.fullscreen) {
            VideoPlayerManager.getInstance().handleFullScreen(getContext());
        } else if (v.getId() == R.id.back) {
            VideoPlayerManager.getInstance().handleFullScreen(getContext());
        } else if (v.getId() == R.id.surface_container && currentState == CURRENT_STATE_ERROR) {
            prepareMediaPlayer();
        }
    }

    public void onPause() {
        if (currentState == CURRENT_STATE_PLAYING) {
            MediaManager.instance().mediaPlayer.pause();
            onStateAction(CURRENT_STATE_PAUSE);
        }
    }

    protected void prepareMediaPlayer() {
        VideoPlayerManager.getInstance().completeAll();
        initTextureView();
        addTextureView();
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        ListVideoUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MediaManager.CURRENT_PLAYING_URL = url;
        onStateAction(CURRENT_STATE_PREPARING);
        VideoPlayerManager.getInstance().setCurrentVideoPlayer(this);
    }

    public int widthRatio = 16;
    public int heightRatio = 9;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onStateAction(int state) {
        currentState = state;
        switch (state) {
            case CURRENT_STATE_PREPARING:
                resetProgressAndTime();
                break;
            case CURRENT_STATE_PLAYING:
            case CURRENT_STATE_PAUSE:
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                startProgressTimer();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                cancelProgressTimer();
                progressBar.setProgress(100);
                currentTimeTextView.setText(totalTimeTextView.getText());
                break;
            case CURRENT_STATE_ERROR:
                cancelProgressTimer();
                break;
        }
    }

    private void startProgressTimer() {
        if (isLive) {
            return;
        }
        cancelProgressTimer();
        updateProgressTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProgressTimer.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (updateProgressTimer != null) {
            updateProgressTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void onPrepared() {

        Log.i(TAG, "onPrepared " + " [" + this.hashCode() + "] ");

        isPrepared = true;

        if (currentState != CURRENT_STATE_PREPARING) return;
        if (seekToInAdvance != 0) {
            MediaManager.instance().mediaPlayer.seekTo(seekToInAdvance);
            seekToInAdvance = 0;
        } else {
            int position = ListVideoUtils.getSavedProgress(getContext(), url);
            if (position != 0) {
                MediaManager.instance().mediaPlayer.seekTo(position);
            }
        }
        if (getDuration() <= 0) {
            isLive = true;
        } else {
            isLive = false;
        }
        setTimeProgressVisi();
        startProgressTimer();
        onStateAction(CURRENT_STATE_PLAYING);
    }

    public void clearFullscreenLayout() {

    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE || currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setProgressAndText();
                    }
                });
            }
        }
    }

    protected void setProgressAndText() {
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        if (!mTouchingProgressBar) {
            if (progress != 0) progressBar.setProgress(progress);
        }
        if (position != 0) currentTimeTextView.setText(ListVideoUtils.stringForTime(position));
        totalTimeTextView.setText(ListVideoUtils.stringForTime(duration));
    }

    public int getDuration() {
        int duration = 0;
        try {
            duration = MediaManager.instance().mediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    protected int getCurrentPositionWhenPlaying() {
        int position = 0;
        if (currentState == CURRENT_STATE_PLAYING ||
                currentState == CURRENT_STATE_PAUSE ||
                currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            try {
                position = MediaManager.instance().mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    Log.d(TAG, "AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
                        if (MediaManager.instance().mediaPlayer != null &&
                                MediaManager.instance().mediaPlayer.isPlaying()) {
                            MediaManager.instance().mediaPlayer.pause();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    public static void releaseAllVideos() {
        if ((System.currentTimeMillis() - CLICK_QUIT_FULLSCREEN_TIME) > FULL_SCREEN_NORMAL_DELAY) {
            Log.d(TAG, "releaseAllVideos");
            VideoPlayerManager.getInstance().completeAll();
            MediaManager.instance().releaseMediaPlayer();
        }
    }

    private void setTimeProgressVisi() {
        currentTimeTextView.setVisibility(isLive ? View.INVISIBLE : View.VISIBLE);
        totalTimeTextView.setVisibility(isLive ? View.INVISIBLE : View.VISIBLE);
        progressBar.setVisibility(isLive ? View.INVISIBLE : View.VISIBLE);
    }

    protected void resetProgressAndTime() {
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(0);
        currentTimeTextView.setText(ListVideoUtils.stringForTime(0));
        totalTimeTextView.setText(ListVideoUtils.stringForTime(0));
    }

    public void initTextureView() {
        removeTextureView();
        MediaManager.textureView = new ResizeTextureView(getContext());
        MediaManager.textureView.setSurfaceTextureListener(MediaManager.instance());
    }

    public void removeTextureView() {
        MediaManager.savedSurfaceTexture = null;
        if (MediaManager.textureView != null && MediaManager.textureView.getParent() != null) {
            ((ViewGroup) MediaManager.textureView.getParent()).removeView(MediaManager.textureView);
        }
    }

    public void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(MediaManager.textureView, layoutParams);
    }

    public void onAutoCompletion() {
        if (!isPrepared) {
            return;
        }
        isPrepared = false;
        Runtime.getRuntime().gc();
        dismissProgressDialog();
        onStateAction(CURRENT_STATE_AUTO_COMPLETE);
        ListVideoUtils.saveProgress(getContext(), url, 0);
        VideoPlayerManager.getInstance().removePlayerFromParent();
    }

    public void onCompletion() {
        Log.i(TAG, "onCompletion " + " [" + this.hashCode() + "] ");
        //save position
        if (currentState == CURRENT_STATE_PLAYING || currentState == CURRENT_STATE_PAUSE) {
            int position = getCurrentPositionWhenPlaying();
//            int duration = getDuration();
            ListVideoUtils.saveProgress(getContext(), url, position);
        }
        onStateAction(CURRENT_STATE_NORMAL);
        // 清理缓存变量
        textureViewContainer.removeView(MediaManager.textureView);
        MediaManager.instance().currentVideoWidth = 0;
        MediaManager.instance().currentVideoHeight = 0;

        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        ListVideoUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        clearFullscreenLayout();
        ListVideoUtils.getActivity(getContext()).setRequestedOrientation(NORMAL_ORIENTATION);

        MediaManager.textureView = null;
        MediaManager.savedSurfaceTexture = null;
//        MediaManager.textureView = null;

    }

    public void showWifiDialog() {

    }

    public void dismissProgressDialog() {

    }

    public void onSeekComplete() {

    }

    public void onError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && what != -38) {
            onStateAction(CURRENT_STATE_ERROR);
            VideoPlayerManager.getInstance().release();
            Toast.makeText(getContext(), R.string.unavailable_resource, Toast.LENGTH_LONG).show();
        }
    }

    public void onInfo(int what, int extra) {
        Log.d(TAG, "onInfo what - " + what + " extra - " + extra);
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            if (currentState == CURRENT_STATE_PLAYING_BUFFERING_START) return;
            BACKUP_PLAYING_BUFFERING_STATE = currentState;
            onStateAction(CURRENT_STATE_PLAYING_BUFFERING_START);//没这个case
            Log.d(TAG, "MEDIA_INFO_BUFFERING_START");
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (BACKUP_PLAYING_BUFFERING_STATE != -1) {
                onStateAction(BACKUP_PLAYING_BUFFERING_STATE);
                BACKUP_PLAYING_BUFFERING_STATE = -1;
            }
            Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
        }
    }

    public void onVideoSizeChanged() {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        MediaManager.textureView.setVideoSize(MediaManager.instance().getVideoSize());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() != R.id.surface_container) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                startProgressTimer();
                break;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (currentState != CURRENT_STATE_PLAYING &&
                currentState != CURRENT_STATE_PAUSE) return;
        int time = seekBar.getProgress() * getDuration() / 100;
        MediaManager.instance().mediaPlayer.seekTo(time);
        Log.i(TAG, "seekTo " + time + " [" + this.hashCode() + "] ");
    }

    public void setBufferProgress(int bufferProgress) {
//        int percent = progressBarValue(bufferProgress);
//        if (percent > 95) percent = 100;
        if (bufferProgress != 0) progressBar.setSecondaryProgress(bufferProgress);
    }

    public static void clearSavedProgress(Context context, String url) {
        ListVideoPlayer.clearSavedProgress(context, url);
    }

    public void setPrepared(boolean prepared) {
        isPrepared = prepared;
    }
}
