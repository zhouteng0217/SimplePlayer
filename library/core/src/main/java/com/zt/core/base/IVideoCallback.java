package com.zt.core.base;

import com.zt.core.listener.OnFullscreenChangedListener;
import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;

/**
 * 播放器的一些回调接口提供
 */
public interface IVideoCallback {

    /**
     * 设置全屏回调
     * @param onFullscreenChangeListener
     */
    void setOnFullscreenChangeListener(OnFullscreenChangedListener onFullscreenChangeListener);

    /**
     * 设置播放状态回调
     * @param onStateChangedListener
     */
    void setOnStateChangedListener(OnStateChangedListener onStateChangedListener);

    /**
     * 设置播放器大小变更回调
     * @param onVideoSizeChangedListener
     */
    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener);
}
