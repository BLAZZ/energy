package net.energy.factory;

import net.energy.cache.CacheManager;

/**
 * 有此工厂子类获取的DAO实例都带有缓存
 * 
 * @author wuqh
 * @see CacheManager
 */
public abstract class AbstractCacheableFactory extends AbstractDaoFactory {
	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}
}
