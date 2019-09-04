# SimplePlayer
一个简单的视频播放框架, 供学习和项目使用

[![](https://jitpack.io/v/zhouteng0217/SimplePlayer.svg)](https://jitpack.io/#zhouteng0217/SimplePlayer)

## 特性
* 播放核心和播放界面分离，扩展性好
* 支持手势调节音量，亮度，播放进度
* 支持竖屏全屏和横屏全屏，以及根据宽高比自动实现全屏策略
* 支持根据重力感应自动旋转屏幕全屏等操作
* 支持在ListView和RecyclerView中播放，全屏等操作
* 支持SurfaceView和TextureView播放
* 支持[ijkplayer](https://github.com/bilibili/ijkplayer)播放扩展, 默认Android原生播放器播放
* 支持[ExoPlayer](https://github.com/google/ExoPlayer)播放扩展
* 支持rtmp播放(限ijkplayer和exoplayer模块)
* 支持播放本地视频，raw和assets视频
* 可以灵活自定义播放界面和播放核心

## Demo截图

|  |  |  |  
|:--:|:--:|---:|
|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/01.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/02.png)|![](https://raw.githubusercontent.com/zhouteng0217/SimplePlayer/master/app/src/main/assets/03.png)|

## 使用

### 1.在project的build.gradle中添加

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
        jcenter()
        google()
    }
}
```

### 2.在module中添加依赖

```
dependencies {
   
   //核心依赖，必需，提供默认的原生MediaPlayer播放支持和标准的播放界面
   implementation 'com.github.zhouteng0217.SimplePlayer:core:1.0.4'
   
   //ijkplayer扩展依赖, 要支持ijiplayer必需添加这两个依赖
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer:1.0.4'
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer-armv7a:1.0.4'
   
   //ijkplayer的其余的处理器架构so库支持依赖,根据需要添加
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer-armv5:1.0.4'
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer-arm64:1.0.4'
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer-x86:1.0.4'
   implementation 'com.github.zhouteng0217.SimplePlayer:ijkplayer-x86_64:1.0.4'
   
   //如需要支持ExoPlayer播放器，需添加以下依赖
   implementation 'com.github.zhouteng0217.SimplePlayer:exoplayer:1.0.4'

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

    //设置是否全屏下支持锁定屏幕
    videoView.setSupportLock(true);

    //设置是否根据重力感应旋转全屏, 默认支持
    videoView.setSupportSensorRotate(true);

    //设置重力感应旋转是否跟随系统设置中的方向锁定，默认支持(在上面的选项，开启重力感应旋转屏幕支持后，该项才生效)
    videoView.setRotateWithSystem(true);

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

### 8.混淆Proguard

```
# ijkplayer模块需添加此proguard

-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

```

### 注意事项

* 默认使用TextureView来实现的视频播放，需要minSdkVersion=16，并且开启硬件加速。
* 使用IjkPlayer,GoogleExoPlayer播放时，需先添加对应的依赖
* 如果没有添加所有abi依赖，请在主module的build.gradle中添加以下代码，限制不同abi库的打包
```

android {
    
    defaultConfig {
        ndk {
            abiFilters "armeabi-v7a", "x86"   //根据需要配置相应的abi
         }
    }

}

```