package net.energy.mongo;

import java.net.UnknownHostException;

import net.energy.exception.DataAccessException;
import net.energy.utils.MongoDbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * 
 * 用于获取MongoDB连接的工厂类
 * 
 * @author wuqh
 *
 */
public class MongoDbFactory {
	private static final Log LOGGER = LogFactory.getLog(MongoDbFactory.class);
	private String host;
	private Integer port;
	private String username;
	private String password;
	private String dbName;
	private Mongo mongo;
	private boolean initialized = false;
	
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	private void initMongo() {
		if(initialized) {
			if(mongo == null) {//init mongo errror
				throw new DataAccessException("Cannot init mongo");
			}
		} else {
			try {
				mongo = new Mongo(host, port);
			} catch (UnknownHostException e) {
				LOGGER.error("Cannot init mongo", e);
			} catch (MongoException e) {
				LOGGER.error("Cannot init mongo", e);
			} finally {
				initialized = true;
			}
		}
	}
	
	/**
	 * 获取一个{@link DB}连接
	 * 
	 * @return
	 */
	public DB getDB() {
		initMongo();
		
		DB db = MongoDbUtils.getDB(mongo, dbName, username, password == null ? null : password.toCharArray());

		return db;
	} 
}
