package com.rsv.cache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * A size limited memory cache implementation
 * 
 */
public class LmtSizeMemCache<K> extends BaseMemCache<String, K> {

	private final int sizeLimit;

	public LmtSizeMemCache(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	@Override
	public boolean put(String key, K value) {

		if (sizeLimit > 0) {

			if (super.put(key, value)) {

				while (this.size() > sizeLimit) {
					this.removeFirst();
				}
			}

			return true;
		}

		return false;
	}

	@Override
	protected Reference<K> createReference(K value) {
		return new SoftReference<K>(value);
	}
}
