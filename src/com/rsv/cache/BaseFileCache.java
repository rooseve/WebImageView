package com.rsv.cache;

import java.io.File;

import com.rsv.utils.FileUtils;

abstract class BaseFileCache {
	protected File cacheDir;

	public BaseFileCache(File cacheDir) {
		if (!cacheDir.exists()) {
			throw new IllegalArgumentException(cacheDir + " not exists");
		}

		this.cacheDir = cacheDir;
	}

	/**
	 * @param key
	 *            the identify string, will be converted to a file name
	 * @param file
	 *            the original file
	 * 
	 * @return the cached file
	 */
	public File put(String key, File file) {
		File tFile = getTargetFile(key);

		if (!file.equals(tFile)) {
			// well, this file is not the target cache file
			FileUtils.copyFile(file, tFile);
		}

		return tFile;
	}

	public void remove(String key) {

		File file = this.get(key);
		if (file != null) {
			file.delete();
		}
	}

	/**
	 * Given a string key, return the targeted cache file
	 * 
	 * @param key
	 * @return
	 */
	public File getTargetFile(String key) {
		return new File(cacheDir, keyToFileName(key));
	}

	public File get(String key) {
		File file = this.getTargetFile(key);

		if (!file.exists())
			return null;

		return file;
	}

	public void clear() {

		File[] files = cacheDir.listFiles();
		if (files != null) {
			for (File f : files) {
				f.delete();
			}
		}
	}

	protected File getCacheDir() {
		return cacheDir;
	}

	protected String keyToFileName(String key) {
		return "c" + Integer.toHexString(key.hashCode());
	}
}
