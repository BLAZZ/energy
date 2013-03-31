package net.energy.factory;

import net.energy.interceptor.JdbcDataAccessInterceptor;
import net.energy.jdbc.JdbcDataAccessor;

/**
 * 有此工厂子类获取的DAO实例都为JDBC操作类，且带有缓存
 * 
 * @author wuqh
 * @see JdbcDataAccessor
 */
public class CacheableJdbcFactory extends AbstractCacheableFactory {
	private JdbcDataAccessor dataAccessor;

	public synchronized JdbcDataAccessInterceptor getDataAccessInterceptor() {
		JdbcDataAccessInterceptor interceptor = new JdbcDataAccessInterceptor(getCacheManager(), dataAccessor);
		return interceptor;
	}

	public void setDataAccessor(JdbcDataAccessor dataAccessor) {
		this.dataAccessor = dataAccessor;
	}
}
