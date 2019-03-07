package com.zt.simplevideoplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zt.simplevideo.ListVideoManager;

import java.util.ArrayList;
import java.util.List;


public class RecyclerViewVideoActivity extends AppCompatActivity {

    private List<ListItem> listItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_recyclerview_video);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("RecyclerView Video");

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener());

        initDatas();

        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);


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
    protected void onPause() {
        super.onPause();
        ListVideoManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListVideoManager.getInstance().destroy();
    }

    //针对LinearLayoutManager布局的recyclerview, 当滑动当前item不见时，停止播放
    private class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
            int curPos = ListVideoManager.getInstance().getCurPos();

            if ((curPos < firstVisiblePosition || curPos > lastVisiblePosition)
                    && !ListVideoManager.getInstance().isFullScreen()) {
                ListVideoManager.getInstance().release();
            }
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;

        private RecyclerViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((RecyclerViewHolder) holder).bindData(position);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        class RecyclerViewHolder extends RecyclerView.ViewHolder {

            private ImageView play;
            private ImageView thumb;

            private RecyclerViewHolder(View itemView) {
                super(itemView);
                play = itemView.findViewById(R.id.play);
                thumb = itemView.findViewById(R.id.thumb);
            }

            private void bindData(final int position) {
                final ListItem listItem = listItems.get(position);
                Glide.with(RecyclerViewVideoActivity.this).load(listItem.videoThumb).into(thumb);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListVideoManager.getInstance().videoPlayer(recyclerView, R.id.container, position, listItem.videoUrl, "title " + position);
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
