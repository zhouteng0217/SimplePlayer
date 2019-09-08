package com.zt.core.base;

import android.view.View;

public interface  IRenderView {

    View getRenderView();

    void setPlayer(IMediaPlayer player);

    IMediaPlayer getPlayer();
}
