package com.zt.core.base;

/**
 * Created by zhouteng on 2019-09-18
 */
public interface IVideoView {
    /**
     * @return 播放器视图
     */
    BaseVideoView getPlayView();

    /**
     * 播放器渲染画面容器视图，从父控件剥离后，返回
     * @return
     */
    RenderContainerView getRenderContainerViewOffParent();

    /**
     * 播放器渲染画面容器视图
     * @return
     */
    RenderContainerView getRenderContainerView();

    /**
     *  添加播放器画面视图，到播放器界面上
     * @param renderContainerView
     */
    void addRenderContainer(RenderContainerView renderContainerView);

    /**
     * @return 是否支持重力感应旋转屏幕
     */
    boolean supportSensorRotate();

    /**
     * @return 开启了重力感应旋转屏幕后，是否跟随系统方向锁定
     */
    boolean rotateWithSystem();

    /**
     * 播放器状态回调
     * @param state
     */
    void onStateChange(int state);

    /**
     * 播放器画面大小回调
     * @param width
     * @param height
     */
    void onVideoSizeChanged(int width, int height);

    /**
     * 数据网络情况下的提示
     */
    void handleMobileData();

    /**
     * 根据传入的方向来全屏
     * @param orientation
     */
    void startFullscreenWithOrientation(int orientation);

    /**
     * 根据传入的方向竖屏
     * @param orientation
     */
    void exitFullscreenWithOrientation(int orientation);

    /**
     * 是否全屏
     * @return
     */
    boolean isFullScreen();
}
