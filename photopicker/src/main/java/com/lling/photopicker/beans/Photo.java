package com.lling.photopicker.beans;

import java.io.Serializable;

/**
 * @Class: Photo
 * @Description: 照片实体
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class Photo implements Serializable {

    private int id;
    private String path;  //路径
    private boolean isCamera;

    public Photo(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isCamera() {
        return isCamera;
    }

    public void setIsCamera(boolean isCamera) {
        this.isCamera = isCamera;
    }
}
