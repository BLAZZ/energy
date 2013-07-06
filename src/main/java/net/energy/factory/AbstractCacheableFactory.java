package net.energy.factory;

import net.energy.cache.CacheManager;

/**
 * 用于创建带有缓存的DAO实例的工厂类
 * 
 * @author wuqh
 * @see CacheManager
 */
public abstract class AbstractCacheableFactory extends AbstractDaoFactory {
	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	CacheManager getCacheManager() {
		return cacheManager;
	}
}
