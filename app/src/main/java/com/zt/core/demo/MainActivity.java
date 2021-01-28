package com.zt.core.demo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    private SampleAdapter sampleAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SimplePlayerDemo");

        ExpandableListView listView = findViewById(R.id.sample_list);
        sampleAdapter = new SampleAdapter();
        listView.setAdapter(sampleAdapter);
        listView.setOnChildClickListener(this);

        new SampleTask(this).execute();

        prepareLocalVideo();
    }

    //复制一份视频文件到文件系统里面，供后面的demo用
    private void prepareLocalVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyTestVideoToLocal();
            }
        }).start();
    }

    private void copyTestVideoToLocal() {
        String path = getExternalFilesDir(null).getAbsolutePath() + "/assets_test_video.mp4";
        File testVideo = new File(path);
        if (testVideo.exists()) {
            return;
        }
        try {
            InputStream inputStream = getAssets().open("assets_test_video.mp4");
            FileOutputStream outputStream = new FileOutputStream(testVideo);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class SampleTask extends AsyncTask<Void, Void, List<SampleGroup>> {

        private WeakReference<MainActivity> activityWeakReference;

        private SampleTask(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected List<SampleGroup> doInBackground(Void... voids) {
            return getSampleGroups(activityWeakReference.get());
        }

        @Override
        protected void onPostExecute(List<SampleGroup> sampleGroups) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.sampleAdapter.setSampleGroups(sampleGroups);
        }
    }

    private static List<SampleGroup> getSampleGroups(Context context) {
        List<SampleGroup> sampleGroups = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("config.json"), "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String result = stringBuilder.toString();
            sampleGroups = new Gson().fromJson(result, new TypeToken<List<SampleGroup>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sampleGroups;
    }

    private final class SampleAdapter extends BaseExpandableListAdapter {

        private List<SampleGroup> sampleGroups;

        public SampleAdapter() {
            sampleGroups = Collections.emptyList();
        }

        public void setSampleGroups(List<SampleGroup> sampleGroups) {
            this.sampleGroups = sampleGroups;
            notifyDataSetChanged();
        }

        @Override
        public Sample getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).samples.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.sample_list_item, parent, false);
            }
            TextView sampleTitle = view.findViewById(R.id.sample_title);
            sampleTitle.setText(sampleGroups.get(groupPosition).samples.get(childPosition).title);
            return view;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).samples.size();
        }

        @Override
        public SampleGroup getGroup(int groupPosition) {
            return sampleGroups.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view =
                        getLayoutInflater()
                                .inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
            }
            ((TextView) view).setText(getGroup(groupPosition).title);
            return view;
        }

        @Override
        public int getGroupCount() {
            return sampleGroups.size();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


    private static final class SampleGroup {

        public String title;
        public List<Sample> samples;

        public SampleGroup(String title, List<Sample> samples) {
            this.title = title;
            this.samples = samples;
        }
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Sample sample = sampleAdapter.getChild(groupPosition, childPosition);
        Intent intent = null;
        switch (sample.demoType) {
            case "list":
                intent = new Intent(this, ListViewVideoActivity.class);
                break;
            case "recycler":
                intent = new Intent(this, RecyclerViewVideoActivity.class);
                break;
            case "float":
                intent = new Intent(this, FloatVideoActivity.class);
                break;
            default:
                intent = new Intent(this, NormalVideoActivity.class);
                break;
        }
        intent.putExtra("sample", sample);
        startActivity(intent);
        return false;
    }

}
