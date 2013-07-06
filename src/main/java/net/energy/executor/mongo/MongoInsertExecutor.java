package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoInsertDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo插入操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoInsertExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoInsertExecutor.class);
	private MongoInsertDefinition definition;

	public MongoInsertExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoInsertDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取Mongo插入文档的shell和参数
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();

		String actualShell = definition.getShellWithToken();
		List<String> parameterNames = definition.getParsedShell().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchValues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo插入操作Shell(带Token)[" + actualShell + "]");

		MongoShell shell = new MongoShell(actualShell, paramArray);
		boolean result = dataAccessor.insert(collectionName, shell);

		return result;
	}

}
