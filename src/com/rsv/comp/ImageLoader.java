package com.rsv.comp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.rsv.cache.LmtSizeMemCache;
import com.rsv.cache.LmtSpaceFileCache;
import com.rsv.config.ConfigReader;
import com.rsv.utils.HttpClientUtils;
import com.rsv.utils.LogUtils;
import com.rsv.utils.StorageUtils;

/**
 * 
 * Fetch the image data and cache
 * 
 */
public class ImageLoader {

	private static final Map<Context, ImageLoader> pool = Collections
			.synchronizedMap(new HashMap<Context, ImageLoader>());

	private static final Map<String, Object> imgUrlLock = Collections
			.synchronizedMap(new HashMap<String, Object>());

	private final LmtSpaceFileCache fileCache;

	private final LmtSizeMemCache<Bitmap> memCache;

	private final String userAgent;

	public synchronized static ImageLoader getImageLoader(final Context cxt) throws Exception {

		final Context appContext = cxt.getApplicationContext();

		ImageLoader obj = pool.get(appContext);

		if (obj == null) {

			String subdir = ConfigReader.getImageCacheDir(appContext);

			File cacheDir;

			switch (ConfigReader.getImageCacheStorage(appContext)) {

				case INTERNAL:
					cacheDir = new File(appContext.getApplicationContext().getCacheDir(), subdir);
					break;

				default:
					if (StorageUtils.isExternalStorageWritable()) {

						cacheDir = StorageUtils.getExternalCacheDirectory(appContext
								.getApplicationContext().getPackageName(), subdir);

					} else {
						throw new Exception("ExternalStorage is not writable");
					}
			}

			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}

			if (!cacheDir.canWrite()) {
				throw new Exception("WebImageCache dir is not writable: "
						+ cacheDir.getCanonicalPath());
			}

			obj = new ImageLoader(appContext, cacheDir.getCanonicalPath());

			pool.put(appContext, obj);

		}
		return obj;

	}

	private ImageLoader(final Context context, final String dir) {

		int cacheSpace = (int) (ConfigReader.getImageCacheSpaceInMB(context) * 1024 * 1024);

		int memorySizeLimit = ConfigReader.getImageMemorySizeLimit(context);

		this.userAgent = ConfigReader.getUseragentForImageLoader(context);

		this.fileCache = new LmtSpaceFileCache(new File(dir), cacheSpace);

		this.memCache = new LmtSizeMemCache<Bitmap>(memorySizeLimit);

		LogUtils.i(this, String.format(
				"ImageLoader: memsize:%d, filespace:%d, dir: %s, useragent:%s", memorySizeLimit,
				cacheSpace, dir, this.userAgent));
	}

	private Bitmap loadImgFromMem(String url) {
		Bitmap bm = memCache != null ? memCache.get(url) : null;

		if (bm != null) {
			LogUtils.i(this, "memcache got: " + url);
		}

		return bm;
	}

	/**
	 * Clear from memory
	 */
	public void clearMemcache() {
		if (this.memCache != null)
			this.memCache.clear();
	}

	/**
	 * Clear file cache
	 * 
	 */
	public void clearFilecache() {
		this.fileCache.clear();
	}

	/**
	 * Download an image to the file cache dir
	 * 
	 * @param url
	 * @param downloadProgressListener
	 * @return
	 * @throws IOException
	 */
	public Bitmap downloadImg(final String url, final IProgressListener downloadProgressListener)
			throws IOException {

		Bitmap bm = this.tryGetFromMem(url, downloadProgressListener);

		if (bm != null)
			return bm;

		Object urlLock = getUrlLock(url);

		synchronized (urlLock) {

			try {
				bm = this.doFetchImage(url, downloadProgressListener);

			} finally {

				removeUrlLock(url);
			}
		}

		return bm;
	}

	private static void removeUrlLock(String url) {
		imgUrlLock.remove(url);
	}

	private static synchronized Object getUrlLock(String url) {

		if (imgUrlLock.containsKey(url))
			return imgUrlLock.get(url);

		Object lock = new Object();

		imgUrlLock.put(url, lock);

		return lock;
	}

	/**
	 * Get the image from file cache or just download
	 * 
	 * @param url
	 * @param downloadProgressListener
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private Bitmap doFetchImage(final String url, final IProgressListener downloadProgressListener)
			throws ClientProtocolException, IOException {

		Bitmap bm = this.tryGetFromMem(url, downloadProgressListener);

		if (bm != null)
			return bm;

		// get the file
		File file = fileCache.get(url);

		if (file == null) {

			file = fileCache.getTargetFile(url);

			if (HttpClientUtils.downloadToFile(url, file, this.userAgent, downloadProgressListener)) {

				fileCache.put(url, file);

			}

		} else {
			LogUtils.i(this, "filecache got: " + url);
		}

		if (file != null && file.exists()) {

			FileInputStream is = null;

			try {
				is = new FileInputStream(file);

				bm = BitmapFactory.decodeStream(is);

			} catch (FileNotFoundException e) {
				LogUtils.logException(e);

				throw e;

			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						LogUtils.logException(e);
					}
				}
			}
		}

		if (bm != null) {
			if (memCache != null)
				memCache.put(url, bm);
		}

		return bm;
	}

	/**
	 * Try get the image data from memory
	 * 
	 * @param url
	 * @param downloadProgressListener
	 * @return
	 */
	public Bitmap tryGetFromMem(final String url, final IProgressListener downloadProgressListener) {

		Bitmap bm = this.loadImgFromMem(url);

		if (bm != null) {
			if (downloadProgressListener != null)
				downloadProgressListener.reportProgress(100);
			return bm;
		}

		return bm;
	}
}
