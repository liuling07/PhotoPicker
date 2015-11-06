package com.lling.photopicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private FlodersPopupWindow mFlodersPopupWindow;

    private TextView mPhotoNumTV;
    private TextView mPhotoNameTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mPhotoNumTV = (TextView) findViewById(R.id.photo_num);
        mPhotoNameTV = (TextView) findViewById(R.id.floder_name);
        if (!OtherUtils.isExternalStorageAvailable()) {
            Toast.makeText(this, "No SD card!", Toast.LENGTH_SHORT).show();
            return;
        }
        getPhotosTask.execute();
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
            @Override
            public void onClick(View v) {
                if (mFlodersPopupWindow == null) {
                    mFlodersPopupWindow = new FlodersPopupWindow(PhotoPickerActivity.this,
                            floders);
                }
                mFlodersPopupWindow.showAsDropDown((RelativeLayout) v.getParent(), 0, 0);
            }
        });
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
