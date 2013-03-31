package net.energy.mongo.impl;

import net.energy.mongo.MongoShell;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Mongo的删除操作实现类
 * 
 * @author wuqh
 *
 */
public class RemoveCallBack extends AbstractCollectionCallBack<Boolean> implements CollectionCallback<Boolean> {
	public RemoveCallBack(MongoShell shell, WriteConcern concern) {
		super(shell, concern);
		
	}
	
	@Override
	public Boolean doInCollectionWithConcern(DBCollection collection, WriteConcern concern) {
		WriteResult result = collection.remove(getShell().toDbObject(), concern);
		return (result.getError() == null);
	}

}
