package net.energy.mongo.impl;

import java.util.ArrayList;
import java.util.List;

import net.energy.mongo.BeanMapper;
import net.energy.utils.Assert;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Mongo的查询操作实现类
 * 
 * @author wuqh
 *
 */
public class FindCallBack<T> implements CollectionCallback<List<T>> {
	private final DBObject queryDbObject;
	private final BeanMapper<T> mapper;
	private final int batchSize;
	private int skip = -1;
	private int limit = -1;
	private DBObject sort = null;
	
	/**
	 * 初始化Mongo的查询操所需要的参数
	 * 
	 * @param queryDbObject 执行查询所需要的DBObject
	 * @param batchSize
	 * @param mapper ORM使用的映射接口实现类
	 */
	public FindCallBack(DBObject queryDbObject, int batchSize, BeanMapper<T> mapper) {
		Assert.notNull(queryDbObject, "参数queryDbObject不能为空");
		Assert.notNull(mapper, "参数mapper不能为空");
		this.queryDbObject = queryDbObject;
		this.batchSize = batchSize;
		this.mapper = mapper;
	}
	
	@Override
	public List<T> doInCollection(DBCollection collection) {
		DBCursor cursor = collection.find(queryDbObject);
		
		if (skip != -1) {
			cursor.skip(skip);
		}
		if (limit != -1) {
			cursor.limit(limit);
		}
		if (sort != null) {
			cursor.sort(sort);
		}
		cursor.batchSize(batchSize);

		return mapperDBObject(cursor, mapper);
	}
	
	private List<T> mapperDBObject(DBCursor cursor, BeanMapper<T> mapper) {
		List<T> list = new ArrayList<T>();
		while (cursor.hasNext()) {
			list.add(mapper.mapper(cursor.next()));
		}
		return list;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setSort(DBObject sort) {
		this.sort = sort;
	}
	
	
}
