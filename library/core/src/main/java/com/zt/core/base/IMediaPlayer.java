package com.zt.core.base;

public interface IMediaPlayer {

    int STATE_ERROR = -1;
    int STATE_IDLE = 0;
    int STATE_PREPARING = 1;
    int STATE_PREPARED = 2;
    int STATE_BUFFERING_START = 3; //暂停播放开始缓冲更多数据
    int STATE_BUFFERING_END = 4; //缓冲了足够的数据重新开始播放
    int STATE_PLAYING = 5;
    int STATE_PAUSED = 6;
    int STATE_COMPLETED = 7;

    void play();

    void pause();

    boolean isPlaying();

    void release();

    void destroy();

    void seekTo(long position);
}
