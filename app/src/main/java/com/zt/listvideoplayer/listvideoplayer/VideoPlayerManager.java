package com.zt.listvideoplayer.listvideoplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
/**
 * Created by zhouteng on 2017/4/8.
 */

public class VideoPlayerManager {

    private static VideoPlayerManager instance;

    private ListVideoPlayer currentVideoPlayer;

    private Dialog fullVideoDialog;

    private boolean isFullScreen = false;

    private int currPos = -1;

    private ListView listView;
    private int containerId;

    private boolean isToggleFullScreen = false;
    public static VideoPlayerManager getInstance() {
        if (instance == null) {
            instance = new VideoPlayerManager();
        }
        return instance;
    }
    public int getCurrPos() {
        return currPos;
    }
    public void setCurrPos(int currPos) {
        this.currPos = currPos;
    }

    public ListVideoPlayer getCurrentVideoPlayer() {
        return currentVideoPlayer;
    }

    public void setCurrentVideoPlayer(ListVideoPlayer currentVideoPlayer) {
        this.currentVideoPlayer = currentVideoPlayer;
    }

    public void completeAll() {
        if (currentVideoPlayer != null) {
            currentVideoPlayer.onCompletion();
            currentVideoPlayer = null;
        }
    }

    public void removePlayerFromParent() {
        if (currentVideoPlayer != null && currentVideoPlayer.getParent() != null) {
            ((ViewGroup) currentVideoPlayer.getParent()).removeView(currentVideoPlayer);
        }
    }

    public void destory() {
        release();
        currentVideoPlayer = null;
    }
    private void release() {
        if (currentVideoPlayer != null) {
        currentVideoPlayer.onCompletion();
        }
        MediaManager.instance().releaseMediaPlayer();
        removePlayerFromParent();
    }

    public void handleFullScreen(Context context) {
        if (isFullScreen) {
            VideoPlayerManager.getInstance().exitFullScreen(context);
        } else {
            VideoPlayerManager.getInstance().startFullScreen(context);
        }
        isFullScreen = !isFullScreen;
    }

    public void exitFullScreen(Context context) {
        isToggleFullScreen = false;
        currentVideoPlayer.setBackButtonVisibility(View.GONE);
        Activity activity = ListVideoUtils.getActivity(context);
        if (fullVideoDialog != null) {
            fullVideoDialog.dismiss();
        }
        toggledFullscreen(activity, false);
        currentVideoPlayer.post(new Runnable() {
            @Override
            public void run() {
        View curPosView = listView.getChildAt(currPos + listView.getHeaderViewsCount() - listView.getFirstVisiblePosition());
        ViewGroup containerView = null;
        if (curPosView != null) {
            containerView = (ViewGroup) curPosView.findViewById(containerId);
        }
        if (containerView != null) {
            containerView.removeAllViews();
            removePlayerFromParent();
            containerView.addView(currentVideoPlayer);
        }
                listView.setSelection(currPos);
            }
        });
    }

    public void startFullScreen(final Context context) {

        isToggleFullScreen = true;
        currentVideoPlayer.setBackButtonVisibility(View.VISIBLE);
        Activity activity = ListVideoUtils.getActivity(context);

        removePlayerFromParent();
        int screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenHeight, screenWidth);
        currentVideoPlayer.setLayoutParams(layoutParams);

        fullVideoDialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullVideoDialog.setContentView(VideoPlayerManager.getInstance().getCurrentVideoPlayer());
        fullVideoDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    handleFullScreen(context);
                }
                return false;
            }
        });
        fullVideoDialog.show();
        toggledFullscreen(activity, true);
    }

    private void toggledFullscreen(Activity mActivity, boolean fullscreen) {
        if (mActivity == null) {
            return;
        }
        if (fullscreen) {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            mActivity.getWindow().setAttributes(attrs);
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                //noinspection all
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            mActivity.getWindow().setAttributes(attrs);
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                //noinspection all
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    public void listVideoPlayer(ListView listView, int containerId, int position, String url, String title, String thumb, ImageLoader imageLoader) {
        if (listView == null) {
            return;
        }
        if (currentVideoPlayer == null) {
            currentVideoPlayer = new ListVideoPlayerStandard(listView.getContext());
        }
        View curPosView = listView.getChildAt(position + listView.getHeaderViewsCount() - listView.getFirstVisiblePosition());
        ViewGroup containerView = null;
        if (curPosView != null) {
            containerView = (ViewGroup) curPosView.findViewById(containerId);
        }
        if (containerView != null) {
            currPos = position;
            removePlayerFromParent();
            containerView.removeAllViews();
            containerView.addView(currentVideoPlayer);
            currentVideoPlayer.setUp(url, title);
            if(thumb != null && thumb.length() > 0) {
                imageLoader.displayImage(thumb,currentVideoPlayer.thumbImageView);
            }
            currentVideoPlayer.startButton.performClick();
        }
        this.listView = listView;
        this.containerId = containerId;
        listViewOnScroll(imageLoader);
    }

    private void listViewOnScroll(ImageLoader imageLoader) {

        listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, true, true,new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((currPos >= firstVisibleItem - listView.getHeaderViewsCount() + visibleItemCount
                        || currPos < firstVisibleItem - listView.getHeaderViewsCount()) && !isToggleFullScreen) {
                    release();
                }
            }
        }));
    }
    public void onPause() {
        if (currentVideoPlayer != null) {
            currentVideoPlayer.onPause();
        }
    }

    public int getCurrentPosition() {
        if (currentVideoPlayer != null) {
            return currentVideoPlayer.getCurrentPositionWhenPlaying();
        }
        return 0;
    }
}
