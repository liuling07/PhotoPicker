package com.lling.photopicker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Class: ImageLoader
 * @Description:
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/5
 */
public class ImageLoader {

    private static final int THREAD_POOL_SIZE = 10;
    private final static Executor BITMAP_LOAD_EXECUTOR = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private LruCache<String, Bitmap> mMemoryCache;
    private Handler mHandler;
    private static ImageLoader mInstance;
    private int mWidth;

    private ImageLoader() {
        init();
    }

    private void init() {
        initMemoryCache();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageHolder holder = (ImageHolder) msg.obj;
                String path = holder.path;
                ImageView imageView = holder.imageView;
                Bitmap bitmap = holder.bitmap;
                if (!TextUtils.isEmpty(path) && path.equals(imageView.getTag().toString())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }

    public static synchronized ImageLoader getInstance() {
        if (mInstance == null) {
            mInstance = new ImageLoader();
        }
        return mInstance;
    }

    /**
     * Initialize the memory cache
     */
    public void initMemoryCache() {

        // Set up memory cache
        if (mMemoryCache != null) {
            try {
                clearMemoryCache();
            } catch (Throwable e) {
            }
        }
        // find the max memory size of the system
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (bitmap == null) return 0;
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public void display(String path, ImageView imageView, int width, int height) {
        if (TextUtils.isEmpty(path) || imageView == null) {
            throw new IllegalArgumentException("args may not be null");
        }
        imageView.setTag(path);
        Bitmap bitmap = getBitmapFromMemoryCache(path);
        if (bitmap == null) {
            //load from file
            BitmapLoadTask bitmapLoadTask = new BitmapLoadTask(path, imageView);
            bitmapLoadTask.executeOnExecutor(BITMAP_LOAD_EXECUTOR, width, height);
        } else {
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.bitmap = bitmap;
            imageHolder.imageView = imageView;
            imageHolder.path = path;
            Message msg = Message.obtain();
            msg.obj = imageHolder;
            mHandler.sendMessage(msg);
        }
    }


    /**
     * get bitmap from memory cache
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        mMemoryCache.put(key, bitmap);
    }

    /**
     * Clear the memory cache
     */
    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    private class ImageHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    public class BitmapLoadTask extends AsyncTask<Integer, Object, Bitmap> {

        private final String path;
        private final WeakReference<ImageView> containerReference;

        public BitmapLoadTask(String path, ImageView container) {
            if (container == null || path == null) {
                throw new IllegalArgumentException("args may not be null");
            }
            this.path = path;
            this.containerReference = new WeakReference<ImageView>(container);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap bitmap = null;

            Bitmap bm = decodeSampledBitmapFromFile(path, params[0],
                    params[1]);
            addBitmapToMemoryCache(path, bm);
            bitmap = getBitmapFromMemoryCache(path);

            return bitmap;
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.bitmap = bitmap;
            imageHolder.imageView = containerReference.get();
            imageHolder.path = path;
            Message msg = Message.obtain();
            msg.obj = imageHolder;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        int min = Math.min(width, height);
        int maxReq = Math.max(reqWidth, reqHeight);

        if(min > maxReq) {
            inSampleSize = Math.round((float) min / (float) maxReq);
        }

        return inSampleSize;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromFile(String pathName,
                                               int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }

}
