package net.energy.cache.ehcache;

import net.energy.cache.CacheErrorHandler;
import net.energy.cache.MultiLevelCache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * ehcache实现的cache
 * 
 * @author wuqh
 * @see Cache
 */
public class Ehcache extends MultiLevelCache implements net.energy.cache.Cache {
	private Cache cache;

	@Override
	protected void doAdd(final String key, final Object value) {
		Element element = new Element(key, value);
		element.setVersion(element.getCreationTime());
		try {
			cache.put(element);
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
		}

	}

	@Override
	protected void doAdd(final String key, final Object value, final long expiry) {
		int timeToLiveSeconds = (int) (expiry / 1000);
		Element element = new Element(key, value);
		element.setEternal(false);
		element.setTimeToLive(timeToLiveSeconds);
		element.setVersion(element.getCreationTime());
		try {
			cache.put(element);
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
		}
	}

	@Override
	protected boolean doDelete(final String key) {
		try {
			return cache.remove(key);
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
			return false;
		}
	}

	@Override
	protected void doRemoveAll() {
		try {
			cache.removeAll();
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
		}
	}

	@Override
	protected Object doGet(final String key) {
		Element element = null;
		try {
			element = cache.get(key);
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
		}
		if (element != null) {
			return element.getObjectValue();
		} else {
			return null;
		}
	}

	@Override
	protected boolean doKeyExists(final String key) {
		try {
			return cache.isKeyInCache(key);
		} catch (Throwable e) {
			CacheErrorHandler.handleError(e);
			return false;
		}
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
