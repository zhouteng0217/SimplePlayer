package com.zt.core.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.zt.core.listener.OnFullscreenChangedListener;

/**
 * 专为列表视频播放定制的播放器,可以自己按需实现
 */
public class ListVideoView extends StandardVideoView {

    public ListVideoView(@NonNull Context context) {
        super(context);
    }

    public ListVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        back.setVisibility(View.GONE);
        isSupportVolume = false;
        isSupportBrightness = false;
        isSupportSeek = false;

        setOnFullscreenChangeListener(new OnFullscreenChangedListener() {
            @Override
            public void onFullscreenChange(boolean isFullscreen) {
                back.setVisibility(isFullscreen ? View.VISIBLE : View.GONE);
                isSupportVolume = isFullscreen;
                isSupportBrightness = isFullscreen;
                isSupportSeek = isFullscreen;
            }
        });
    }
}
