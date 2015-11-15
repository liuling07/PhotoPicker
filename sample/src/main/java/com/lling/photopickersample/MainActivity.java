package com.lling.photopickersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lling.photopicker.PhotoPickerActivity;
import com.lling.photopicker.utils.ImageLoader;

public class MainActivity extends Activity {
    private static final int PICK_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.picker_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
                //设置显示拍照框
                intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);
                startActivityForResult(intent, PICK_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PHOTO){
            if(resultCode == RESULT_OK){
                String path = data.getStringExtra(PhotoPickerActivity.KEY_RESULT);
                ImageLoader.getInstance().display(path, (ImageView) findViewById(R.id.imageview), 100, 100);
            }
        }
    }

}
