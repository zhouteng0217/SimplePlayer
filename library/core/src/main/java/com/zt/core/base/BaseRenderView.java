package com.zt.core.base;

import android.view.View;

public abstract class BaseRenderView {

    protected BasePlayer player;

    public abstract View getRenderView();

    public void setPlayer(BasePlayer player) {
        this.player = player;
    }
}
