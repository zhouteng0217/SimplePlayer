# SimplePlayer
一个简单的视频播放框架, 供学习和项目使用

[![](https://jitpack.io/v/zhouteng0217/SimplePlayer.svg)](https://jitpack.io/#zhouteng0217/SimplePlayer)

## 特性
* 播放核心和播放界面分离，扩展性好
* 支持手势调节音量，亮度，播放进度
* 支持竖屏全屏和横屏全屏，以及根据宽高比自动实现全屏策略
* 支持在ListView和RecyclerView中播放，全屏等操作
* 支持SurfaceView和TextureView播放
* 默认使用原生MediaPlayer播放，支持[ijkplayer](https://github.com/bilibili/ijkplayer)扩展
* 可以灵活自定义播放界面和播放核心

## Demo截图

|  |  |  |  |  |
|:--:|:--:|---:|:--:|:--:|
|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/1.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/2.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/3.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/4.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/5.png)|

## 使用

### 1.在project的build.gradle中添加

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

### 2.在module中添加依赖

```
dependencies {
    implementation 'com.github.zhouteng0217:SimplePlayer:1.0.0'
}
```

或者将library中代码下载下来导入到项目中，以源码的形式引用

### 3.在xml布局中引入

```
<com.zt.simpleplayer.view.StandardVideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

### 4.正常模式下视频播放

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.aty_normal_video);

    videoView = findViewById(R.id.video_view);
    videoView.setVideoPath("http://mirror.aarnet.edu.au/pub/TED-talks/AlexLaskey_2013.mp4");

    //设置全屏策略，设置视频渲染界面类型,设置是否循环播放，设置自定义播放器
    PlayerConfig playerConfig = new PlayerConfig.Builder()
            .fullScreenMode(PlayerConfig.AUTO_FULLSCREEN_MODE)
            .renderType(PlayerConfig.RENDER_TEXTURE_VIEW)
            .looping(true)
            .player(new IjkPlayer(this))  //IjkPlayer需添加对应的依赖
            .build();
    videoView.setPlayerConfig(playerConfig);

    //设置是否支持手势调节音量, 默认支持
    videoView.setSupportVolume(true);

    //设置是否支持手势调节亮度，默认支持
    videoView.setSupportBrightness(true);

    //设置是否支持手势调节播放进度，默认支持
    videoView.setSupportSeek(true);

    //设置是否支持锁定屏幕，默认全屏的时候支持
    videoView.setSupportLock(true);

    videoView.start();
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
```

### 5.ListView或RecyclerView中视频播放（具体查看demo)

```
//Listview中，传入listview, 包裹播放器的容器id,listview中item的位置，视频url，标题
play.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        ListVideoManager.getInstance().videoPlayer(listView, R.id.container, position, videoUrl, "title " + position);
    }
});

//RecyclerView中，传入recyclerview, 包裹播放器的容器id,recyclerview中item的位置，视频url，标题
play.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        ListVideoManager.getInstance().videoPlayer(recyclerView, R.id.container, position, videoUrl, "title " + position);
    }
});

```

### 6.正常模式下自定义播放器

```
//设置自定义播放器
PlayerConfig playerConfig = new PlayerConfig.Builder()
        .player(new IjkPlayer(this))  //IjkPlayer需添加对应的依赖
        .build();
videoView.setPlayerConfig(playerConfig);
```

### 7.自定义列表视频的播放界面和播放器

```
//自定义ListView中播放视频的控件,配置ijkplayer来播放
class CustomListVideoView extends ListVideoView {

    public CustomListVideoView(@NonNull Context context) {
        super(context);
    }

    public CustomListVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .player(new IjkPlayer(getContext()))
                .build();
        setPlayerConfig(playerConfig);

    }
}

play.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        ListVideoManager.getInstance().play(listView
                , R.id.container
                , position
                , listItem.videoUrl
                , "title " + position
                , new CustomListVideoView(context));
    }
});
```

也可以通继承StandardVideoView, BaseVideoView， ListVideoView来实现自定义各个界面，可以通过继承BasePlayer来实现自定义播放器核心。

### 注意事项

* 默认使用TextureView来实现的视频播放，需要minSdkVersion=16，并且开启硬件加速。
* 使用IjkPlayer播放时，需先添加对应的依赖