package net.energy.factory;

import net.energy.interceptor.MongoDataAccessInterceptor;
import net.energy.mongo.MongoDataAccessor;

/**
 * 有此工厂子类获取的DAO实例都为MONGODB操作类，且带有缓存
 * 
 * @author wuqh
 * @see MongoDataAccessor
 */
public class CacheableMongoFactory extends AbstractCacheableFactory {
	private MongoDataAccessor dataAccessor;

	public synchronized MongoDataAccessInterceptor getDataAccessInterceptor() {
		MongoDataAccessInterceptor interceptor = new MongoDataAccessInterceptor(getCacheManager(), dataAccessor);
		return interceptor;
	}

	public void setDataAccessor(MongoDataAccessor dataAccessor) {
		this.dataAccessor = dataAccessor;
	}
}
