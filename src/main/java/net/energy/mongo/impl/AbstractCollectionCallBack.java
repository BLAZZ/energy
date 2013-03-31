package net.energy.mongo.impl;

import net.energy.mongo.MongoShell;
import net.energy.utils.MongoDbUtils;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

/**
 * 生成DBCollection后的回调接口的抽象实现，封装了MongoShell、WriteConcern对象获取
 * 
 * @author wuqh
 * 
 * @param <T>
 */
public abstract class AbstractCollectionCallBack<T> implements CollectionCallback<T> {
	private final MongoShell shell;
	private final WriteConcern concern;

	public AbstractCollectionCallBack(MongoShell shell, WriteConcern concern) {
		this.shell = shell;
		this.concern = concern;
	}

	public T doInCollection(DBCollection collection) {
		WriteConcern concern = getWriteConcern(collection);
		return doInCollectionWithConcern(collection, concern);
	}

	/**
	 * 包含WriteConcern的DBCollection后的回调方法
	 * 
	 * @param collection
	 * @param concern
	 * @return
	 */
	public abstract T doInCollectionWithConcern(DBCollection collection, WriteConcern concern);

	private WriteConcern getWriteConcern(DBCollection collection) {
		return MongoDbUtils.getWriteConcern(collection, concern);
	}

	protected MongoShell getShell() {
		return shell;
	}
}
