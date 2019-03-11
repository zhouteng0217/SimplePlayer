package com.zt.simpleplayer.render;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zt.simpleplayer.base.BaseRenderView;

public class SurfaceRenderView extends BaseRenderView implements SurfaceHolder.Callback {

    protected SurfaceView surfaceView;
    protected SurfaceHolder savedSurfaceHolder;

    public SurfaceRenderView(Context context) {
        surfaceView = new SurfaceView(context);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void resetSurface() {

    }

    @Override
    public View getRenderView() {
        return surfaceView;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        savedSurfaceHolder = holder;
        player.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
