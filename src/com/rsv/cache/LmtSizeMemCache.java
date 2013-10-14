package com.rsv.cache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A size limited memory cache implementation
 * 
 */
public class LmtSizeMemCache<K> extends BaseMemCache<String, K> {
	private final List<String> queue = Collections
			.synchronizedList(new LinkedList<String>());

	private final int sizeLimit;

	public LmtSizeMemCache(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	@Override
	public boolean put(String key, K value) {
		if (super.put(key, value)) {
			while (sizeLimit > 0 && queue.size() >= sizeLimit) {
				removeNext();
			}

			queue.add(key);

			return true;
		}

		return false;
	}

	@Override
	public void remove(String key) {
		queue.remove(key);
		super.remove(key);
	}

	@Override
	public void clear() {
		queue.clear();
		super.clear();
	}

	protected String removeNext() {
		return queue.remove(0);
	}

	@Override
	protected Reference<K> createReference(K value) {
		return new SoftReference<K>(value);
	}
}
