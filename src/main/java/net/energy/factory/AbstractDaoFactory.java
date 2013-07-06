package net.energy.factory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.utils.ReflectionUtils;
import net.sf.cglib.proxy.Enhancer;

/**
 * 通过CGLIB动态产生DAO接口实例
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractDaoFactory implements DaoFactory {
	private final static int PROXY_CACHE_SIZE = 32;
	private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<Class<?>, Object>(PROXY_CACHE_SIZE);

	@SuppressWarnings("unchecked")
	public <T> T createDao(Class<T> clazz) throws Exception {
		T instance = (T) proxyCache.get(clazz);
		if (instance == null) {
			DataAccessInterceptor interceptor = newDataAccessInterceptor();
			detectDataAccessMethod(clazz, interceptor);
			instance = (T) Enhancer.create(clazz, interceptor);
			proxyCache.put(clazz, instance);
		}
		return instance;
	}

	/**
	 * 创建DataAccessInterceptor实例
	 * 
	 * @return
	 */
	DataAccessInterceptor newDataAccessInterceptor() {
		return new DataAccessInterceptor();
	}

	/**
	 * 读取方法的配置，并生成相应的执行方法
	 * 
	 * @param method
	 * @return
	 * @throws DaoGenerateException
	 *             方法中的Annotation配置错误，或者写法不符合要求时抛出DataAccessInterceptor
	 */
	protected abstract DataAccessExecutor createDataAccessExecutor(Method method) throws DaoGenerateException;

	/**
	 * 检测类中的DAO方法
	 * 
	 * @param clazz
	 * @param interceptor
	 * @throws Exception 
	 */
	void detectDataAccessMethod(Class<?> clazz, final DataAccessInterceptor interceptor) throws Exception {
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {

			@Override
			public void doWith(Method method) throws Exception {
					DataAccessExecutor executor = createDataAccessExecutor(method);
					interceptor.addDataAccessExecutor(method, executor);
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
	}
}
