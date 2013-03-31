package net.energy.interceptor;

import java.lang.reflect.Method;

import net.energy.cache.CacheManager;
import net.energy.executor.DataAccessExecutor;
import net.energy.executor.ExecutorFactory;
import net.energy.mongo.MongoDataAccessor;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * MongoDB数据访问操作的拦截器
 * 
 * @author wuqh
 * @see MethodInterceptor
 */
public class MongoDataAccessInterceptor implements MethodInterceptor {
	private final CacheManager cacheManager;
	private final MongoDataAccessor dataAccessor;

	public MongoDataAccessInterceptor(CacheManager cacheManager, MongoDataAccessor dataAccessor) {
		this.cacheManager = cacheManager;
		this.dataAccessor = dataAccessor;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		DataAccessExecutor executor = ExecutorFactory.getMongoExecutor(cacheManager, dataAccessor, method);
		if (executor != null) {
			return executor.execute(obj, args);
		}
		return null;
	}

}
