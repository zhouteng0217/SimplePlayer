package com.zt.core.render;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zt.core.base.IMediaPlayer;
import com.zt.core.base.IRenderView;

public class SurfaceRenderView extends SurfaceView implements IRenderView, SurfaceHolder.Callback {

    protected IMediaPlayer player;

    private int videoWidth, videoHeight;

    public SurfaceRenderView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }

    @Override
    public View getRenderView() {
        return this;
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
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            setMeasuredDimension(videoWidth, videoHeight);
        }
    }
}
