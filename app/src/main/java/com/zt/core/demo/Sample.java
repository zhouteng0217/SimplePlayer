package com.zt.core.demo;

import com.zt.core.base.PlayerConfig;

import java.io.Serializable;

public class Sample implements Serializable {

    public String title;
    public String path;
    public int renderType = 0; // 0表示 TextureView, 1表示 SurfaceView
    public int player = 0; //0 表示 Android MediaPlayer, 1表示 IjkPlayer, 2表示ExoPlayer
    public String fileType = "url"; //url, file, raw, assets
    public boolean looping = false; //是否循环播放
    public int fullscreenMode = PlayerConfig.LANDSCAPE_FULLSCREEN_MODE; //全屏模式
    public boolean volumeSupport = true; //是否支持音量手势调节
    public boolean brightnessSupport = true; //是否支持亮度手势调节
    public boolean seekSupport = true; //是否支持手势调节进度(仅限视频，直播流不支持)

    public String demoType = "normal"; // normal 正常视频播放，list listView中列表视频播放, recycler recyclerView中视频播放, scroll scrollview中视频播放

}