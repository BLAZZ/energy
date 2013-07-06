package net.energy.executor.mongo;

import java.lang.reflect.Method;

import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.mongo.MongoDataAccessor;

/**
 * Mongo操作调用方法抽象类
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractMongoExecutor implements DataAccessExecutor {
	final MongoDataAccessor dataAccessor;

	AbstractMongoExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		this.dataAccessor = dataAccessor;
		initDefinition(method);
	}

	/**
	 * 初始化操作配置信息
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	protected abstract void initDefinition(Method method) throws DaoGenerateException;

}
