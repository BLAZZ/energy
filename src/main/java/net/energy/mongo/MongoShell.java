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
	private String shellWithToken;
	private Object[] shellParameters;

	public MongoShell() {
		super();
	}

	public MongoShell(String shell, Object[] args) {
		this.shellWithToken = shell;
		this.shellParameters = args;
	}

	public String getShellWithToken() {
		return shellWithToken;
	}

	public void setShellWithToken(String shellWithToken) {
		this.shellWithToken = shellWithToken;
	}

	public Object[] getShellParameters() {
		return shellParameters;
	}

	public void setShellParameters(Object[] shellParameters) {
		this.shellParameters = shellParameters;
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
