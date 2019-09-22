package com.zt.core.base;

import android.view.MotionEvent;

/**
 * Created by zhouteng on 2019-09-14
 * <p>
 * 悬浮窗小窗口视频，需要实现的接口
 */
public interface IFloatView extends IVideoView {

    /**
     * 销毁播放控制层，不销毁RenderContainer层逻辑
     */
    void destroyPlayerController();

    /**
     * 设置悬浮小窗口视频的大小和位置参数
     *
     * @param layoutParams
     */
    void setFloatVideoLayoutParams(LayoutParams layoutParams);

    LayoutParams getFloatVideoLayoutParams();

    void setFloatVideoViewListener(FloatViewListener listener);

    interface FloatViewListener {

        /**
         * 关闭小窗口
         */
        void closeVideoView();

        /**
         * 触摸事件监听
         */
        boolean onTouch(MotionEvent event);

        /**
         * 由悬浮小窗口形态跳转到正常视频形态
         */
        void backToNormalView();
    }

    class LayoutParams {
        public int x;
        public int y;
        public int width;
        public int height;

        public LayoutParams() {

        }

        public LayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
