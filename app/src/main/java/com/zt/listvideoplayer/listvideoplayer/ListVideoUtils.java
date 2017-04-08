package com.zt.listvideoplayer.listvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;

import com.zt.listvideoplayer.listvideoplayer.ListVideoPlayer;

import java.util.Formatter;
import java.util.Locale;

public class ListVideoUtils {

    public static String stringForTime(int timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    public static Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getActivity(((ContextThemeWrapper) context).getBaseContext());
        } else if(context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    public static void saveProgress(Context context, String url, int progress) {
        if (!ListVideoPlayer.SAVE_PROGRESS) return;
        SharedPreferences spn = context.getSharedPreferences("LIST_VIDEO_PLAYER_PROGRESS",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spn.edit();
        editor.putInt(url, progress);
        editor.commit();
    }

    public static int getSavedProgress(Context context, String url) {
        if (!ListVideoPlayer.SAVE_PROGRESS) return 0;
        SharedPreferences spn;
        spn = context.getSharedPreferences("LIST_VIDEO_PLAYER_PROGRESS",
                Context.MODE_PRIVATE);
        return spn.getInt(url, 0);
    }

    /**
     * if url == null, clear all progress
     *
     * @param context
     * @param url     if url!=null clear this url progress
     */
    public static void clearSavedProgress(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            SharedPreferences spn = context.getSharedPreferences("LIST_VIDEO_PLAYER_PROGRESS",
                    Context.MODE_PRIVATE);
            spn.edit().clear().commit();
        } else {
            SharedPreferences spn = context.getSharedPreferences("LIST_VIDEO_PLAYER_PROGRESS",
                    Context.MODE_PRIVATE);
            spn.edit().putInt(url, 0).commit();
        }
    }
}
