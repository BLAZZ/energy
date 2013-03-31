package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.exception.DaoGenerateException;
import net.energy.mongo.BeanMapper;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoQuery;
import net.energy.mongo.definition.MongoFindDefinition;
import net.energy.utils.CommonUtils;
import net.energy.utils.Page;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DBObject;

/**
 * Mongo查询操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoFindExecutor extends AbstractMongoExecutor {
	private static final Log LOGGER = LogFactory.getLog(MongoFindExecutor.class);
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
		Object[] paramArray = CommonUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);

		String collectionName = definition.getCollectionName(args);

		MongoQuery query = new MongoQuery(actualShell, paramArray);
		query.setBatchSize(batchSize);
		DBObject sort = definition.getSortObject();
		query.setSort(sort);

		// 判断是否为分页查询，如果是则进行分页查询
		int pageIndex = definition.getPageIndex();
		Page page = CommonUtils.getPageArgument(args, pageIndex);
		if (page != null) {
			return dataAccessor.findPage(collectionName, query, beanMapper, page);
		}

		LOGGER.info("Query Shell With Token:" + actualShell);

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
				LOGGER.debug("result size > 1, fetch the 1st result");
			}
			return result.get(0);
		} else {
			return result;
		}
	}

}
