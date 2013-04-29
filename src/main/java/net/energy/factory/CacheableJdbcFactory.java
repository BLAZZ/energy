package net.energy.factory;

import java.lang.reflect.Method;

import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.executor.ExecutorFactory;
import net.energy.jdbc.JdbcDataAccessor;

/**
 * 用于创建带缓存的JDBC操作实例工厂类
 * 
 * @author wuqh
 * @see JdbcDataAccessor
 */
public class CacheableJdbcFactory extends AbstractCacheableFactory {
	private JdbcDataAccessor dataAccessor;

	public void setDataAccessor(JdbcDataAccessor dataAccessor) {
		this.dataAccessor = dataAccessor;
	}

	@Override
	protected DataAccessExecutor createDataAccessExecutor(Method method) throws DaoGenerateException {
		DataAccessExecutor executor = ExecutorFactory.createJdbcExecutor(getCacheManager(), dataAccessor, method);
		return executor;
	}
}
