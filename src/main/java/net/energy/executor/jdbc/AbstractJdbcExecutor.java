package net.energy.executor.jdbc;

import java.lang.reflect.Method;

import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.jdbc.JdbcDataAccessor;

/**
 * JDBC相关调用方法基类
 * 
 * @author wuqh
 *
 */
public abstract class AbstractJdbcExecutor implements DataAccessExecutor {
	protected final JdbcDataAccessor dataAccessor;
	protected final Method method;

	public AbstractJdbcExecutor(JdbcDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		this.dataAccessor = dataAccessor;
		this.method = method;
		initDefinition(method);
	}
	
	/**
	 * 初始化配置信息
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	protected abstract void initDefinition(Method method) throws DaoGenerateException ;

}
