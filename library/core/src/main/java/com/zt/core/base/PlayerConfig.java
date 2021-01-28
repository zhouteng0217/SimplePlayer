package com.zt.core.base;

import androidx.annotation.IntDef;

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

    public final int screenMode;  //全屏模式
    public final int renderType;  //渲染类型
    public final boolean enableMediaCodec;  //是否启用硬解码
    public final boolean enableOpenSLES;   //是否启用OpenSL ES
    public final boolean looping; //是否循环播放
    public final float aspectRatio; //正常模式下播放画面的高宽比
    public final BasePlayer player;  //自定义播放器播放

    private PlayerConfig(PlayerConfig.Builder builder) {
        this.screenMode = builder.screenMode;
        this.renderType = builder.renderType;
        this.enableMediaCodec = builder.enableMediaCodec;
        this.enableOpenSLES = builder.enableOpenSLES;
        this.player = builder.player;
        this.looping = builder.looping;
        this.aspectRatio = builder.aspectRatio;
    }

    public static class Builder {
        private int screenMode;
        private int renderType;
        private boolean enableMediaCodec;
        private boolean enableOpenSLES;
        private boolean looping;
        private float aspectRatio; //播放画面高宽比
        private BasePlayer player;

        public Builder fullScreenMode(@FullScreeMode int screenMode) {
            this.screenMode = screenMode;
            return this;
        }

        public Builder renderType(@RenderView int renderType) {
            this.renderType = renderType;
            return this;
        }

        public Builder enableMediaCodec(boolean enableMediaCodec) {
            this.enableMediaCodec = enableMediaCodec;
            return this;
        }

        public Builder enableOpenSLES(boolean enableOpenSLES) {
            this.enableOpenSLES = enableOpenSLES;
            return this;
        }

        public Builder looping(boolean isLooping) {
            this.looping = isLooping;
            return this;
        }

        public Builder aspectRatio(float aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        public Builder player(BasePlayer player) {
            this.player = player;
            return this;
        }

        public PlayerConfig build() {
            return new PlayerConfig(this);
        }
    }


}
