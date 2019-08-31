package com.zt.core.base;

import android.view.ViewGroup;

/**
 * 视频播放器视图层接口, 提供给VideoController层
 *
 * 自定义播放器时，通过实现该接口，来提供VideoController层所需
 *
 */
public interface IVideoView {

    /**
     * @return 播放器视图中承载画面渲染的容器视图
     */
    ViewGroup getSurfaceContainer();

    /**
     * @return 播放器视图
     */
    ViewGroup getPlayView();

    /**
     * @param state 播放器状态回调
     */
    void onStateChange(int state);

    /**
     * 数据网络下，弹出提示具体实现
     */
    void showMobileDataDialog();

}
