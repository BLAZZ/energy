package net.energy.utils;

import net.energy.exception.DataAccessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbUtils.class);

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
		Assert.notNull(mongo, "必须指定Mongo实例");

		LOGGER.trace("开始连接MongoDB[" + databaseName + "]");
		DB db = mongo.getDB(databaseName);

		boolean credentialsGiven = username != null && password != null;
		if (credentialsGiven && !db.isAuthenticated()) {
			// Note, can only authenticate once against the same com.mongodb.DB
			// object.
			if (!db.authenticate(username, password)) {
				throw new DataAccessException("认证失败！认证DB[" + databaseName + "],用户名["
						+ username + "],密码[" + new String(password) + "]");
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
			LOGGER.trace("正在关闭MongoDB[" + db.getName() + "]");
			try {
				db.requestDone();
			} catch (Throwable ex) {
				LOGGER.debug("关闭MongoDB时发生未知异常", ex);
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
