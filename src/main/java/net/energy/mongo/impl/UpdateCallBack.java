package net.energy.mongo.impl;

import net.energy.mongo.MongoShell;
import net.energy.mongo.MongoUpdate;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Mongo的更新操作实现类
 * 
 * @author wuqh
 *
 */
public class UpdateCallBack extends AbstractCollectionCallBack<Boolean> implements CollectionCallback<Boolean> {
	private final MongoUpdate update;
	
	public UpdateCallBack(MongoShell query, MongoUpdate update, WriteConcern concern) {
		super(query, concern);
		this.update = update;
	}
	
	@Override
	public Boolean doInCollectionWithConcern(DBCollection collection, WriteConcern concern) {
		DBObject queryDbObject = getShell().toDbObject();
		DBObject modifierDbObject = update.toDbObject();
		
		boolean upsert = update.isUpsert();
		boolean multi = update.isMulti();
		
		WriteResult result = collection.update(queryDbObject, modifierDbObject, upsert, multi, concern);

		return (result.getError() == null);
	}

}
