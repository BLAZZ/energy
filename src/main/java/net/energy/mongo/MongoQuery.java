package net.energy.mongo;

import com.mongodb.DBObject;

/**
 * 用于存放Mongo查询所需要用到的参数：查询的shell、对应的参数、排序、查询偏移、查询数量等信息
 * 
 * @author wuqh
 *
 */
public class MongoQuery extends MongoShell {
	private int skip;
	private int limit;
	private DBObject sort;
	private int batchSize;

	public MongoQuery(String shell, Object[] args) {
		super(shell, args);
	}
	
	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public DBObject getSort() {
		return sort;
	}

	public void setSort(DBObject sort) {
		this.sort = sort;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
