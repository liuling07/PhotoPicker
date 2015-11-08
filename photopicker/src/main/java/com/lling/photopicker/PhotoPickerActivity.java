package com.lling.photopicker;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lling.photopicker.adapters.FloderAdapter;
import com.lling.photopicker.adapters.PhotoAdapter;
import com.lling.photopicker.beans.Photo;
import com.lling.photopicker.beans.PhotoFloder;
import com.lling.photopicker.utils.OtherUtils;
import com.lling.photopicker.utils.PhotoUtils;
import com.lling.photopicker.widgets.FlodersPopupWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Class: PhotoPickerActivity
 * @Description: 照片选择界面
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoPickerActivity extends Activity {

    private GridView mGridView;
    private Map<String, PhotoFloder> mFloderMap;
    private List<Photo> mPhotoLists = new ArrayList<Photo>();
    private PhotoAdapter mPhotoAdapter;
    private ProgressDialog mProgressDialog;
    private RelativeLayout mFloderListLayout;

    private FlodersPopupWindow mFlodersPopupWindow;
    private View mFloderListLayoutView;

    private TextView mPhotoNumTV;
    private TextView mPhotoNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);
        initView();
        if (!OtherUtils.isExternalStorageAvailable()) {
            Toast.makeText(this, "No SD card!", Toast.LENGTH_SHORT).show();
            return;
        }
        getPhotosTask.execute();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mPhotoNumTV = (TextView) findViewById(R.id.photo_num);
        mPhotoNameTV = (TextView) findViewById(R.id.floder_name);
        mFloderListLayout = (RelativeLayout) findViewById(R.id.floder_list_layout);
    }

    /**
     * load success and init the adapter
     */
    private void getPhotosSuccess() {
        mProgressDialog.dismiss();
        mPhotoLists.addAll(mFloderMap.get("所有图片").getPhotoList());
        mPhotoNumTV.setText(mPhotoLists.size() + "张");
        mPhotoAdapter = new PhotoAdapter(this.getApplicationContext(), mPhotoLists);
        mGridView.setAdapter(mPhotoAdapter);
        Set<String> keys = mFloderMap.keySet();
        final List<PhotoFloder> floders = new ArrayList<PhotoFloder>();
        for (String key : keys) {
            floders.add(mFloderMap.get(key));
        }
        mPhotoNameTV.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                /*if (mFlodersPopupWindow == null) {
                    mFlodersPopupWindow = new FlodersPopupWindow(PhotoPickerActivity.this,
                            floders);
                }
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                mFlodersPopupWindow.showAsDropDown(v, 0, 0);//v.getLayoutParams().height*/
                showFloderList(floders);
            }
        });
    }

    private void showFloderList(List<PhotoFloder> floders) {
        if(mFloderListLayoutView == null) {
            mFloderListLayoutView = LayoutInflater.from(this).inflate(
                    R.layout.floderlist_layout, null);
            ListView listView = (ListView) mFloderListLayoutView.findViewById(R.id.listview_floder);
            FloderAdapter adapter = new FloderAdapter(this, floders);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            mFloderListLayout.addView(mFloderListLayoutView);
            initAnimation();
        }
        if(isShow) {
            outAnimatorSet.start();
            isShow = false;
        } else {
            inAnimatorSet.start();
            isShow = true;
        }
    }
    boolean isShow = false;

    AnimatorSet inAnimatorSet = new AnimatorSet();
    AnimatorSet outAnimatorSet = new AnimatorSet();
    private void initAnimation() {
        ObjectAnimator alphaInAnimator, alphaOutAnimator, transInAnimator, transOutAnimator;
        //获取actionBar的高
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        int height = OtherUtils.getHeightInPx(this) - 3*actionBarHeight;
        alphaInAnimator = ObjectAnimator.ofFloat(mGridView, "alpha", 1f, 0.3f);
        alphaOutAnimator = ObjectAnimator.ofFloat(mGridView, "alpha", 0.3f, 1f);
        transInAnimator = ObjectAnimator.ofFloat(mFloderListLayoutView, "translationY", height , 0);
        transOutAnimator = ObjectAnimator.ofFloat(mFloderListLayoutView, "translationY", 0, height);

        LinearInterpolator linearInterpolator = new LinearInterpolator();

        inAnimatorSet.play(transInAnimator).with(alphaInAnimator);
        inAnimatorSet.setDuration(300);
        inAnimatorSet.setInterpolator(linearInterpolator);
        outAnimatorSet.play(transOutAnimator).with(alphaOutAnimator);
        outAnimatorSet.setDuration(300);
        outAnimatorSet.setInterpolator(linearInterpolator);
    }

    /**
     * select floder
     * @param photoFloder
     */
    public void selectFloder(PhotoFloder photoFloder) {
        mPhotoAdapter.setDatas(photoFloder.getPhotoList());
        mPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * a async task that load all photo's path
     */
    private AsyncTask getPhotosTask = new AsyncTask() {
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(PhotoPickerActivity.this, null, "loading...");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            mFloderMap = PhotoUtils.getPhotos(
                    PhotoPickerActivity.this.getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            getPhotosSuccess();
        }
    };

}
