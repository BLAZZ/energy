package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoRemoveDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo删除操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoRemoveExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoRemoveExecutor.class);
	private MongoRemoveDefinition definition;

	public MongoRemoveExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoRemoveDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取Mongo删除中需要用到的查询shell和参数
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();

		String actualShell = definition.getShellWithToken();
		List<String> parameterNames = definition.getParsedShell().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo删除操作Shell(带Token)[" + actualShell + "]");
		
		// 构造删除操作的数据结构
		MongoShell shell = new MongoShell(actualShell, paramArray);
		boolean result = dataAccessor.remove(collectionName, shell);

		return result;
	}

}
