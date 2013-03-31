package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.mongo.definition.MongoRemoveDefinition;
import net.energy.utils.CommonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mongo删除操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoRemoveExecutor extends AbstractMongoExecutor {
	private static final Log LOGGER = LogFactory.getLog(MongoRemoveExecutor.class);
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
		Object[] paramArray = CommonUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo Remove Shell With Token:" + actualShell);

		// 构造删除操作的数据结构
		MongoShell shell = new MongoShell(actualShell, paramArray);
		boolean result = dataAccessor.remove(collectionName, shell);

		return result;
	}

}
