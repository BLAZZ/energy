package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoUpdateDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoShell;
import net.energy.mongo.MongoUpdate;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo更新操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoUpdateExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoUpdateExecutor.class);
	private MongoUpdateDefinition definition;

	public MongoUpdateExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoUpdateDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取Mongo更新中需要用到的查询shell和参数
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();

		String actualShell = definition.getShellWithToken();
		List<String> parameterNames = definition.getParsedShell().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		boolean upsert = definition.isUpsert();
		boolean multi = definition.isMulti();

		// 获取Mongo更新中需要用到的更新shell和参数
		Method[] modifierGetterMethods = definition.getModifierGetterMethods();
		Integer[] modifierParameterIndexes = definition.getModifierParameterIndexes();
		String modifier = definition.getModifierShellWithToken();
		List<String> modifierParameterNames = definition.getParsedModifierShell().getParameterNames();
		Object[] modifierParamArray = ReflectionUtils.fetchVlaues(modifierGetterMethods, modifierParameterIndexes, args,
				modifierParameterNames);

		LOGGER.info("Mongo更新,查询部分Shell(带Token)[" + actualShell + "]");
		LOGGER.info("Mongo更新,更新部分Shell(带Token)[" + modifier + "]");

		// 构造查询、更新的数据结构
		MongoShell query = new MongoShell(actualShell, paramArray);
		MongoUpdate update = new MongoUpdate(modifier, modifierParamArray);
		update.setUpsert(upsert);
		update.setMulti(multi);

		// 执行更新操作
		boolean result = dataAccessor.update(collectionName, query, update);

		return result;
	}

}
