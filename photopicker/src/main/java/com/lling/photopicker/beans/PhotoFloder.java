package com.lling.photopicker.beans;

import java.io.Serializable;
import java.util.List;

/**
 * @Class: PhotoFloder
 * @Description: 相片文件夹实体类
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoFloder implements Serializable {

    private String name;

    private String dirPath;

    private List<Photo> photoList;

    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public List<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }
}
