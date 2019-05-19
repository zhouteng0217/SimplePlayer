package com.zt.core.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zt.core.base.BasePlayer;
import com.zt.core.base.PlayerConfig;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.player.ListVideoManager;
import com.zt.core.view.ListVideoView;
import com.zt.exoplayer.GoogleExoPlayer;
import com.zt.ijkplayer.IjkPlayer;

import java.util.ArrayList;
import java.util.List;


public class ListViewVideoActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private List<ListItem> listItems = new ArrayList<>();
    private ListView listView;
    private ListAdapter listAdapter;

    private Sample sample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_list_video);

        initDatas();

        sample = (Sample) getIntent().getSerializableExtra("sample");

        initToolbar();

        listView = findViewById(R.id.listview);
        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);

        listView.setOnScrollListener(this);

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ListView");

        String player = sample.player == 0 ? "Android MediaPlayer" : sample.player == 1 ? "Bilibili IjkPlayer" : "Google ExoPlayer";
        String render = sample.renderType == 0 ? "TextureView" : "SurfaceView";


        TextView descTextView = findViewById(R.id.desc);
        descTextView.setText(player + "+" + render);
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
                Glide.with(ListViewVideoActivity.this).load(listItem.videoThumb).into(thumb);
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
            }
        }
    }

    private class ListItem {
        private String videoUrl;
        private String videoThumb;
    }

    //自定义ListView中播放视频的控件, 配置播放器
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
            BasePlayer player = sample.player == 0 ? new AndroidPlayer(getContext()) : sample.player == 1 ? new IjkPlayer(getContext()) : new GoogleExoPlayer(getContext());
            PlayerConfig playerConfig = new PlayerConfig.Builder()
                    .player(player)
                    .renderType(sample.renderType)
                    .looping(sample.looping)
                    .fullScreenMode(sample.fullscreenMode)
                    .build();
            setPlayerConfig(playerConfig);

        }
    }
}
