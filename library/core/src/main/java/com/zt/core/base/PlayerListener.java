package com.zt.core.base;

public interface PlayerListener {
    void play();
    void pause();
    boolean isPlaying();
    void release();
    void destroy();
    void seekTo(long position);
}
