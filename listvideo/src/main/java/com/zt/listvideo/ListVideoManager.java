package com.zt.listvideo;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zt.listvideo.view.ListVideoView;

public class ListVideoManager {

    private static ListVideoManager instance;

    private ListVideoView currentVideoView;

    private int curPos = -1;

    public static ListVideoManager getInstance() {
        if (instance == null) {
            instance = new ListVideoManager();
        }
        return instance;
    }

    private void removePlayerFromParent() {
        if (currentVideoView != null && currentVideoView.getParent() != null) {
            ((ViewGroup) currentVideoView.getParent()).removeView(currentVideoView);
        }
    }

    private View getChildViewAt(ListView listView, int position) {
        return listView.getChildAt(position + listView.getHeaderViewsCount() - listView.getFirstVisiblePosition());
    }

    private View getChildViewAt(RecyclerView recyclerView, int position) {
        return recyclerView.getLayoutManager().findViewByPosition(position);
    }

    public void videoPlayer(@NonNull ListView listView, @IdRes int containerId, int position, String url, String title) {
        curPos = position;
        View curPosView = getChildViewAt(listView, position);
        initVideoView(listView.getContext(), curPosView, containerId, url, title);
    }

    public void videoPlayer(@NonNull RecyclerView recyclerView, @IdRes int containerId, int position, String url, String title) {
        curPos = position;
        View curPosView = getChildViewAt(recyclerView, position);
        initVideoView(recyclerView.getContext(), curPosView, containerId, url, title);
    }

    private void initVideoView(Context context, View curPosView, @IdRes int containerId, String url, String title) {
        if (currentVideoView == null) {
            currentVideoView = new ListVideoView(context);
        }
        currentVideoView.release();
        currentVideoView.resetSurface();
        ViewGroup containerView = null;
        if (curPosView != null) {
            containerView = curPosView.findViewById(containerId);
        }
        if (containerView != null) {
            removePlayerFromParent();
            containerView.removeAllViews();
            containerView.addView(currentVideoView);
            currentVideoView.setVideoPath(url);
            currentVideoView.setTitle(title);
            currentVideoView.invalidate();
            currentVideoView.start();
        }
    }

    public void destroy() {
        if (currentVideoView != null) {
            currentVideoView.release();
            currentVideoView = null;
        }
    }

    public void release() {
        if (currentVideoView != null) {
            currentVideoView.destroy();
        }
        removePlayerFromParent();
    }

    public void pause() {
        if (currentVideoView != null) {
            currentVideoView.pause();
        }
    }

    public boolean isFullScreen() {
        return currentVideoView != null && currentVideoView.isFullScreen();
    }

    public boolean onBackKeyPressed() {
        if (currentVideoView != null) {
            return currentVideoView.onBackKeyPressed();
        }
        return false;
    }

    public int getCurPos() {
        return curPos;
    }
}
