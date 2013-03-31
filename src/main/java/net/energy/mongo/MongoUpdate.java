package net.energy.mongo;


/**
 * 用于存放Mongo查询所需要用到的参数：查询的shell、对应的参数、不存在数据时是否插入、是否更新所有记录
 * 
 * @author wuqh
 * 
 */
public class MongoUpdate extends MongoShell {
	private boolean upsert;
	private boolean multi;

	public MongoUpdate(String shell, Object[] args) {
		super(shell, args);
	}
	
	public boolean isUpsert() {
		return upsert;
	}

	public void setUpsert(boolean upsert) {
		this.upsert = upsert;
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

}
