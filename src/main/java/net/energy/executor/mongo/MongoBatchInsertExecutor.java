package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.mongo.MongoBatchInsertDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mongo批量操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoBatchInsertExecutor extends AbstractMongoExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoBatchInsertExecutor.class);
	private MongoBatchInsertDefinition definition;
	
	public MongoBatchInsertExecutor(MongoDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new MongoBatchInsertDefinition(method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取批量插入用到的Shell和调用参数
		String actualShell = definition.getShellWithToken();
		
		List<Object[]> paramArrays = ReflectionUtils.generateBatchQueryArguments(args, definition);
		
		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo批量插入操作Shell(带Token)[" + actualShell + "]");
		
		boolean result = dataAccessor.batchInsert(collectionName, actualShell, paramArrays);
			
		return result;
	}

}
