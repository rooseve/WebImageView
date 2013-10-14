package com.rsv.utils;

import java.io.File;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

public final class StorageUtils {
	private static final String EXT_STORAGE_PATH_PREFIX = "/Android/data/";
	private static final String EXT_STORAGE_FILES_PATH_SUFFIX = "/files/";
	private static final String EXT_STORAGE_CACHE_PATH_SUFFIX = "/cache/";

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
			return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();

		return stat.getBlockSize() * stat.getAvailableBlocks();
	}

	public static long getExternalFreeSpace() {
		return getFreeSpace(getExternalDir("").toString());
	}

	public static File getExternalCacheDirectory(String packageName,
			String dirName) {
		File cacheDir = getExternalCacheDir(packageName);

		File individualCacheDir = new File(cacheDir, dirName);

		if (!individualCacheDir.exists()) {
			if (!individualCacheDir.mkdir()) {
				individualCacheDir = cacheDir;
			}
		}
		return individualCacheDir;
	}

	public static boolean isExternalStorageWritable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static boolean isExternalStorageReadable() {
		if (isExternalStorageWritable()) {
			return true;
		}
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED_READ_ONLY);
	}

	/**
	 * Return the recommended external files directory, whether using API level
	 * 8 or lower. (Uses getExternalStorageDirectory and then appends the
	 * recommended path.)
	 * 
	 * @param packageName
	 * @return
	 */
	public static File getExternalFilesDir(final String packageName) {
		return getExternalDirAllApiLevels(packageName,
				EXT_STORAGE_FILES_PATH_SUFFIX);
	}

	/**
	 * Return the recommended external cache directory, whether using API level
	 * 8 or lower. (Uses getExternalStorageDirectory and then appends the
	 * recommended path.)
	 * 
	 * @param packageName
	 * @return
	 */
	public static File getExternalCacheDir(final String packageName) {
		return getExternalDirAllApiLevels(packageName,
				EXT_STORAGE_CACHE_PATH_SUFFIX);
	}

	public static File getExternalDir(final String packageName) {
		return getExternalDirAllApiLevels(packageName, "");
	}

	private static File getExternalDirAllApiLevels(final String packageName,
			final String suffixType) {
		File baseDir = new File(Environment.getExternalStorageDirectory()
				+ EXT_STORAGE_PATH_PREFIX + packageName);

		if (!baseDir.exists()) {
			synchronized (FileUtils.DATA_LOCK) {
				baseDir.mkdirs();
				try {
					new File(baseDir, ".nomedia").createNewFile();
				} catch (IOException e) {
					LogUtils.logException(e);
				}
			}

		}

		File dir = new File(baseDir + suffixType);

		if (!dir.exists()) {
			synchronized (FileUtils.DATA_LOCK) {
				dir.mkdir();
			}
		}

		return dir;
	}

}
