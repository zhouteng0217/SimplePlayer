package com.zt.listvideoplayer.listvideoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zt.listvideoplayer.R;

import java.util.Timer;
import java.util.TimerTask;

public class ListVideoPlayerStandard extends ListVideoPlayer {

    protected Timer dismissControlViewTimer;
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    private ProgressBar bottomProgressBar, loadingProgressBar;

    public ListVideoPlayerStandard(Context context) {
        super(context);
    }

    public ListVideoPlayerStandard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        bottomProgressBar = (ProgressBar) findViewById(R.id.bottom_progressbar);
        thumbImageView = (ImageView) findViewById(R.id.thumb);
        thumbImageView.setOnClickListener(this);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);

    }

    @Override
    public int getLayoutId() {
        return R.layout.list_video_layout;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.thumb) {
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentState == CURRENT_STATE_NORMAL) {
                if (!url.startsWith("file") && !ListVideoUtils.isWifiConnected(getContext()) && !isWifiTipDialogShowed) {
                    showWifiDialog();
                    return;
                }
                startVideo();
            } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
                onClickUiToggle();
            }
        } else if (i == R.id.surface_container) {
            startDismissControlViewTimer();
        }
    }


    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startVideo();
                isWifiTipDialogShowed = true;
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        startDismissControlViewTimer();
    }

    protected void startVideo() {
        prepareMediaPlayer();
    }

    @Override
    public void setProgressAndText() {
        super.setProgressAndText();
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        if (progress != 0) bottomProgressBar.setProgress(progress);
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        if (bufferProgress != 0) bottomProgressBar.setSecondaryProgress(bufferProgress);
    }

    @Override
    public void resetProgressAndTime() {
        super.resetProgressAndTime();
        bottomProgressBar.setProgress(0);
        bottomProgressBar.setSecondaryProgress(0);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
        startDismissControlViewTimer();
    }

    public void setAllControlsVisible(int topCon, int bottomCon, int startBtn, int loadingPro,
                                      int thumbImg, int bottomPro) {
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        thumbImageView.setVisibility(thumbImg);
        bottomProgressBar.setVisibility(bottomPro);
    }

    @Override
    public void onStateAction(int state) {
        super.onStateAction(state);
        switch (currentState) {
            case CURRENT_STATE_NORMAL:
                changeUiToNormal();
                break;
            case CURRENT_STATE_PREPARING:
                changeUiToPreparingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PLAYING:
                changeUiToPlayingShow();
                startDismissControlViewTimer();
                break;
            case CURRENT_STATE_PAUSE:
                changeUiToPauseShow();
                cancelDismissControlViewTimer();
                break;
            case CURRENT_STATE_ERROR:
                changeUiToError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                changeUiToCompleteShow();
                cancelDismissControlViewTimer();
                bottomProgressBar.setProgress(100);
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }

    public void onCLickUiToggleToClear() {
        if (currentState == CURRENT_STATE_PREPARING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPreparingClear();
            } else {
            }
        } else if (currentState == CURRENT_STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
            }
        } else if (currentState == CURRENT_STATE_PAUSE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
            }
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToCompleteClear();
            } else {
            }
        } else if (currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingBufferingClear();
            } else {
            }
        }
    }

    public void onClickUiToggle() {
        if (currentState == CURRENT_STATE_PREPARING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPreparingClear();
            } else {
                changeUiToPreparingShow();
            }
        } else if (currentState == CURRENT_STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        } else if (currentState == CURRENT_STATE_PAUSE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
                changeUiToPauseShow();
            }
        } else if (currentState == CURRENT_STATE_AUTO_COMPLETE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToCompleteClear();
            } else {
                changeUiToCompleteShow();
            }
        } else if (currentState == CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingBufferingClear();
            } else {
                changeUiToPlayingBufferingShow();
            }
        }
    }

    public void changeUiToNormal() {
        setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                View.INVISIBLE, View.VISIBLE,  View.INVISIBLE);
        updateStartImage();
    }

    public void changeUiToPreparingShow() {
        setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.VISIBLE, View.VISIBLE, View.INVISIBLE);
    }

    public void changeUiToPreparingClear() {
        setAllControlsVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.VISIBLE, View.VISIBLE, View.INVISIBLE);
    }

    public void changeUiToPlayingShow() {
        setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        updateStartImage();
    }

    public void changeUiToPlayingClear() {
        setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
    }

    public void changeUiToPauseShow() {
        setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        updateStartImage();
    }

    public void changeUiToPauseClear() {
        setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
    }

    public void changeUiToPlayingBufferingShow() {
        setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
    }

    public void changeUiToPlayingBufferingClear() {
        setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.VISIBLE, View.INVISIBLE, View.VISIBLE);
        updateStartImage();
    }

    public void changeUiToCompleteShow() {
        setAllControlsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
        updateStartImage();
    }

    public void changeUiToCompleteClear() {
        setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                View.INVISIBLE, View.VISIBLE, View.VISIBLE);
        updateStartImage();
    }

    public void changeUiToError() {
        setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        updateStartImage();
    }

    public void updateStartImage() {
        if (currentState == CURRENT_STATE_PLAYING) {
            startButton.setImageResource(R.drawable.jc_click_pause_selector);
        } else if (currentState == CURRENT_STATE_ERROR) {
            startButton.setImageResource(R.drawable.jc_click_error_selector);
        } else {
            startButton.setImageResource(R.drawable.jc_click_play_selector);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    onClickUiToggle();
                    break;
            }
        } else if(id == R.id.progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelDismissControlViewTimer();
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    break;
            }
        }
        return super.onTouch(v, event);
    }

    public void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        dismissControlViewTimer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        dismissControlViewTimer.schedule(mDismissControlViewTimerTask, 2500);
    }

    public void cancelDismissControlViewTimer() {
        if (dismissControlViewTimer != null) {
            dismissControlViewTimer.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }

    }

    public class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (currentState != CURRENT_STATE_NORMAL
                    && currentState != CURRENT_STATE_ERROR
                    && currentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bottomContainer.setVisibility(View.INVISIBLE);
                            topContainer.setVisibility(View.INVISIBLE);
                            startButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
    }
}
