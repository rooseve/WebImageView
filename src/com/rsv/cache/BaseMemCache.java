package com.rsv.cache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class BaseMemCache<K, V> {

	protected final Map<K, Reference<V>> cacheMap = Collections
			.synchronizedMap(new LinkedHashMap<K, Reference<V>>());

	public V get(K key) {
		V result = null;
		Reference<V> reference = cacheMap.get(key);
		if (reference != null) {
			result = reference.get();
		}
		return result;
	}

	public boolean put(K key, V value) {
		cacheMap.put(key, createReference(value));
		return true;
	}

	public boolean exists(K key) {
		return this.get(key) != null;
	}

	public void remove(K key) {
		cacheMap.remove(key);
	}

	public void removeFirst() {

		if (cacheMap.size() == 0)
			return;

		K fkey = cacheMap.keySet().iterator().next();
		this.remove(fkey);
	}

	public void removeLast() {

		if (cacheMap.size() == 0)
			return;

		K lkey = null;

		for (K key : cacheMap.keySet()) {
			lkey = key;
		}

		this.remove(lkey);
	}

	public int size() {
		return cacheMap.size();
	}

	public Collection<K> keys() {
		return cacheMap.keySet();
	}

	public void clear() {
		cacheMap.clear();
	}

	protected Reference<V> createReference(V value) {
		return new WeakReference<V>(value);
	}
}
