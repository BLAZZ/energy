package net.energy.utils;

import net.energy.exception.DataAccessException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

/**
 * MongoDB操作工具类
 * 
 * @author wuqh
 * 
 */
public class MongoDbUtils {
	private static final Log LOGGER = LogFactory.getLog(MongoDbUtils.class);

	/**
	 * 从{@link Mongo}中获取一个{@link DB}连接
	 * 
	 * @param mongo
	 * @param databaseName
	 * @return
	 */
	public static DB getDB(Mongo mongo, String databaseName) {
		return getDB(mongo, databaseName, null, null);
	}

	/**
	 * 从{@link Mongo}中获取一个{@link DB}连接
	 * 
	 * @param mongo
	 * @param databaseName
	 * @param username
	 * @param password
	 * @return
	 */
	public static DB getDB(Mongo mongo, String databaseName, String username, char[] password) {
		Assert.notNull(mongo, "No Mongo instance specified");

		LOGGER.trace("Getting Mongo Database name=[" + databaseName + "]");
		DB db = mongo.getDB(databaseName);

		boolean credentialsGiven = username != null && password != null;
		if (credentialsGiven && !db.isAuthenticated()) {
			// Note, can only authenticate once against the same com.mongodb.DB
			// object.
			if (!db.authenticate(username, password)) {
				throw new DataAccessException("Failed to authenticate to database [" + databaseName + "], username = ["
						+ username + "], password = [" + new String(password) + "]");
			}
		}

		return db;
	}

	/**
	 * 关闭一个{@link DB}连接
	 * 
	 * @param db
	 */
	public static void closeDB(DB db) {
		if (db != null) {
			LOGGER.debug("Closing Mongo DB object");
			try {
				db.requestDone();
			} catch (Throwable ex) {
				LOGGER.debug("Unexpected exception on closing Mongo DB object", ex);
			}
		}
	}

	/**
	 * 获取实际的WriteConcern，如果传入WriteConcern为空，则从DBCollection中获取WriteConcern
	 * 
	 * @param collection
	 * @param concern
	 * @return
	 */
	public static WriteConcern getWriteConcern(DBCollection collection, WriteConcern concern) {
		return concern == null ? collection.getWriteConcern() : concern;
	}
}
