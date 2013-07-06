package net.energy.mongo.impl;

import java.util.List;

import net.energy.mongo.BeanMapper;
import net.energy.mongo.MongoDataAccessor;
import net.energy.queue.Task;
import net.energy.queue.TaskParameters;
import net.energy.queue.TaskRepository;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * MongoDataAccessor的高并发实现。采用队列方式，多线程共享数据，以牺牲一定数据一致性为代价，获得较高的并发处理性能。
 * 
 * @author wuqh
 * 
 */
public class QueuedMongoDataAccessor extends SimpleMongoDataAccessor implements MongoDataAccessor {
	private final SimpleMongoDataAccessor dataAccessor = new SimpleMongoDataAccessor();
	
	private long timeout = 0L;

	@Override
	protected <T> List<T> findInternal(DBCollection collection, DBObject queryDbObject, BeanMapper<T> mapper, int skip,
			int limit, DBObject sort, int batchSize) {

		TaskParameters parameters = new MongoFindTaskParameters(collection, queryDbObject, mapper, skip, limit, sort,
				batchSize);

		Task task = new MongoFindTask(dataAccessor);

		TaskRepository.addTask(task, parameters);

		List<T> result = TaskRepository.processTask(task, parameters, timeout);

		return result;

	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
