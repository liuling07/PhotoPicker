package com.lling.photopicker.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.lling.photopicker.PhotoPickerActivity;
import com.lling.photopicker.R;
import com.lling.photopicker.adapters.FloderAdapter;
import com.lling.photopicker.beans.PhotoFloder;
import com.lling.photopicker.utils.OtherUtils;

import java.util.List;

/**
 * @Class: FlodersPopupWindow
 * @Description:  选择文件夹的PopupWindow
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/6
 */
public class FlodersPopupWindow extends PopupWindow {

    private Context mContext;
    private ListView mFloderListView;
    private List<PhotoFloder> mDatas;
    private int mHeight;

    public FlodersPopupWindow(Context context, List<PhotoFloder> datas) {
        this.mContext = context;
        this.mDatas = datas;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_floderlist_layout, null);
        mFloderListView = (ListView)view.findViewById(R.id.listview_floder);
        FloderAdapter adapter = new FloderAdapter(context, datas);
        mFloderListView.setAdapter(adapter);
        mFloderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoFloder photoFloder = mDatas.get(position);
                ((PhotoPickerActivity) mContext).selectFloder(photoFloder);
            }
        });
        mHeight = OtherUtils.getHeightInPx(context)*2 / 3;
        initWindow();
        this.setContentView(view);
    }

    /**
     * 初始化窗口
     */
    private void initWindow() {
        this.setClippingEnabled(false);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(mHeight);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.inoutformbottom);
        ColorDrawable dw = new ColorDrawable(0xFFFFFFFF);
        this.setBackgroundDrawable(dw);

        //popupwindow消失的时候让窗口背景恢复
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                Activity activity = ((Activity) mContext).getParent();
                if (activity == null) {
                    activity = ((Activity) mContext);
                }
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1f;
                activity.getWindow().setAttributes(lp);
            }
        });
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        Activity activity = ((Activity)mContext).getParent();
        if(activity == null) {
            activity = ((Activity)mContext);
        }
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.6f;
        activity.getWindow().setAttributes(lp);
        super.showAsDropDown(anchor, xoff, yoff);
    }
}
