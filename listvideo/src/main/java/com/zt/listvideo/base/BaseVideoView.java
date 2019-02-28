package com.zt.listvideo.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zt.listvideo.listener.StateCallback;

public abstract class BaseVideoView extends FrameLayout implements StateCallback {

    protected BasePlayer player;

    public BaseVideoView(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        player = new BasePlayer(context);
        player.setStateCallback(this);
    }

    public void setVideoPath(String url) {
        player.setVideoPath(url);
    }

    public void startVideo() {
        int currentState = player.getCurrentState();
        if (currentState == BasePlayer.STATE_IDLE || currentState == BasePlayer.STATE_ERROR) {
            prepareToPlay();
        } else if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void prepareToPlay() {
        TextureView textureView = new TextureView(getContext());
        player.setTextureView(textureView);

        ViewGroup surfaceContainer = getSurfaceContainer();
        surfaceContainer.removeAllViews();

        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);

        surfaceContainer.addView(textureView, layoutParams);
    }

    protected abstract ViewGroup getSurfaceContainer();

    protected abstract int getLayoutId();

    @Override
    public abstract void onStateChange(int state);
}
