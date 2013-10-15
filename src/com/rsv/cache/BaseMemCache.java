package com.rsv.cache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class BaseMemCache<K, V>
{
	private final Map<K, Reference<V>> cacheMap = Collections
			.synchronizedMap(new HashMap<K, Reference<V>>());

	public V get(K key)
	{
		V result = null;
		Reference<V> reference = cacheMap.get(key);
		if (reference != null)
		{
			result = reference.get();
		}
		return result;
	}

	public boolean put(K key, V value)
	{
		cacheMap.put(key, createReference(value));
		return true;
	}

	public boolean exists(K key)
	{
		return this.get(key) != null;
	}

	public void remove(K key)
	{
		cacheMap.remove(key);
	}

	public Collection<K> keys()
	{
		return cacheMap.keySet();
	}

	public void clear()
	{
		cacheMap.clear();
	}

	protected Reference<V> createReference(V value)
	{
		return new WeakReference<V>(value);
	}
}
