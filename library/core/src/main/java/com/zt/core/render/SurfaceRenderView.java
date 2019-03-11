package com.zt.core.render;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zt.core.base.BaseRenderView;

public class SurfaceRenderView extends BaseRenderView implements SurfaceHolder.Callback {

    protected SurfaceView surfaceView;

    public SurfaceRenderView(Context context) {
        surfaceView = new SurfaceView(context);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public View getRenderView() {
        return surfaceView;
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
}
