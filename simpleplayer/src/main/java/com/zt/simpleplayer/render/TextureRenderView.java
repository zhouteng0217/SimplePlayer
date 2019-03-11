package com.zt.simpleplayer.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.zt.simpleplayer.base.BaseRenderView;

public class TextureRenderView extends BaseRenderView implements TextureView.SurfaceTextureListener {

    protected SurfaceTexture savedSurfaceTexture;
    protected TextureView textureView;

    public TextureRenderView(Context context) {
        textureView = new TextureView(context);
        textureView.setSurfaceTextureListener(this);
        savedSurfaceTexture = null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            player.setSurface(new Surface(surfaceTexture));
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void resetSurface() {
        savedSurfaceTexture = null;
    }

    @Override
    public View getRenderView() {
        return textureView;
    }
}
