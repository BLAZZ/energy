package net.energy.mongo.impl;

import com.mongodb.DBCollection;

/**
 * 生成DBCollection后的回调接口，用于执行Mongo的shell操作
 * 
 * @author wuqh
 *
 * @param <T>
 */
public interface CollectionCallback<T> {
	/**
	 *  生成DBCollection后的回调方法
	 * 
	 * @param collection
	 * @return
	 */
	T doInCollection(DBCollection collection);
}
