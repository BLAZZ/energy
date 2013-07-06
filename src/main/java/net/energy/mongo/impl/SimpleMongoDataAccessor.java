package net.energy.mongo.impl;

import java.util.ArrayList;
import java.util.List;

import net.energy.mongo.BeanMapper;
import net.energy.mongo.MongoDataAccessor;
import net.energy.mongo.MongoDbFactory;
import net.energy.mongo.MongoQuery;
import net.energy.mongo.MongoShell;
import net.energy.mongo.MongoUpdate;
import net.energy.utils.MongoDbUtils;
import net.energy.utils.Page;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
 * MongoDataAccessor的默认实现
 * 
 * @author wuqh
 * 
 */
public class SimpleMongoDataAccessor implements MongoDataAccessor {
	private MongoDbFactory mongoDbFactory;
	private WriteConcern concern;

	public void setMongoDbFactory(MongoDbFactory mongoDbFactory) {
		this.mongoDbFactory = mongoDbFactory;
	}

	public void setConcern(WriteConcern concern) {
		this.concern = concern;
	}

	private DBCollection getCollection(String collectionName) {
		DB db = mongoDbFactory.getDB();
		DBCollection dbCollection = db.getCollection(collectionName);
		return dbCollection;
	}

	@Override
	public boolean batchInsert(String collectionName, String shell, List<Object[]> argsList) {
		final List<DBObject> dbObjects = new ArrayList<DBObject>(argsList.size());
		for (Object[] args : argsList) {
			dbObjects.add(QueryBuilder.toDBObject(shell, args));
		}

		return execute(collectionName, new CollectionCallback<Boolean>() {
			@Override
			public Boolean doInCollection(DBCollection collection) {
				WriteResult result = collection.insert(dbObjects, MongoDbUtils.getWriteConcern(collection, concern));

				return (result.getError() == null);
			}
		});
	}

	@Override
	public boolean insert(String collectionName, MongoShell shell) {
		return execute(collectionName, new InsertCallBack(shell, concern));
	}

	@Override
	public boolean remove(String collectionName, MongoShell shell) {
		return execute(collectionName, new RemoveCallBack(shell, concern));
	}

	@Override
	public boolean update(String collectionName, MongoShell query, MongoUpdate update) {
		return execute(collectionName, new UpdateCallBack(query, update, concern));
	}

	@Override
	public long count(String collectionName, MongoShell shell) {
		DBCollection collection = getCollection(collectionName);

		DBObject queryDbObject = shell.toDbObject();

		return countInternal(collection, queryDbObject);
	}

	@Override
	public <T> List<T> findPage(String collectionName, MongoQuery query, BeanMapper<T> mapper, Page page) {
		DBCollection collection = getCollection(collectionName);

		DBObject queryDbObject = query.toDbObject();
		long count = countInternal(collection, queryDbObject);

		page.setTotal(new Long(count).intValue());

		int skip = page.getStartIndex();
		int limit = page.getSize();

		return findInternal(collection, queryDbObject, mapper, skip, limit, query.getSort(), query.getBatchSize());
	}

	@Override
	public <T> List<T> find(String collectionName, MongoQuery query, BeanMapper<T> mapper) {
		DBCollection collection = getCollection(collectionName);

		DBObject queryDbObject = query.toDbObject();

		return findInternal(collection, queryDbObject, mapper, query.getSkip(), query.getLimit(), query.getSort(),
				query.getBatchSize());
	}

	/**
	 * 执行Mongo的操作
	 * 
	 * @param <T>
	 * @param collectionName
	 * @param action
	 * @return
	 */
	private <T> T execute(String collectionName, CollectionCallback<T> action) {
		DBCollection collection = getCollection(collectionName);

		return action.doInCollection(collection);
	}

	/**
	 * Mongo统计的内部实现入口
	 * 
	 * @param collection
	 * @param queryDbObject
	 * @return
	 */
	private long countInternal(DBCollection collection, DBObject queryDbObject) {
		return collection.count(queryDbObject);
	}

	/**
	 * Mongo查询的内部实现入口。
	 * 
	 * @param <T>
	 * @param collection
	 * @param queryDbObject
	 * @param mapper
	 * @param skip
	 * @param limit
	 * @param sort
	 * @param batchSize
	 * @return
	 */
	<T> List<T> findInternal(DBCollection collection, DBObject queryDbObject, BeanMapper<T> mapper, int skip,
			int limit, DBObject sort, int batchSize) {
		FindCallBack<T> findCallBack = new FindCallBack<T>(queryDbObject, batchSize, mapper);

		findCallBack.setSkip(skip);
		findCallBack.setLimit(limit);
		findCallBack.setSort(sort);

		return findCallBack.doInCollection(collection);

	}
}
