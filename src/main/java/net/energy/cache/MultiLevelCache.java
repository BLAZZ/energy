package net.energy.cache;

import net.energy.exception.CacheUnreachableException;

public abstract class MultiLevelCache implements Cache {
	private Cache highLevelCache;
	private boolean highLevelCacheReachable = true;
	private long lastUnreachableTime = 0L;
	private long reconnectTime = 0L;

	public void setHighLevelCache(Cache highLevelCache) {
		this.highLevelCache = highLevelCache;
	}

	public void setReconnectTime(long reconnectTime) {
		this.reconnectTime = reconnectTime;
	}

	private boolean isHighLevelCacheEnable() {
		if (highLevelCache == null) {
			return false;
		}

		// 如果上一次连接不成功，则判断是否超过重连时间
		boolean reconnect = false;
		if (!highLevelCacheReachable) {
			long interval = System.currentTimeMillis() - lastUnreachableTime;
			if (interval > reconnectTime) {
				reconnect = true;
			}
		}

		return (highLevelCacheReachable || reconnect);
	}

	private void setHighLevelCacheDisable() {
		highLevelCacheReachable = false;
		lastUnreachableTime = System.currentTimeMillis();
	}

	private void setHighLevelCacheEnable() {
		highLevelCacheReachable = true;
	}

	@Override
	public void add(final String key, final Object value) {
		doAdd(key, value);

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				cache.add(key, value);
				return null;
			}
		};
		callback.process(highLevelCache);
	}

	protected abstract void doAdd(final String key, final Object value);

	@Override
	public void add(final String key, final Object value, final long expiry) {
		doAdd(key, value, expiry);

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				cache.add(key, value, expiry);
				return null;
			}
		};
		callback.process(highLevelCache);
	}

	protected abstract void doAdd(final String key, final Object value, final long expiry);

	@Override
	public boolean delete(final String key) {
		boolean result = doDelete(key);

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				return cache.delete(key);
			}
		};
		callback.process(highLevelCache);

		return result;
	}

	protected abstract boolean doDelete(final String key);

	@Override
	public void removeAll() {
		doRemoveAll();

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				cache.removeAll();
				return null;
			}
		};
		callback.process(highLevelCache);

	}

	protected abstract void doRemoveAll();

	@Override
	public Object get(final String key) {
		Object result = doGet(key);

		if (result != null) {
			return result;
		}

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				return cache.get(key);
			}
		};
		result = callback.process(highLevelCache);

		return result;
	}

	protected abstract Object doGet(final String key);

	@Override
	public boolean keyExists(final String key) {
		boolean exist = doKeyExists(key);

		if (exist) {
			return exist;
		}

		HighLevelCacheCallback callback = new HighLevelCacheCallback() {

			@Override
			protected Object doInCache(Cache cache) {
				return cache.keyExists(key);
			}
		};
		exist = (Boolean) callback.process(highLevelCache);

		return exist;
	}

	protected abstract boolean doKeyExists(final String key);

	public abstract class HighLevelCacheCallback {
		public Object process(Cache highLevelCache) {
			if (isHighLevelCacheEnable()) {
				try {
					Object result = doInCache(highLevelCache);
					setHighLevelCacheEnable();
					return result;
				} catch (CacheUnreachableException e) {
					setHighLevelCacheDisable();
				}
			}

			return null;
		}

		protected abstract Object doInCache(Cache cache);
	}
}
