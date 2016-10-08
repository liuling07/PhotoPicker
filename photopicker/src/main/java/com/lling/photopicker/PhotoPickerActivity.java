package com.lling.photopicker;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lling.photopicker.adapters.FolderAdapter;
import com.lling.photopicker.adapters.PhotoAdapter;
import com.lling.photopicker.beans.Photo;
import com.lling.photopicker.beans.PhotoFolder;
import com.lling.photopicker.utils.LogUtils;
import com.lling.photopicker.utils.OtherUtils;
import com.lling.photopicker.utils.PhotoUtils;

import java.io.File;
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
public class PhotoPickerActivity extends Activity implements PhotoAdapter.PhotoClickCallBack {

    public final static String TAG = "PhotoPickerActivity";

    public final static String KEY_RESULT = "picker_result";
    public final static int REQUEST_CAMERA = 1;

    /** 是否显示相机 */
    public final static String EXTRA_SHOW_CAMERA = "is_show_camera";
    /** 照片选择模式 */
    public final static String EXTRA_SELECT_MODE = "select_mode";
    /** 最大选择数量 */
    public final static String EXTRA_MAX_MUN = "max_num";
    /** 单选 */
    public final static int MODE_SINGLE = 0;
    /** 多选 */
    public final static int MODE_MULTI = 1;
    /** 默认最大选择数量 */
    public final static int DEFAULT_NUM = 9;

    private final static String ALL_PHOTO = "所有图片";
    /** 是否显示相机，默认不显示 */
    private boolean mIsShowCamera = false;
    /** 照片选择模式，默认是单选模式 */
    private int mSelectMode = 0;
    /** 最大选择数量，仅多选模式有用 */
    private int mMaxNum;

    private GridView mGridView;
    private Map<String, PhotoFolder> mFolderMap;
    private List<Photo> mPhotoLists = new ArrayList<>();
    private ArrayList<String> mSelectList = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private ProgressDialog mProgressDialog;
    private ListView mFolderListView;

    private TextView mPhotoNumTV;
    private TextView mPhotoNameTV;
    private Button mCommitBtn;
    /** 文件夹列表是否处于显示状态 */
    boolean mIsFolderViewShow = false;
    /** 文件夹列表是否被初始化，确保只被初始化一次 */
    boolean mIsFolderViewInit = false;

