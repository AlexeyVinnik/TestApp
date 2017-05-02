package test.com.test.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class NetAPI {

    private static final int CACHE_SIZE = 30;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private ImageLoader.ImageCache mImageCache;
    private Context mContext;

    private static NetAPI mInstance;

    private NetAPI(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
        mImageCache = new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
                    cache = new LruCache<String, Bitmap>(CACHE_SIZE);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        };

        mImageLoader = new ImageLoader(mRequestQueue, mImageCache);
    }

    public static synchronized NetAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetAPI(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public Bitmap getBitmap(String url) {
        return mImageCache.getBitmap(url);
    }

    public void putBitmap(String url, Bitmap bitmap) {
        mImageCache.putBitmap(url, bitmap);
    }
}
