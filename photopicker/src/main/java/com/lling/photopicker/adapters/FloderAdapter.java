package com.lling.photopicker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lling.photopicker.R;
import com.lling.photopicker.beans.PhotoFloder;
import com.lling.photopicker.utils.ImageLoader;
import com.lling.photopicker.utils.OtherUtils;

import java.util.List;

/**
 * @Class: FloderAdapter
 * @Description: 图片目录适配器
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/6
 */
public class FloderAdapter extends BaseAdapter {

    List<PhotoFloder> mDatas;
    Context mContext;
    int mWidth;

    public FloderAdapter(Context context, List<PhotoFloder> mDatas) {
        this.mDatas = mDatas;
        this.mContext = context;
        mWidth = OtherUtils.dip2px(context, 90);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_floder_layout, null);
            holder.photoIV = (ImageView) convertView.findViewById(R.id.imageview_floder_img);
            holder.floderNameTV = (TextView) convertView.findViewById(R.id.textview_floder_name);
            holder.photoNumTV = (TextView) convertView.findViewById(R.id.textview_photo_num);
            holder.selectIV = (ImageView) convertView.findViewById(R.id.imageview_floder_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.selectIV.setVisibility(View.GONE);
        holder.photoIV.setImageResource(R.drawable.ic_photo_loading);
        PhotoFloder floder = mDatas.get(position);
        if(floder.isSelected()) {
            holder.selectIV.setVisibility(View.VISIBLE);
        }
        holder.floderNameTV.setText(floder.getName());
        holder.photoNumTV.setText(floder.getPhotoList().size() + "张");
        ImageLoader.getInstance().display(floder.getPhotoList().get(0).getPath(), holder.photoIV,
                mWidth, mWidth);
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoIV;
        private TextView floderNameTV;
        private TextView photoNumTV;
        private ImageView selectIV;
    }

}
