package com.zt.core.demo;

import android.support.annotation.LayoutRes;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zt.core.view.StandardVideoView;

public class NormalVideoActivity extends BaseDemoActivity {

    protected StandardVideoView videoView;

    @Override
    protected void initView() {
        setActionBarTitle();

        TextView descTextView = findViewById(R.id.desc);
        initDescView(descTextView);

        videoView = findViewById(R.id.video_view);
        initPlayerView(videoView);
    }


    protected void setActionBarTitle() {
        getSupportActionBar().setTitle("Video Play");
    }

    @Override
    protected @LayoutRes
    int getLayoutId() {
        return R.layout.aty_normal_video;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (videoView.onBackKeyPressed()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

}
