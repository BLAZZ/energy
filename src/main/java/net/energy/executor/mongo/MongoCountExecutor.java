package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.mongo.definition.MongoCountDefinition;
import net.energy.utils.CommonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mongo统计操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoCountExecutor extends AbstractMongoExecutor {
	private static final Log LOGGER = LogFactory.getLog(MongoCountExecutor.class);
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
		Object[] paramArray = CommonUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);
		
		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo Count Shell With Token:" + actualShell);

		MongoShell shell = new MongoShell(actualShell, paramArray);
		long result = dataAccessor.count(collectionName, shell);
		
		return (new Long(result).intValue());
	}

}
