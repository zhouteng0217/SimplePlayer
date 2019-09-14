package com.zt.core.demo;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zt.core.base.BasePlayer;
import com.zt.core.base.PlayerConfig;
import com.zt.core.listener.OnFullscreenChangedListener;
import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.OnVideoSizeChangedListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.view.StandardVideoView;
import com.zt.exoplayer.GoogleExoPlayer;
import com.zt.ijkplayer.IjkPlayer;

public class NormalVideoActivity extends AppCompatActivity {

    private static final String TAG = "NormalVideoActivity";

    private Sample sample;

    protected StandardVideoView videoView;

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

    protected @LayoutRes
    int getLayoutId() {
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

        int renderType;
        switch (sample.renderType) {
            case 0:
                renderType = PlayerConfig.RENDER_TEXTURE_VIEW;
                break;
            case 1:
                renderType = PlayerConfig.RENDER_SURFACE_VIEW;
                break;
            default:
                renderType = PlayerConfig.RENDER_NONE;
        }

        float aspectRatio = TextUtils.isEmpty(sample.aspectRatio) ? 0 :
                Float.parseFloat(sample.aspectRatio.substring(0, sample.aspectRatio.indexOf(":"))) /
                        Float.parseFloat(sample.aspectRatio.substring(sample.aspectRatio.indexOf(":") + 1));

        //设置全屏策略，设置视频渲染界面类型,设置是否循环播放，设置播放器画面高宽比例，设置自定义播放器核心
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .fullScreenMode(sample.fullscreenMode)
                .renderType(renderType)
                .looping(sample.looping)
                .aspectRatio(aspectRatio)
                .player(player)  //IjkPlayer,GoogleExoPlayer 需添加对应的依赖
                .build();

        //在start播放之前，设置playerConfig
        videoView.setPlayerConfig(playerConfig);

        //设置是否支持手势调节音量, 默认支持
        videoView.setSupportVolume(sample.volumeSupport);

        //设置是否支持手势调节亮度，默认支持
        videoView.setSupportBrightness(sample.brightnessSupport);

        //设置是否支持手势调节播放进度，默认支持
        videoView.setSupportSeek(sample.seekSupport);

        //设置是否支持锁定屏幕，默认全屏的时候支持
        videoView.setSupportLock(sample.lockSupport);

        //设置是否根据重力感应旋转全屏, 默认支持
        videoView.setSupportSensorRotate(sample.sensorRotateSupport);

        //设置重力感应旋转是否跟随系统设置中的方向锁定，默认支持(在上面的选项，开启重力感应旋转屏幕支持后，该项才生效)
        videoView.setRotateWithSystem(sample.rotateWithSystem);

        //监听播放器状态变化
        videoView.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                Log.d(TAG, "onStateChange state =" + state);
            }
        });

        //监听播放器全屏状态变化
        videoView.setOnFullscreenChangeListener(new OnFullscreenChangedListener() {
            @Override
            public void onFullscreenChange(boolean isFullscreen) {
                Log.d(TAG, "onFullscreenChange isFullScreen =" + isFullscreen);
            }
        });

        //监听播放器画面大小变化
        videoView.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                Log.d(TAG, "onVideoSizeChanged width =" + width + ", height=" + height);
            }
        });

        videoView.start();
    }

    private void initDescView() {
        TextView descTextView = findViewById(R.id.desc);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("播放器: ");
        stringBuilder.append(sample.player == 0 ? "原生MediaPlayer" : sample.player == 1 ? "Bilibili IjkPlayer" : "Google ExoPlayer");
        stringBuilder.append("\n");

        stringBuilder.append("Render: ");
        stringBuilder.append(sample.renderType == 0 ? "TextureView" : sample.renderType == 1 ? "SurfaceView" : "None");
        stringBuilder.append("\n");

        stringBuilder.append("播放地址: ");
        stringBuilder.append(sample.path);
        stringBuilder.append("\n");

        stringBuilder.append("退出后开启小窗口模式: ");
        stringBuilder.append("float".equals(sample.demoType) ? "是" : "否");
        stringBuilder.append("\n");

        stringBuilder.append("画面比例(高宽比): ");
        stringBuilder.append(TextUtils.isEmpty(sample.aspectRatio) ? "自适应" : sample.aspectRatio);
        stringBuilder.append("\n");

        stringBuilder.append("全屏模式: ");
        stringBuilder.append(sample.fullscreenMode == 0 ? "横向全屏"
                : sample.fullscreenMode == 1 ? "竖向全屏" : "根据视频比例来设定全屏方向");
        stringBuilder.append("\n");

        stringBuilder.append("循环播放: ");
        stringBuilder.append(sample.looping ? "是" : "否");
        stringBuilder.append("\n");

        stringBuilder.append("音量调节手势: ");
        stringBuilder.append(sample.volumeSupport ? "启用" : "关闭");
        stringBuilder.append("\n");

        stringBuilder.append("亮度调节手势: ");
        stringBuilder.append(sample.brightnessSupport ? "启用" : "关闭");
        stringBuilder.append("\n");

        stringBuilder.append("进度调节手势: ");
        stringBuilder.append(sample.seekSupport ? "启用" : "关闭");
        stringBuilder.append("\n");

        stringBuilder.append("全屏时锁定屏幕支持: ");
        stringBuilder.append(sample.lockSupport ? "启用" : "关闭");
        stringBuilder.append("\n");

        stringBuilder.append("重力感应旋转屏幕: ");
        stringBuilder.append(sample.sensorRotateSupport && sample.fullscreenMode == PlayerConfig.LANDSCAPE_FULLSCREEN_MODE ? "启用" : "关闭");
        stringBuilder.append("\n");

        stringBuilder.append("重力感应旋转方向跟随系统设置: ");
        stringBuilder.append(sample.rotateWithSystem ? "启用" : "关闭");
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
