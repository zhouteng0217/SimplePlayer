package com.zt.core.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.zt.core.listener.HeadsetBroadcastReceiver;

public class PlayerAudioManager {

    private Context context;
    private AudioManager audioManager;
    private IMediaPlayer mediaPlayer;
    private HeadsetBroadcastReceiver headsetBroadcastReceiver;

    protected final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    private final BroadcastReceiver mediaActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HeadsetBroadcastReceiver.MEDIA_ACTION.equals(intent.getAction())) {
                int mediaStatus = intent.getIntExtra(HeadsetBroadcastReceiver.MEDIA_KEY, -1);
                switch (mediaStatus) {
                    case HeadsetBroadcastReceiver.MEDIA_PAUSE:
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();
                        }
                        break;
                    case HeadsetBroadcastReceiver.MEDIA_PLAY:
                        if (mediaPlayer != null) {
                            mediaPlayer.play();
                        }
                        break;
                }
            }
        }
    };

    public PlayerAudioManager(Context context, IMediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        headsetBroadcastReceiver = new HeadsetBroadcastReceiver();
        registerHeadsetReceiver();
        registerMediaActionReceiver();
    }

    private void registerMediaActionReceiver() {
        IntentFilter intentFilter = new IntentFilter(HeadsetBroadcastReceiver.MEDIA_ACTION);
        context.registerReceiver(mediaActionReceiver, intentFilter);
    }

    private void unregisterMediaActionReceiver() {
        if (context != null) {
            context.unregisterReceiver(mediaActionReceiver);
        }
    }

    private void registerHeadsetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(headsetBroadcastReceiver, intentFilter);

        ComponentName componentName = new ComponentName(context.getPackageName(), HeadsetBroadcastReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(componentName);
    }

    public void unregisterHeadsetReceiver() {
        ComponentName componentName = new ComponentName(context.getPackageName(), HeadsetBroadcastReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(componentName);
        context.unregisterReceiver(headsetBroadcastReceiver);
    }

    //获取音频焦点
    public void requestAudioFocus() {
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    //丢弃音频焦点
    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    //获取最大音量
    public int getStreamMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取当前音量
    public int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //设置音量
    public void setStreamVolume(int value) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    public void destroy() {
        abandonAudioFocus();
        unregisterHeadsetReceiver();
        unregisterMediaActionReceiver();
    }

}
