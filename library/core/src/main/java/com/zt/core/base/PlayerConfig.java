package com.zt.core.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PlayerConfig {

    public static final int LANDSCAPE_FULLSCREEN_MODE = 0;  //横向的全屏模式
    public static final int PORTRAIT_FULLSCREEN_MODE = 1;  //竖向的全屏模式
    public static final int AUTO_FULLSCREEN_MODE = 2;      //根据视频内容宽高比，自动判定全屏模式, 宽>高（横屏全屏), 宽 < 高(竖屏全屏)

    @IntDef({LANDSCAPE_FULLSCREEN_MODE, PORTRAIT_FULLSCREEN_MODE, AUTO_FULLSCREEN_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FullScreeMode {
    }

    public static final int RENDER_TEXTURE_VIEW = 0;  //用texture渲染播放界面
    public static final int RENDER_SURFACE_VIEW = 1;  //用surfaceview渲染播放界面
    public static final int RENDER_NONE = 2;          //没有渲染界面

    @IntDef({
            RENDER_NONE, RENDER_SURFACE_VIEW, RENDER_TEXTURE_VIEW
    })
    public @interface RenderView {
    }

    final int screenMode;
    final int renderType;

    private PlayerConfig(PlayerConfig.Builder builder) {
        this.screenMode = builder.screenMode;
        this.renderType = builder.renderType;
    }

    public static class Builder {
        private int screenMode;
        private int renderType;

        public Builder fullScreenMode(@FullScreeMode int screenMode) {
            this.screenMode = screenMode;
            return this;
        }

        public Builder renderType(@RenderView int renderType) {
            this.renderType = renderType;
            return this;
        }

        public PlayerConfig build() {
            return new PlayerConfig(this);
        }
    }


}
