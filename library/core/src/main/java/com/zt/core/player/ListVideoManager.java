package com.zt.core.player;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zt.core.base.BaseVideoView;
import com.zt.core.view.ListVideoView;

public class ListVideoManager {

    private static ListVideoManager instance;

    protected BaseVideoView currentVideoView;

    protected int curPos = -1;

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

    public void play(@NonNull ListView listView, @IdRes int containerId, int position, String url, String title) {
        play(listView, containerId, position, url, title, null);
    }

    /**
     *
     * @param listView       视频所在的ListView
     * @param containerId    ListView的item中用于包裹video的容器ID
     * @param position       视频所在item的位置
     * @param url            视频url
     * @param title          视频title
     * @param customVideoView  自定义VideoView
     */
    public void play(@NonNull ListView listView, @IdRes int containerId, int position, String url, String title, BaseVideoView customVideoView) {
        curPos = position;
        View curPosView = getChildViewAt(listView, position);
        initVideoView(listView.getContext(), curPosView, containerId, url, title, customVideoView);
    }

    public void play(@NonNull RecyclerView recyclerView, @IdRes int containerId, int position, String url, String title) {
        play(recyclerView, containerId, position, url, title, null);
    }


    /**
     *
     * @param recyclerView      视频所在的RecyclerView
     * @param containerId    RecyclerView的item中用于包裹video的容器ID
     * @param position       视频所在item的位置
     * @param url            视频url
     * @param title          视频title
     * @param customVideoView  自定义VideoView
     */
    public void play(@NonNull RecyclerView recyclerView, @IdRes int containerId, int position, String url, String title, BaseVideoView customVideoView) {
        curPos = position;
        View curPosView = getChildViewAt(recyclerView, position);
        initVideoView(recyclerView.getContext(), curPosView, containerId, url, title, customVideoView);
    }

    protected void initVideoView(Context context, View curPosView, @IdRes int containerId, String url, String title, BaseVideoView customVideoView) {

        if (currentVideoView != null) {
            currentVideoView.release();
            removePlayerFromParent();
        }
        currentVideoView = customVideoView;

        if (currentVideoView == null) {
            currentVideoView = newVideoViewInstance(context);
        }

        ViewGroup containerView = null;
        if (curPosView != null) {
            containerView = curPosView.findViewById(containerId);
        }
        if (containerView != null) {
            containerView.removeAllViews();
            containerView.addView(currentVideoView);
            currentVideoView.setVideoUrlPath(url);
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
            currentVideoView.release();
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

    //用于BaseVideoView的继承扩展
    protected BaseVideoView newVideoViewInstance(Context context) {
        return new ListVideoView(context);
    }
}
