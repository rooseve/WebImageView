package com.rsv.cache;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.rsv.utils.LogUtils;
import com.rsv.utils.StorageUtils;

/**
 * A space size limited file cache implementation
 * 
 */
public class LmtSpaceFileCache extends BaseFileCache {

	private int cachedSpace = 0;

	private int spaceLimit;

	private final Map<String, Long> lastUsageDates = Collections
			.synchronizedMap(new HashMap<String, Long>());

	public LmtSpaceFileCache(File cacheDir, int spaceLimit) {
		super(cacheDir);

		setSpaceLimit((int) Math.min(spaceLimit,
				StorageUtils.getFreeSpace(cacheDir.getAbsolutePath())));

		LogUtils.d(this, "size limit: " + this.spaceLimit);

		scanCacheDirAndFillStat();
	}

	public void setSpaceLimit(int spaceLimit) {
		this.spaceLimit = spaceLimit;
	}

	public int getCachedSize() {
		return this.cachedSpace;
	}

	private void scanCacheDirAndFillStat() {
		int size = 0;
		File[] cachedFiles = getCacheDir().listFiles();

		for (File cachedFile : cachedFiles) {
			size += getSize(cachedFile);

			lastUsageDates.put(cachedFile.getAbsolutePath(), cachedFile.lastModified());
		}
		cachedSpace = size;

		LogUtils.i(this, "cached size " + cachedSpace);

		if (cachedSpace > spaceLimit) {
			freeCache(0);
		}
	}

	@Override
	public File put(String key, File file) {
		file = super.put(key, file);

		int valueSize = getSize(file);

		if (cachedSpace + valueSize > spaceLimit) {
			freeCache(valueSize);
		}

		cachedSpace += valueSize;

		Long currentTime = System.currentTimeMillis();
		// file.setLastModified(currentTime);
		lastUsageDates.put(file.getAbsolutePath(), currentTime);

		return file;
	}

	private synchronized void freeCache(int reqiredExtraSize) {
		int tSize = (int) Math.max(Math.ceil(spaceLimit * 0.8), spaceLimit - 1024 * 1024);

		while (cachedSpace + reqiredExtraSize > tSize) {
			int freedSize = removeNext();

			if (freedSize == 0)
				break; // cache is empty (have nothing to delete)

			cachedSpace -= freedSize;
		}

		LogUtils.i(this, "free to " + cachedSpace);
	}

	@Override
	public File get(String key) {
		File file = super.get(key);

		if (file != null) {
			Long currentTime = System.currentTimeMillis();
			// file.setLastModified(currentTime);
			lastUsageDates.put(file.getAbsolutePath(), currentTime);
		}

		return file;
	}

	@Override
	public void remove(String key) {

		File file = this.get(key);

		if (file != null) {
			cachedSpace -= this.getSize(file);
			lastUsageDates.remove(file);
		}

		super.remove(key);
	}

	@Override
	public void clear() {

		lastUsageDates.clear();
		cachedSpace = 0;

		super.clear();
	}

	private int removeNext() {
		if (lastUsageDates.isEmpty()) {
			return 0;
		}

		Long oldestUsage = null;

		String mostLongUsedFilePath = null;

		Set<Entry<String, Long>> entries = lastUsageDates.entrySet();

		for (Entry<String, Long> entry : entries) {

			if (mostLongUsedFilePath == null) {

				mostLongUsedFilePath = entry.getKey();
				oldestUsage = entry.getValue();

			} else {

				Long lastValueUsage = entry.getValue();

				if (lastValueUsage < oldestUsage) {
					oldestUsage = lastValueUsage;
					mostLongUsedFilePath = entry.getKey();
				}
			}
		}

		if (mostLongUsedFilePath != null) {

			lastUsageDates.remove(mostLongUsedFilePath);

			File mostLongUsedFile = new File(mostLongUsedFilePath);

			if (!mostLongUsedFile.exists()) {
				return 1;
			}

			int fileSize = getSize(mostLongUsedFile);

			if (mostLongUsedFile.delete()) {
				LogUtils.i(this, "remove " + mostLongUsedFile.getName());
			}

			return fileSize;
		}

		return 1;
	}

	protected int getSize(File file) {
		return (int) file.length();
	}

}
