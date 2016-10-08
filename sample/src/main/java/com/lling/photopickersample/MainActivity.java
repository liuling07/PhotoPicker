package com.lling.photopickersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.lling.photopicker.PhotoPickerActivity;
import com.lling.photopicker.utils.ImageLoader;
import com.lling.photopicker.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PICK_PHOTO = 1;

    private RadioGroup mChoiceMode, mShowCamera;
    private EditText mRequestNum;
    private LinearLayout mRequestNumLayout;
    private GridView mGrideView;
    private List<String> mResults;
    private GridAdapter mAdapter;
    private int mColumnWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int screenWidth = OtherUtils.getWidthInPx(getApplicationContext());
        mColumnWidth = (screenWidth - OtherUtils.dip2px(getApplicationContext(), 4))/3;
        mChoiceMode = (RadioGroup) findViewById(R.id.choice_mode);
        mShowCamera = (RadioGroup) findViewById(R.id.show_camera);
        mRequestNum = (EditText) findViewById(R.id.request_num);
        mRequestNumLayout = (LinearLayout) findViewById(R.id.num_layout);
        mGrideView = (GridView) findViewById(R.id.gridview);

        mChoiceMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId == R.id.multi){
                    mRequestNumLayout.setVisibility(View.VISIBLE);
                }else{
                    mRequestNumLayout.setVisibility(View.GONE);
                    mRequestNum.setText("");
                }
            }
        });

        findViewById(R.id.picker_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedMode;
                if(mChoiceMode.getCheckedRadioButtonId() == R.id.multi){
                    selectedMode = PhotoPickerActivity.MODE_MULTI;
                }else{
                    selectedMode = PhotoPickerActivity.MODE_SINGLE;
                }

                boolean showCamera = false;
                if(mShowCamera.getCheckedRadioButtonId() == R.id.show) {
                    showCamera = true;
                }

                int maxNum = PhotoPickerActivity.DEFAULT_NUM;
                if(!TextUtils.isEmpty(mRequestNum.getText())){
                    maxNum = Integer.valueOf(mRequestNum.getText().toString());
                }

                Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
                intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, showCamera);
                intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, selectedMode);
                intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, maxNum);
                startActivityForResult(intent, PICK_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PHOTO){
            if(resultCode == RESULT_OK){
                ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                showResult(result);
            }
        }
    }

    private void showResult(ArrayList<String> paths){
        if(mResults == null){
            mResults = new ArrayList<>();
        }
        mResults.clear();
        mResults.addAll(paths);

        if(mAdapter == null){
            mAdapter = new GridAdapter(mResults);
            mGrideView.setAdapter(mAdapter);
        }else {
            mAdapter.setPathList(mResults);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class GridAdapter extends BaseAdapter {
        private List<String> pathList;

        public GridAdapter(List<String> listUrls) {
            this.pathList = listUrls;
        }

        @Override
        public int getCount() {
            return pathList.size();
        }

        @Override
        public String getItem(int position) {
            return pathList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setPathList(List<String> pathList) {
            this.pathList = pathList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.item_image, null);
                imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(imageView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mColumnWidth, mColumnWidth);
                imageView.setLayoutParams(params);
            }else {
                imageView = (ImageView) convertView.getTag();
            }
            ImageLoader.getInstance().display(getItem(position), imageView, mColumnWidth, mColumnWidth);
            return convertView;
        }
    }

}
