package net.energy.cache.memcached;

import java.util.Date;

import net.energy.cache.Cache;
import net.energy.cache.CacheErrorHandler;
import net.energy.cache.MultiLevelCache;

import com.danga.MemCached.MemCachedClient;

/**
 * memcached实现的cache
 * 
 * @author wuqh
 * @see MemCachedClient
 */
public class MemcachedCache extends MultiLevelCache implements Cache {
	private final MemCachedClient client;
	
	public MemcachedCache(MemCachedClient client) {
		this.client = client;
	}
	
	@Override
	protected void doAdd(final String key, final Object value) {
		add(key, value, 0L);
	}

	@Override
	protected void doAdd(final String key, final Object value, final long expiry) {
		if(value == null) { // can't set a null value to memcached
			return;
		}
		Date expireDate = new Date(expiry);;
		boolean ok = client.set(key, value, expireDate);
		if(!ok) {
			CacheErrorHandler.handleError(new Exception("add Object to memcache failed"));
		}
	}

	@Override
	protected boolean doDelete(final String key) {
		boolean ok = client.delete(key);
		if(!ok) {
			CacheErrorHandler.handleError(new Exception("delete Object in memcache failed"));
		}
		return ok;
	}

	@Override
	protected void doRemoveAll() {
		boolean ok = client.flushAll();
		if(!ok) {
			CacheErrorHandler.handleError(new Exception("flushAll Object in memcache failed"));
		}
	}

	@Override
	protected Object doGet(final String key) {
		return client.get(key);
	}

	@Override
	protected boolean doKeyExists(final String key) {
		return client.keyExists(key);
	}

}
