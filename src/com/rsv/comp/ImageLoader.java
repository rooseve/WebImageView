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
 */
public class ImageLoader {

	private static final HashMap<String, ImageLoader> pool = new HashMap<String, ImageLoader>();

	private static final String InterCachePostTag = "_rsv_imgcache";

	private LmtSpaceFileCache fileCache = null;

	private LmtSizeMemCache<Bitmap> memCache = null;

	private String userAgent;

	public static ImageLoader getImageLoader(final Context context) throws Exception {

		return getImageLoader(context, ConfigReader.getWebImgCacheDir(context));

	}

	private synchronized static ImageLoader getImageLoader(final Context context, String subdir)
			throws Exception {

		File cacheDir;

		if (StorageUtils.isExternalStorageWritable()) {

			cacheDir = StorageUtils.getExternalCacheDirectory(context.getApplicationContext()
					.getPackageName(), subdir);

		} else {

			// throw new Exception("ExternalStorage is not writable");

			cacheDir = new File(context.getApplicationContext().getCacheDir(), subdir
					+ InterCachePostTag);
		}

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

		String dir = cacheDir.getAbsolutePath();
		String ck = String.format("%s", dir);

		ImageLoader obj = pool.get(ck);
		if (obj == null) {

			obj = new ImageLoader(context, dir);
			pool.put(ck, obj);

		}
		return obj;
	}

	private ImageLoader(final Context context, String dir) {

		int cacheSpace = dir.endsWith(InterCachePostTag) ? 5 : ConfigReader
				.getFileCacheSpaceInMB(context);

		fileCache = new LmtSpaceFileCache(new File(dir), cacheSpace * 1024 * 1024);

		memCache = new LmtSizeMemCache<Bitmap>(10);

		this.userAgent = ConfigReader.getUseragentForImageLoader(context);

		LogUtils.i(this, String.format("ImageLoader: size:%dMB, dir: %s, useragent:%s", cacheSpace,
				dir, this.userAgent));
	}

	private Bitmap loadImgFromMem(String url) {
		Bitmap bm = memCache != null ? memCache.get(url) : null;
		if (bm != null) {
			LogUtils.i(this, "memcache got: " + url);
		}

		return bm;
	}

	public void clearMemcache()
	{
		this.memCache.clear();
	}
	
	public void clearFilecache()
	{
		this.fileCache.clear();
	}
	
	public Bitmap loadImg(final String url, final IProgressListener downloadProgressListener)
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
