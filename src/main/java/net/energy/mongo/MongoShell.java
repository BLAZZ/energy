package net.energy.mongo;

import com.mongodb.DBObject;
import net.energy.mongo.impl.QueryBuilder;

/**
 * Mongo查询用到的最基本结构，包含操作用的shell和对应参数，可以使用{@link QueryBuilder}绑定成查询用的
 * {@link DBObject}
 * 
 * @author wuqh
 * 
 */
public class MongoShell {
	private final String shellWithToken;
	private final Object[] shellParameters;

	public MongoShell(String shell, Object[] args) {
		this.shellWithToken = shell;
		this.shellParameters = args;
	}

	/**
	 * 生成使用{@link QueryBuilder}绑定成查询用的 {@link DBObject}
	 * 
	 * @return
	 */
	public DBObject toDbObject() {
		DBObject queryDbObject = QueryBuilder.toDBObject(shellWithToken, shellParameters);
		return queryDbObject;
	}
}
