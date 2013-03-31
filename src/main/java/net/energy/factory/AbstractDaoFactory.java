package net.energy.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * 通过CGLIB动态产生DAO接口实例
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractDaoFactory implements DaoFactory {
	private final int proxyCacheSize = 32;
	private Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<Class<?>, Object>(proxyCacheSize);

	@SuppressWarnings("unchecked")
	public <T> T createDao(Class<T> clazz) {
		T instance = (T) proxyCache.get(clazz);
		if (instance == null) {
			instance = (T) Enhancer.create(clazz, getDataAccessInterceptor());
			proxyCache.put(clazz, instance);
		}
		return instance;
	}

	public abstract MethodInterceptor getDataAccessInterceptor();
}
