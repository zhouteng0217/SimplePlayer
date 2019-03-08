package com.zt.simpleplayer.base;

import android.graphics.SurfaceTexture;
import android.view.View;

public abstract class BaseRenderView {

    protected RenderViewCallback renderViewCallback;

    public abstract void resetSurface();

    public abstract View getRenderView();

    public void setRenderViewCallback(RenderViewCallback renderViewCallback) {
        this.renderViewCallback = renderViewCallback;
    }

    public interface RenderViewCallback {
        void prepareWhenRenderViewAvailable();
    }

    public abstract SurfaceTexture getSurfaceTexture();
}
