package net.energy.factory;

import java.lang.reflect.Method;

import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.executor.ExecutorFactory;
import net.energy.mongo.MongoDataAccessor;

/**
 * 用于创建带缓存的MongoDB操作实例工厂类
 * 
 * @author wuqh
 * @see MongoDataAccessor
 */
public class CacheableMongoFactory extends AbstractCacheableFactory {
	private MongoDataAccessor dataAccessor;

	public void setDataAccessor(MongoDataAccessor dataAccessor) {
		this.dataAccessor = dataAccessor;
	}

	@Override
	protected DataAccessExecutor createDataAccessExecutor(Method method) throws DaoGenerateException {
		DataAccessExecutor executor = ExecutorFactory.createMongoExecutor(getCacheManager(), dataAccessor, method);
		return executor;
	}
}
