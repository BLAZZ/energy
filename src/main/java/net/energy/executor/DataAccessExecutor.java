package net.energy.executor;

/**
 * 数据访问执行器，用于执行数据访问操作。 DataAccessExecutor的实现类一般采用链式调用的方式,如：
 * CacheDataAccess->JDBCDataAccess，CacheDataAccess->MongoDataAccess
 * 
 * @author wuqh
 * 
 */
public interface DataAccessExecutor {
	/**
	 * 执行方法
	 * 
	 * @param obj 方法的对象
	 * @param args 方法的参数
	 * @return 方法的调用结果
	 */
	Object execute(Object obj, Object[] args);
}
