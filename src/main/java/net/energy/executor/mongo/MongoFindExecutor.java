package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoFindDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.BeanMapper;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoQuery;
import net.energy.utils.Page;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;

/**
 * Mongo查询操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoFindExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoFindExecutor.class);
	private MongoFindDefinition definition;

	public MongoFindExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoFindDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取Mongo查询中需要用到的查询shell和参数
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();
		BeanMapper<?> beanMapper = definition.getBeanMapper();
		int batchSize = definition.getBatchSize();

		String actualShell = definition.getShellWithToken();
		List<String> parameterNames = definition.getParsedShell().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		MongoQuery query = new MongoQuery(actualShell, paramArray);
		query.setBatchSize(batchSize);
		DBObject sort = definition.getSortObject();
		query.setSort(sort);

		// 判断是否为分页查询，如果是则进行分页查询
		Page page = definition.getPageArgument(args);
		if (page != null) {
			return dataAccessor.findPage(collectionName, query, beanMapper, page);
		}

		LOGGER.info("Mongo查询操作Shell(带Token)[" + actualShell + "]");

		// 不是分页，进行普通查询
		Integer skip = definition.getSkip(args);
		if (skip == null) {
			skip = -1;
		}
		query.setSkip(skip);

		Integer limit = definition.getLimit(args);
		if (limit == null) {
			limit = -1;
		}
		query.setLimit(limit);

		List<?> result = dataAccessor.find(collectionName, query, beanMapper);

		if (definition.isUnique()) {
			if (result == null || result.isEmpty()) {
				return null;
			}
			if (result.size() > 1) {
				LOGGER.debug("返回记录数 >1,默认获取第一条记录");
			}
			return result.get(0);
		} else {
			return result;
		}
	}

}