    /** 拍照时存储拍照结果的临时文件 */
    private File mTmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);
        initIntentParams();
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
        findViewById(R.id.bottom_tab_bar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //消费触摸事件，防止触摸底部tab栏也会选中图片
                return true;
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 初始化选项参数
     */
    private void initIntentParams() {
        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        mSelectMode = getIntent().getIntExtra(EXTRA_SELECT_MODE, MODE_SINGLE);
        mMaxNum = getIntent().getIntExtra(EXTRA_MAX_MUN, DEFAULT_NUM);
        if(mSelectMode == MODE_MULTI) {
            //如果是多选模式，需要将确定按钮初始化以及绑定事件
            mCommitBtn = (Button) findViewById(R.id.commit);
            mCommitBtn.setVisibility(View.VISIBLE);
            mCommitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectList.addAll(mPhotoAdapter.getmSelectedPhotos());
                    returnData();
                }
            });
        }
    }

    private void getPhotosSuccess() {
        mProgressDialog.dismiss();
        mPhotoLists.addAll(mFolderMap.get(ALL_PHOTO).getPhotoList());

        mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                R.string.photos_num, mPhotoLists.size()));

        mPhotoAdapter = new PhotoAdapter(this.getApplicationContext(), mPhotoLists);
        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
        mPhotoAdapter.setSelectMode(mSelectMode);
        mPhotoAdapter.setMaxNum(mMaxNum);
        mPhotoAdapter.setPhotoClickCallBack(this);
        mGridView.setAdapter(mPhotoAdapter);
        Set<String> keys = mFolderMap.keySet();
        final List<PhotoFolder> folders = new ArrayList<>();
        for (String key : keys) {
            if (ALL_PHOTO.equals(key)) {
                PhotoFolder folder = mFolderMap.get(key);
                folder.setIsSelected(true);
                folders.add(0, folder);
            } else {
                folders.add(mFolderMap.get(key));
            }
        }
        mPhotoNameTV.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                toggleFolderList(folders);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPhotoAdapter.isShowCamera() && position == 0) {
                    showCamera();
                    return;
                }
                selectPhoto(mPhotoAdapter.getItem(position));
            }
        });
    }

    /**
     * 点击选择某张照片
     * @param photo
     */
    private void selectPhoto(Photo photo) {
        LogUtils.e(TAG, "selectPhoto");
        if(photo == null) {
            return;
        }
        String path = photo.getPath();
        if(mSelectMode == MODE_SINGLE) {
            mSelectList.add(path);
            returnData();
        }
    }

    @Override
    public void onPhotoClick() {
        LogUtils.e(TAG, "onPhotoClick");
        List<String> list = mPhotoAdapter.getmSelectedPhotos();
        if(list != null && list.size()>0) {
            mCommitBtn.setEnabled(true);
            mCommitBtn.setText(OtherUtils.formatResourceString(getApplicationContext(),
                    R.string.commit_num, list.size(), mMaxNum));
        } else {
            mCommitBtn.setEnabled(false);
            mCommitBtn.setText(R.string.commit);
        }
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putStringArrayListExtra(KEY_RESULT, mSelectList);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 显示或者隐藏文件夹列表
     * @param folders
     */
    private void toggleFolderList(final List<PhotoFolder> folders) {
        //初始化文件夹列表
        if(!mIsFolderViewInit) {
            ViewStub folderStub = (ViewStub) findViewById(R.id.floder_stub);
            folderStub.inflate();
            View dimLayout = findViewById(R.id.dim_layout);
            mFolderListView = (ListView) findViewById(R.id.listview_floder);
            final FolderAdapter adapter = new FolderAdapter(this, folders);
            mFolderListView.setAdapter(adapter);
            mFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (PhotoFolder folder : folders) {
                        folder.setIsSelected(false);
                    }
                    PhotoFolder folder = folders.get(position);
                    folder.setIsSelected(true);
                    adapter.notifyDataSetChanged();

                    mPhotoLists.clear();
                    mPhotoLists.addAll(folder.getPhotoList());
                    if (ALL_PHOTO.equals(folder.getName())) {
                        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
                    } else {
                        mPhotoAdapter.setIsShowCamera(false);
                    }
                    //这里重新设置adapter而不是直接notifyDataSetChanged，是让GridView返回顶部
                    mGridView.setAdapter(mPhotoAdapter);
                    mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                            R.string.photos_num, mPhotoLists.size()));
                    mPhotoNameTV.setText(folder.getName());
                    toggle();
                }
            });
            dimLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mIsFolderViewShow) {
                        toggle();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            initAnimation(dimLayout);
            mIsFolderViewInit = true;
        }
        toggle();
    }

    /**
     * 弹出或者收起文件夹列表
     */
    private void toggle() {
        if(mIsFolderViewShow) {
            outAnimatorSet.start();
            mIsFolderViewShow = false;
        } else {
            inAnimatorSet.start();
            mIsFolderViewShow = true;
        }
    }


    /**
     * 初始化文件夹列表的显示隐藏动画
     */
    AnimatorSet inAnimatorSet = new AnimatorSet();
    AnimatorSet outAnimatorSet = new AnimatorSet();
    private void initAnimation(View dimLayout) {
        ObjectAnimator alphaInAnimator, alphaOutAnimator, transInAnimator, transOutAnimator;
        //获取actionBar的高
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        /**
         * 这里的高度是，屏幕高度减去上、下tab栏，并且上面留有一个tab栏的高度
         * 所以这里减去3个actionBarHeight的高度
         */
        int height = OtherUtils.getHeightInPx(this) - 3*actionBarHeight;
        alphaInAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0f, 0.7f);
        alphaOutAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0.7f, 0f);
        transInAnimator = ObjectAnimator.ofFloat(mFolderListView, "translationY", height , 0);
        transOutAnimator = ObjectAnimator.ofFloat(mFolderListView, "translationY", 0, height);

        LinearInterpolator linearInterpolator = new LinearInterpolator();

        inAnimatorSet.play(transInAnimator).with(alphaInAnimator);
        inAnimatorSet.setDuration(300);
        inAnimatorSet.setInterpolator(linearInterpolator);
        outAnimatorSet.play(transOutAnimator).with(alphaOutAnimator);
        outAnimatorSet.setDuration(300);
        outAnimatorSet.setInterpolator(linearInterpolator);
    }

    /**
     * 选择文件夹
     * @param photoFolder
     */
    public void selectFolder(PhotoFolder photoFolder) {
        mPhotoAdapter.setDatas(photoFolder.getPhotoList());
        mPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * 获取照片的异步任务
     */
    private AsyncTask getPhotosTask = new AsyncTask() {
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(PhotoPickerActivity.this, null, "loading...");
        }

        @Override
        protected Object doInBackground(Object[] params) {
            mFolderMap = PhotoUtils.getPhotos(
                    PhotoPickerActivity.this.getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            getPhotosSuccess();
        }
    };

    /**
     * 选择相机
     */
    private void showCamera() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = OtherUtils.createFile(getApplicationContext());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }else{
            Toast.makeText(getApplicationContext(),
                    R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 相机拍照完成后，返回图片路径
        if(requestCode == REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mTmpFile.getAbsolutePath())));
                    mSelectList.add(mTmpFile.getAbsolutePath());
                    returnData();
                }
            }else{
                if(mTmpFile != null && mTmpFile.exists()){
                    mTmpFile.delete();
                }
            }
        }
    }
}
