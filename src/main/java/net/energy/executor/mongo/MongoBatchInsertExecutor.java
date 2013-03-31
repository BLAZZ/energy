package net.energy.executor.mongo;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.exception.DaoGenerateException;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.definition.MongoBatchInsertDefinition;
import net.energy.utils.CommonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mongo批量操作调用方法
 * 
 * @author wuqh
 * 
 */
public class MongoBatchInsertExecutor extends AbstractMongoExecutor {
	private static final Log LOGGER = LogFactory.getLog(MongoBatchInsertExecutor.class);
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
		
		List<Object[]> paramArrays = CommonUtils.generateBatchQueryArguments(args, definition);
		
		String collectionName = definition.getCollectionName(args);

		LOGGER.info("Mongo Insert Shell With Token:" + actualShell);
		
		boolean result = dataAccessor.batchInsert(collectionName, actualShell, paramArrays);
			
		return result;
	}

}
