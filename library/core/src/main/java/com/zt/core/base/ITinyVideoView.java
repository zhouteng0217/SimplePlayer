package com.zt.core.base;

import android.view.MotionEvent;

/**
 * Created by zhouteng on 2019-09-14
 * 小窗口模式接口
 */
public interface ITinyVideoView  extends IVideoView{

    /**
     * 销毁播放控制层，不销毁RenderContainer层逻辑
     */
    void destroyPlayerController();

    /**
     * 设置小窗口视频的大小和位置参数
     *
     * @param layoutParams
     */
    void setFloatVideoLayoutParams(LayoutParams layoutParams);

    LayoutParams getFloatVideoLayoutParams();

    void setTinyVideoViewListener(TinyVideoViewListenr listener);

    interface TinyVideoViewListenr {

        /**
         * 关闭小窗口
         */
        void closeVideoView();

        /**
         * 跳转到正常View的状态
         */
        void backToNormalView();

        /**
         * 触摸事件监听
         */
        boolean onTouch(MotionEvent event);
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
