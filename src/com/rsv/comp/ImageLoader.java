package com.rsv.comp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

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

	private static final HashMap<Context, ImageLoader> pool = new HashMap<Context, ImageLoader>();

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

		double cacheSpaceInMB = ConfigReader.getImageCacheSpaceInMB(context);

		int memorySizeLimit = ConfigReader.getImageMemorySizeLimit(context);

		this.userAgent = ConfigReader.getUseragentForImageLoader(context);

		this.fileCache = new LmtSpaceFileCache(new File(dir), (int) (cacheSpaceInMB * 1024 * 1024));

		this.memCache = new LmtSizeMemCache<Bitmap>(memorySizeLimit);

		LogUtils.i(this, String.format(
				"ImageLoader: memsize:%d, filespace:%fMB, dir: %s, useragent:%s", memorySizeLimit,
				cacheSpaceInMB, dir, this.userAgent));
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

		Bitmap bm = this.loadImgFromMem(url);

		if (bm != null) {
			if (downloadProgressListener != null)
				downloadProgressListener.reportProgress(100);
			return bm;
		}

		File file = null;

		synchronized (url.intern()) {

			file = fileCache.get(url);

			if (file == null) {

				file = fileCache.getTargetFile(url);

				if (HttpClientUtils.downloadToFile(url, file, this.userAgent,
						downloadProgressListener)) {

					fileCache.put(url, file);

				}

			} else {
				LogUtils.i(this, "filecache got: " + url);
			}
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
}
