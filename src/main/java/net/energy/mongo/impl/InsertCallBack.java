package net.energy.mongo.impl;

import net.energy.mongo.MongoShell;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Mongo的插入操作实现类
 * 
 * @author wuqh
 *
 */
public class InsertCallBack extends AbstractCollectionCallBack<Boolean> implements CollectionCallback<Boolean> {
	
	
	public InsertCallBack(MongoShell shell, WriteConcern concern) {
		super(shell, concern);
		
	}
	
	@Override
	public Boolean doInCollectionWithConcern(DBCollection collection, WriteConcern concern) {
		WriteResult result = collection.insert(getShell().toDbObject(), concern);
		return (result.getError() == null);
	}

}
