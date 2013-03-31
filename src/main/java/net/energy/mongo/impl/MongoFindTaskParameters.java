package net.energy.mongo.impl;

import net.energy.mongo.BeanMapper;
import net.energy.queue.TaskParameters;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Mongo查询任务参数集
 * 
 * @author wuqh
 * @see TaskParameters
 */
public class MongoFindTaskParameters implements TaskParameters {
	private final DBCollection collection;
	private final DBObject queryDbObject;
	private final BeanMapper<?> mapper;
	private final int skip;
	private final int limit;
	private final DBObject sort;
	private final int batchSize;
	private final int hashCode;

	public MongoFindTaskParameters(DBCollection collection, DBObject queryDbObject, BeanMapper<?> mapper, int skip,
			int limit, DBObject sort, int batchSize) {
		int hashCode = 0;

		this.collection = collection;
		hashCode += collection.hashCode();

		this.queryDbObject = queryDbObject;
		hashCode += queryDbObject.hashCode();

		this.mapper = mapper;

		this.skip = skip;
		hashCode += skip;

		this.limit = limit;
		hashCode += limit;

		this.sort = sort;
		hashCode += sort.hashCode();

		this.batchSize = batchSize;
		hashCode += batchSize;

		this.hashCode = 17 + 23 * hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof MongoFindTaskParameters == false) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		MongoFindTaskParameters parameters = (MongoFindTaskParameters) obj;

		boolean equals = (collection.equals(parameters.getCollection()) && skip == parameters.getSkip()
				&& limit == parameters.getLimit() && batchSize == parameters.getBatchSize());

		if (equals) {
			DBObject paramQueryDbObject = parameters.getQueryDbObject();
			DBObject paramSort = parameters.getSort();

			if ((paramQueryDbObject == null && queryDbObject == null)
					|| (queryDbObject != null && queryDbObject.equals(paramQueryDbObject))) {
				if ((paramSort == null && sort == null) || (sort != null && sort.equals(paramSort))) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	public DBCollection getCollection() {
		return collection;
	}

	public DBObject getQueryDbObject() {
		return queryDbObject;
	}

	public BeanMapper<?> getMapper() {
		return mapper;
	}

	public int getSkip() {
		return skip;
	}

	public int getLimit() {
		return limit;
	}

	public DBObject getSort() {
		return sort;
	}

	public int getBatchSize() {
		return batchSize;
	}

}
