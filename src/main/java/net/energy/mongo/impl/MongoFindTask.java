package net.energy.mongo.impl;

import java.util.List;

import net.energy.mongo.BeanMapper;
import net.energy.queue.Task;
import net.energy.queue.TaskParameters;
import net.energy.queue.TaskResult;
import net.energy.utils.Assert;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Mongo查询任务
 * 
 * @author wuqh
 * @see Task
 */
public class MongoFindTask extends Task {
	private final SimpleMongoDataAccessor dataAccessor;
	
	public MongoFindTask(SimpleMongoDataAccessor dataAccessor) {
		Assert.isNull(dataAccessor, "DataAccessorObject can't be null");
		this.dataAccessor = dataAccessor;
	}
	
	@SuppressWarnings("unchecked")
	public <T> TaskResult<T> process(TaskParameters parameters) {
		MongoFindTaskParameters taskParameters = (MongoFindTaskParameters) parameters;
		
		DBCollection collection = taskParameters.getCollection();
		DBObject queryDbObject = taskParameters.getQueryDbObject();
		BeanMapper<?> mapper = (BeanMapper<?>) taskParameters.getMapper();
		int skip = taskParameters.getSkip();
		int limit = taskParameters.getLimit();
		DBObject sort = taskParameters.getSort();
		int batchSize = taskParameters.getBatchSize();
		
		List<?> result = dataAccessor.findInternal(collection, queryDbObject, mapper, skip, limit, sort, batchSize);
		
		TaskResult<T> taskResult = new TaskResult<T>();
		taskResult.setResult((T) result);
		taskResult.setDone(true);
		
		return taskResult;
	}
	
}
