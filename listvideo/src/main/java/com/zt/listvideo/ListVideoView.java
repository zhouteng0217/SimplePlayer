package com.zt.listvideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

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
        setOnFullScreenChangeListener(new OnFullScreenChangeListener() {
            @Override
            public void onFullScreenChange(boolean isFullScreen) {
                back.setVisibility(isFullScreen ? View.VISIBLE : View.GONE);
            }
        });
    }
}
