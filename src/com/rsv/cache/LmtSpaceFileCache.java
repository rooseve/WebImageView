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

	private final Map<File, Long> lastUsageDates = Collections
			.synchronizedMap(new HashMap<File, Long>());

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

			lastUsageDates.put(cachedFile, cachedFile.lastModified());
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
		lastUsageDates.put(file, currentTime);

		return file;
	}

	private synchronized void freeCache(int reqSize) {
		int tSize = (int) Math.max(Math.ceil(spaceLimit * 0.8), spaceLimit - 1024 * 1024);

		while (cachedSpace + reqSize > tSize) {
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
			lastUsageDates.put(file, currentTime);
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
		File mostLongUsedFile = null;
		Set<Entry<File, Long>> entries = lastUsageDates.entrySet();

		for (Entry<File, Long> entry : entries) {

			if (mostLongUsedFile == null) {

				mostLongUsedFile = entry.getKey();
				oldestUsage = entry.getValue();

			} else {
				Long lastValueUsage = entry.getValue();

				if (lastValueUsage < oldestUsage) {
					oldestUsage = lastValueUsage;
					mostLongUsedFile = entry.getKey();
				}
			}
		}

		if (mostLongUsedFile != null && !mostLongUsedFile.exists()) {
			lastUsageDates.remove(mostLongUsedFile);
			return 1;
		}

		int fileSize = getSize(mostLongUsedFile);

		if (mostLongUsedFile.delete()) {
			LogUtils.i(this, "remove " + mostLongUsedFile.getName());
			lastUsageDates.remove(mostLongUsedFile);
		}
		return fileSize;
	}

	protected int getSize(File file) {
		return (int) file.length();
	}

}
