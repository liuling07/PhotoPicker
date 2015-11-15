package com.lling.photopicker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
    //图片加载任务队列
    private LinkedList<BitmapLoadTask> mTaskQueue;
    private volatile Semaphore mPoolSemaphore;
    private Handler mHandler;

    private Thread mPoolThread;
    private Handler mPoolThreadHander;
    private volatile Semaphore mSemaphore = new Semaphore(0);
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
                if(imageView == null || bitmap == null) {
                    return;
                }
                if (!TextUtils.isEmpty(path) && path.equals(imageView.getTag().toString())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };

        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                        BitmapLoadTask task = getTask();
                        if(task != null) {
                            task.executeOnExecutor(BITMAP_LOAD_EXECUTOR, mWidth, mWidth);
                        }
                    }
                };
                // 释放一个信号量，告知mPoolThreadHander对象已经创建完成
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        mTaskQueue = new LinkedList<BitmapLoadTask>();
        mPoolSemaphore = new Semaphore(THREAD_POOL_SIZE);
    }

    public static synchronized ImageLoader getInstance() {
        if (mInstance == null) {
            mInstance = new ImageLoader();
        }
        return mInstance;
    }

    /**
     * 初始化内存缓存
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
        mWidth = width;
        imageView.setTag(path);
        Bitmap bitmap = getBitmapFromMemoryCache(path);
        if (bitmap == null) {
            //从文件中加载
            BitmapLoadTask bitmapLoadTask = new BitmapLoadTask(path, imageView);
            addTask(bitmapLoadTask);
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

    private synchronized void addTask(BitmapLoadTask task) {
        try {
            // 如果mPoolThreadHander为空，则阻塞等待mPoolThreadHander创建完毕
            if (mPoolThreadHander == null) {
                mSemaphore.acquire();
            }
        } catch (InterruptedException e) {
        }
        mTaskQueue.add(task);
        mPoolThreadHander.sendEmptyMessage(0);
    }

    private synchronized BitmapLoadTask getTask() {
        return mTaskQueue.removeLast();
    }

    /**
     * 从内存缓存中获取图片
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(!TextUtils.isEmpty(key) && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 清空内存缓存
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
            mPoolSemaphore.release();
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
