package com.lling.photopicker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.lling.photopicker.beans.Photo;
import com.lling.photopicker.beans.PhotoFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Class: PhotoUtils
 * @Description:
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoUtils {


    public static Map<String, PhotoFolder> getPhotos(Context context) {
        Map<String, PhotoFolder> folderMap = new HashMap<>();

        String allPhotosKey = "所有图片";
        PhotoFolder allFolder = new PhotoFolder();
        allFolder.setName(allPhotosKey);
        allFolder.setDirPath(allPhotosKey);
        allFolder.setPhotoList(new ArrayList<Photo>());
        folderMap.put(allPhotosKey, allFolder);

        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpeg和png的图片
        Cursor mCursor = mContentResolver.query(imageUri, null,
                MediaStore.Images.Media.MIME_TYPE + " in(?, ?)",
                new String[] { "image/jpeg", "image/png" },
                MediaStore.Images.Media.DATE_MODIFIED + " desc");

        int pathIndex = mCursor
                .getColumnIndex(MediaStore.Images.Media.DATA);

        if (mCursor.moveToFirst()) {
            do {
                // 获取图片的路径
                String path = mCursor.getString(pathIndex);

                // 获取该图片的父路径名
                File parentFile = new File(path).getParentFile();
                if (parentFile == null) {
                    continue;
                }
                String dirPath = parentFile.getAbsolutePath();

                if (folderMap.containsKey(dirPath)) {
                    Photo photo = new Photo(path);
                    PhotoFolder photoFolder = folderMap.get(dirPath);
                    photoFolder.getPhotoList().add(photo);
                    folderMap.get(allPhotosKey).getPhotoList().add(photo);
                    continue;
                } else {
                    // 初始化imageFolder
                    PhotoFolder photoFolder = new PhotoFolder();
                    List<Photo> photoList = new ArrayList<>();
                    Photo photo = new Photo(path);
                    photoList.add(photo);
                    photoFolder.setPhotoList(photoList);
                    photoFolder.setDirPath(dirPath);
                    photoFolder.setName(dirPath.substring(dirPath.lastIndexOf(File.separator) + 1, dirPath.length()));
                    folderMap.put(dirPath, photoFolder);
                    folderMap.get(allPhotosKey).getPhotoList().add(photo);
                }
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        return folderMap;
    }

}
