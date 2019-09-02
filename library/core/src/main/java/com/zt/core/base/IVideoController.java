package com.zt.core.base;

import android.support.annotation.RawRes;

import java.util.Map;

/**
 * 播放器的控制行为接口，有哪些播放行为，通过VideoController层来实现
 *
 * 定义了一个基本的播放器需要支持的行为，具体通过VideoController层来实现
 *
 */
public interface IVideoController {

    //设置播放器headers
    void setVideoHeaders(Map<String,String> headers);

    //设置播放器url或本地file路径
    void setVideoUrlPath(String url);

    //设置raw下视频的路径
    void setVideoRawPath(@RawRes int rawId);

    //设置assets下视频的路径
    void setVideoAssetPath(String assetFileName);

    //播放器是否全屏
    boolean isFullScreen();

    //播放器全屏
    void startFullScreen();

    //播放器退出全屏
    void exitFullscreen();

    //是否正在播放
    boolean isPlaying();

    //播放
    void start();

    //暂停
    void pause();

    //释放播放器
    void release();

    //重新播放
    void replay();

    //销毁
    void destroy();

    //获取播放器实例
    BasePlayer getPlayer();
}
