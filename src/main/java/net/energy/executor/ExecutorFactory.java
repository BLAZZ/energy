package net.energy.executor;

import java.lang.reflect.Method;

import net.energy.annotation.jdbc.BatchUpdate;
import net.energy.annotation.jdbc.Query;
import net.energy.annotation.jdbc.Update;
import net.energy.annotation.mongo.MongoBatchInsert;
import net.energy.annotation.mongo.MongoCount;
import net.energy.annotation.mongo.MongoFind;
import net.energy.annotation.mongo.MongoInsert;
import net.energy.annotation.mongo.MongoRemove;
import net.energy.annotation.mongo.MongoUpdate;
import net.energy.cache.CacheManager;
import net.energy.exception.DaoGenerateException;
import net.energy.executor.cache.CacheExecutor;
import net.energy.executor.jdbc.AbstractJdbcExecutor;
import net.energy.executor.jdbc.JdbcBatchUpdateExecutor;
import net.energy.executor.jdbc.JdbcQueryExecutor;
import net.energy.executor.jdbc.JdbcUpdateExecutor;
import net.energy.executor.mongo.AbstractMongoExecutor;
import net.energy.executor.mongo.MongoBatchInsertExecutor;
import net.energy.executor.mongo.MongoCountExecutor;
import net.energy.executor.mongo.MongoFindExecutor;
import net.energy.executor.mongo.MongoInsertExecutor;
import net.energy.executor.mongo.MongoRemoveExecutor;
import net.energy.executor.mongo.MongoUpdateExecutor;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.mongo.MongoDataAccessor;

/**
 * 创建DataAccessExecutor的工厂类，
 * 用于创建JDBC的DataAccessExecutor和MongoDB的DataAccessExecutor
 * 
 * @author wuqh
 * @see DataAccessExecutor
 */
public final class ExecutorFactory {
	private ExecutorFactory() {
	}

	/**
	 * 创建JDBC的DataAccessExecutor
	 * 
	 * @param cacheManager
	 * @param dataAccessor
	 * @param method
	 * @return
	 * @throws DaoGenerateException
	 */
	public static DataAccessExecutor createJdbcExecutor(CacheManager cacheManager, JdbcDataAccessor dataAccessor,
			Method method) throws DaoGenerateException {
		DataAccessExecutor executor = null;

		CacheExecutor cacheExecutor = createCacheExecutor(cacheManager, method);
		AbstractJdbcExecutor jdbcExecutor = createJdbcExecutor(dataAccessor, method);

		if (jdbcExecutor == null) {
			cacheExecutor.setDataAccessExecutor(new MethodExecutor(method));
		} else {
			cacheExecutor.setDataAccessExecutor(jdbcExecutor);
		}

		executor = cacheExecutor;

		return executor;

	}

	/**
	 * 创建MongoDB的DataAccessExecutor
	 * 
	 * @param cacheManager
	 * @param dataAccessor
	 * @param method
	 * @return
	 * @throws DaoGenerateException
	 */
	public static DataAccessExecutor createMongoExecutor(CacheManager cacheManager, MongoDataAccessor dataAccessor,
			Method method) throws DaoGenerateException {
		DataAccessExecutor executor = null;
		CacheExecutor cacheExecutor = createCacheExecutor(cacheManager, method);
		AbstractMongoExecutor mongoExecutor = createMongoExecutor(dataAccessor, method);

		if (mongoExecutor == null) {
			cacheExecutor.setDataAccessExecutor(new MethodExecutor(method));
		} else {
			cacheExecutor.setDataAccessExecutor(mongoExecutor);
		}

		executor = cacheExecutor;

		return executor;

	}

	private static AbstractMongoExecutor createMongoExecutor(MongoDataAccessor dataAccessor, Method method)
			throws DaoGenerateException {
		AbstractMongoExecutor executor = null;

		if (method.getAnnotation(MongoFind.class) != null) {
			executor = new MongoFindExecutor(dataAccessor, method);
		} else if (method.getAnnotation(MongoUpdate.class) != null) {
			executor = new MongoUpdateExecutor(dataAccessor, method);
		} else if (method.getAnnotation(MongoInsert.class) != null) {
			executor = new MongoInsertExecutor(dataAccessor, method);
		} else if (method.getAnnotation(MongoRemove.class) != null) {
			executor = new MongoRemoveExecutor(dataAccessor, method);
		} else if (method.getAnnotation(MongoCount.class) != null) {
			executor = new MongoCountExecutor(dataAccessor, method);
		} else if (method.getAnnotation(MongoBatchInsert.class) != null) {
			executor = new MongoBatchInsertExecutor(dataAccessor, method);
		}

		return executor;
	}

	private static CacheExecutor createCacheExecutor(CacheManager cacheManager, Method method)
			throws DaoGenerateException {
		CacheExecutor executor = new CacheExecutor(cacheManager, method);

		return executor;
	}

	private static AbstractJdbcExecutor createJdbcExecutor(JdbcDataAccessor dataAccessor, Method method)
			throws DaoGenerateException {
		AbstractJdbcExecutor executor = null;
		if (method.getAnnotation(Query.class) != null) {
			executor = new JdbcQueryExecutor(dataAccessor, method);
		} else if (method.getAnnotation(Update.class) != null) {
			executor = new JdbcUpdateExecutor(dataAccessor, method);
		} else if (method.getAnnotation(BatchUpdate.class) != null) {
			executor = new JdbcBatchUpdateExecutor(dataAccessor, method);
		}

		return executor;
	}

}
