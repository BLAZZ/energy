package net.energy.cache;

/**
 * 缓存管理接口，通过不同实现来管理不同的缓存实例，如Ehcache，MemCached
 * 
 * @author wuqh
 * 
 */
public interface CacheManager {
	/**
	 * 根据缓存池的名称获取缓存操作客户端。 如果无法获取缓存客户端，将抛出CacheUnreachableException
	 * 
	 * @param pool
	 * @return
	 */
	Cache getCache(String pool);
}
