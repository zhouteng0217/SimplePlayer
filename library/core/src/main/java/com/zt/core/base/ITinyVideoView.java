package com.zt.core.base;

/**
 * Created by zhouteng on 2019-09-10
 * <p>
 * 小窗口行为接口
 */
public interface ITinyVideoView {

    /**
     * 设置小窗口播放器视图宽度
     * @param width
     */
    void setTinyVideoViewWidth(int width);

    /**
     * 设置小窗口播放器视图高度
     * @param height
     */
    void setTinyVideoViewHeight(int height);

    /**
     * @return 小窗口播放器视图宽度
     */
    int getTinyVideoViewWidth();

    /**
     * @return 小窗口播放器视图高度
     */
    int getTinyVideoViewHeight();

}
