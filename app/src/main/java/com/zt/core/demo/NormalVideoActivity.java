package com.zt.core.demo;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zt.core.base.BasePlayer;
import com.zt.core.base.PlayerConfig;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.view.StandardVideoView;
import com.zt.exoplayer.GoogleExoPlayer;
import com.zt.ijkplayer.IjkPlayer;

public class NormalVideoActivity extends AppCompatActivity {

    private Sample sample;

    private StandardVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setActionBarTitle();

        sample = (Sample) getIntent().getSerializableExtra("sample");

        videoView = findViewById(R.id.video_view);
        videoView.setTitle(sample.title);

        initPlayerView();

        initDescView();
    }

    protected void setActionBarTitle() {
        getSupportActionBar().setTitle("Video Play");
    }

    protected @LayoutRes int getLayoutId() {
        return R.layout.aty_normal_video;
    }

    private void initPlayerView() {
        switch (sample.fileType) {
            case "url":
                videoView.setVideoUrlPath(sample.path);
                break;
            case "file":
                sample.path = getExternalFilesDir(null).getAbsolutePath() + "/assets_test_video.mp4";
                videoView.setVideoUrlPath("file:///" + sample.path);
                break;
            case "raw":
                sample.path = "R.raw.raw_test_video";
                videoView.setVideoRawPath(R.raw.raw_test_video);
                break;
            case "assets":
                sample.path = "assets_test_video.mp4";
                videoView.setVideoAssetPath(sample.path);
                break;
        }

        BasePlayer player;
        switch (sample.player) {
            case 0:
                player = new AndroidPlayer(this);
                break;
            case 1:
                player = new IjkPlayer(this);
                break;
            default:
                player = new GoogleExoPlayer(this);
                break;
        }

        int renderType = sample.renderType == 0 ? PlayerConfig.RENDER_TEXTURE_VIEW : PlayerConfig.RENDER_SURFACE_VIEW;

        //设置全屏策略，设置视频渲染界面类型,设置是否循环播放，设置自定义播放器
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .fullScreenMode(sample.fullscreenMode)
                .renderType(renderType)
                .looping(sample.looping)
                .player(player)  //IjkPlayer,GoogleExoPlayer 需添加对应的依赖
                .build();
        videoView.setPlayerConfig(playerConfig);

        //设置是否支持手势调节音量, 默认支持
        videoView.setSupportVolume(sample.volumeSupport);

        //设置是否支持手势调节亮度，默认支持
        videoView.setSupportBrightness(sample.brightnessSupport);

        //设置是否支持手势调节播放进度，默认支持
        videoView.setSupportSeek(sample.seekSupport);

        //设置是否支持锁定屏幕，默认全屏的时候支持
        videoView.setSupportLock(true);

        videoView.start();
    }

    private void initDescView() {
        TextView descTextView = findViewById(R.id.desc);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("播放器: ");
        stringBuilder.append(sample.player == 0 ? "原生MediaPlayer" : sample.player == 1 ? "Bilibili IjkPlayer" : "Google ExoPlayer");
        stringBuilder.append("\n");

        stringBuilder.append("Render: ");
        stringBuilder.append(sample.renderType == 0 ? "TextureView" : "SurfaceView");
        stringBuilder.append("\n");

        stringBuilder.append("播放地址:");
        stringBuilder.append(sample.path);
        stringBuilder.append("\n");

        stringBuilder.append("全屏模式: ");
        stringBuilder.append(sample.fullscreenMode == 0 ? "横向全屏"
                : sample.fullscreenMode == 1 ? "竖向全屏" : "根据视频比例来设定全屏方向");
        stringBuilder.append("\n");

        stringBuilder.append("循环播放:");
        stringBuilder.append(sample.looping ? "是" : "否");
        stringBuilder.append("\n");

        stringBuilder.append("音量调节手势:");
        stringBuilder.append(sample.volumeSupport ? "支持" : "不支持");
        stringBuilder.append("\n");

        stringBuilder.append("亮度调节手势:");
        stringBuilder.append(sample.brightnessSupport ? "支持" : "不支持");
        stringBuilder.append("\n");

        stringBuilder.append("进度调节手势:");
        stringBuilder.append(sample.seekSupport ? "支持" : "不支持");
        stringBuilder.append("\n");

        descTextView.setText(stringBuilder.toString());
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
