package com.zt.core.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.zt.core.base.IMediaPlayer;
import com.zt.core.base.IRenderView;

public class TextureRenderView extends TextureView implements IRenderView, TextureView.SurfaceTextureListener {

    protected SurfaceTexture savedSurfaceTexture;
    protected IMediaPlayer player;
    private int videoWidth, videoHeight;

    public TextureRenderView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
        savedSurfaceTexture = null;
    }

    @Override
    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            player.setSurface(new Surface(surfaceTexture));
        } else {
            setSurfaceTexture(savedSurfaceTexture);
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
    public IMediaPlayer getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(IMediaPlayer player) {
        this.player = player;
    }

    @Override
    public View getRenderView() {
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            setMeasuredDimension(videoWidth, videoHeight);
        }
    }
}
