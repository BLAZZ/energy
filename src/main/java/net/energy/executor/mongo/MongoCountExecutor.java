package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoCountDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo统计操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoCountExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoCountExecutor.class);
	private MongoCountDefinition definition;

	public MongoCountExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoCountDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取Mongo统计中需要用到的查询shell和参数
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();

		String actualShell = definition.getShellWithToken();
		List<String> parameterNames = definition.getParsedShell().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchValues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo统计操作Shell(带Token)[" + actualShell + "]");

		MongoShell shell = new MongoShell(actualShell, paramArray);
		long result = dataAccessor.count(collectionName, shell);

		return (new Long(result).intValue());
	}

}
