package com.zt.simplevideoplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.zt.simplevideo.ListVideoManager;

import java.util.ArrayList;
import java.util.List;


public class ListVideoActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private List<ListItem> listItems = new ArrayList<>();
    private ListView listView;
    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_list_video);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("List Video");

        listView = findViewById(R.id.listview);

        initDatas();

        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);

        listView.setOnScrollListener(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (ListVideoManager.getInstance().onBackKeyPressed()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void initDatas() {
        ListItem listItem = new ListItem();
        listItem.videoUrl = "http://mirror.aarnet.edu.au/pub/TED-talks/AlexLaskey_2013.mp4";
        listItem.videoThumb = "http://ww1.sinaimg.cn/large/635b39e4gy1g0go7gpgf3j20e8080whl.jpg";
        listItems.add(listItem);

        listItem = new ListItem();
        listItem.videoUrl = "http://mirror.aarnet.edu.au/pub/TED-talks/AJJacobs_2007P-480p.mp4";
        listItem.videoThumb = "http://ww1.sinaimg.cn/large/635b39e4gy1g0gokgygg8j20ng0dc7da.jpg";
        listItems.add(listItem);

        listItem = new ListItem();
        listItem.videoUrl = "http://mirror.aarnet.edu.au/pub/TED-talks/AndrewSolomon_2013P.mp4";
        listItem.videoThumb = "http://ww1.sinaimg.cn/large/635b39e4gy1g0gon7nvs3j20e8080whz.jpg";
        listItems.add(listItem);

        listItem = new ListItem();
        listItem.videoUrl = "http://mirror.aarnet.edu.au/pub/TED-talks/DanCobley_2010G_480.mp4";
        listItem.videoThumb = "http://ww1.sinaimg.cn/large/635b39e4gy1g0gorzfkyij20n80dcalf.jpg";
        listItems.add(listItem);

        listItem = new ListItem();
        listItem.videoUrl = "http://mirror.aarnet.edu.au/pub/TED-talks/ChrisBliss_2011X-480p.mp4";
        listItem.videoThumb = "http://ww1.sinaimg.cn/large/635b39e4gy1g0govmjjvbj20ng0dcjx3.jpg";
        listItems.add(listItem);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    //listview中视频滚动不见时，停止播放
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int curPos = ListVideoManager.getInstance().getCurPos();
        if ((curPos >= firstVisibleItem - listView.getHeaderViewsCount() + visibleItemCount
                || curPos < firstVisibleItem - listView.getHeaderViewsCount()) && !ListVideoManager.getInstance().isFullScreen()) {
            ListVideoManager.getInstance().release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ListVideoManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListVideoManager.getInstance().destroy();
    }

    class ListAdapter extends BaseAdapter {

        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item_layout, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.bindData(position);
            return convertView;
        }

        class ViewHolder {

            private ImageView play;
            private ImageView thumb;

            private ViewHolder(View convertView) {
                play = convertView.findViewById(R.id.play);
                thumb = convertView.findViewById(R.id.thumb);
            }

            private void bindData(final int position) {
                final ListItem listItem = listItems.get(position);
                Glide.with(ListVideoActivity.this).load(listItem.videoThumb).into(thumb);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListVideoManager.getInstance().videoPlayer(listView, R.id.container, position, listItem.videoUrl, "title " + position);
                    }
                });
            }
        }

    }

    private class ListItem {
        private String videoUrl;
        private String videoThumb;
    }
}
