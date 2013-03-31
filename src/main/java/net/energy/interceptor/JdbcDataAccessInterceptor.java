package net.energy.interceptor;

import java.lang.reflect.Method;

import net.energy.cache.CacheManager;
import net.energy.executor.DataAccessExecutor;
import net.energy.executor.ExecutorFactory;
import net.energy.jdbc.JdbcDataAccessor;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * JDBC数据访问操作的拦截器
 * 
 * @author wuqh
 * @see MethodInterceptor
 */
public class JdbcDataAccessInterceptor implements MethodInterceptor {
	private final CacheManager cacheManager;
	private final JdbcDataAccessor dataAccessor;

	public JdbcDataAccessInterceptor(CacheManager cacheManager, JdbcDataAccessor dataAccessor) {
		this.cacheManager = cacheManager;
		this.dataAccessor = dataAccessor;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		DataAccessExecutor executor = ExecutorFactory.getJdbcExecutor(cacheManager, dataAccessor, method);
		if (executor != null) {
			return executor.execute(obj, args);
		}
		return null;
	}

}
