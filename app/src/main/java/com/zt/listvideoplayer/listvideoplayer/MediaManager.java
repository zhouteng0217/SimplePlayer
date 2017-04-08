package com.zt.listvideoplayer.listvideoplayer;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.lang.reflect.Method;
import java.util.Map;

public class MediaManager implements TextureView.SurfaceTextureListener
        , MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
        , MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener
        , MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    public static String TAG = "MediaManager";

    private static MediaManager mediaManager;
    public static ResizeTextureView textureView;

    public static SurfaceTexture savedSurfaceTexture;
    public MediaPlayer mediaPlayer = new MediaPlayer();
    public static String CURRENT_PLAYING_URL;

    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;

    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;

    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    private Handler mainThreadHandler;

    public static MediaManager instance() {
        if (mediaManager == null) {
            mediaManager = new MediaManager();
        }
        return mediaManager;
    }

    private MediaManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public Point getVideoSize() {
        if (currentVideoWidth != 0 && currentVideoHeight != 0) {
            return new Point(currentVideoWidth, currentVideoHeight);
        } else {
            return null;
        }
    }

    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    try {
                        currentVideoWidth = 0;
                        currentVideoHeight = 0;
                        mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        Class<MediaPlayer> clazz = MediaPlayer.class;
                        Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                        method.invoke(mediaPlayer, CURRENT_PLAYING_URL, null);
                        mediaPlayer.setLooping(false);
                        mediaPlayer.setOnPreparedListener(MediaManager.this);
                        mediaPlayer.setOnCompletionListener(MediaManager.this);
                        mediaPlayer.setOnBufferingUpdateListener(MediaManager.this);
                        mediaPlayer.setScreenOnWhilePlaying(true);
                        mediaPlayer.setOnSeekCompleteListener(MediaManager.this);
                        mediaPlayer.setOnErrorListener(MediaManager.this);
                        mediaPlayer.setOnInfoListener(MediaManager.this);
                        mediaPlayer.setOnVideoSizeChangedListener(MediaManager.this);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setSurface(new Surface(savedSurfaceTexture));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HANDLER_RELEASE:
                    mediaPlayer.release();
                    break;
            }
        }
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.i(TAG, "onSurfaceTextureAvailable [" + this.hashCode() + "] ");
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.i(TAG, "onSurfaceTextureSizeChanged [" + this.hashCode() + "] ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onPrepared();
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onAutoCompletion();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, final int percent) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onSeekComplete();
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onError(what, extra);
                }
            }
        });
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onInfo(what, extra);
                }
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        currentVideoWidth = width;
        currentVideoHeight = height;
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerManager.getInstance().getCurrentVideoPlayer() != null) {
                    VideoPlayerManager.getInstance().getCurrentVideoPlayer().onVideoSizeChanged();
                }
            }
        });
    }

}
