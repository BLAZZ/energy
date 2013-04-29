package net.energy.factory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.executor.DataAccessExecutor;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数据访问操作的拦截器
 * 
 * @author wuqh
 * @see MethodInterceptor
 */
public class DataAccessInterceptor implements MethodInterceptor {
	private static final Log LOGGER = LogFactory.getLog(DataAccessInterceptor.class);
	private static final int CACHE_INIT_SIZE = 256;

	private final Map<Method, DataAccessExecutor> EXECUTOR_CACHE = new HashMap<Method, DataAccessExecutor>(
			CACHE_INIT_SIZE);

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		DataAccessExecutor executor = EXECUTOR_CACHE.get(method);
		if (executor != null) {
			return executor.execute(obj, args);
		} else {
			LOGGER.debug("方法[" + method + "]无对应的DataAccessExecutor实现,采用默认实现执行");
			return proxy.invoke(obj, args);
		}
	}

	public void addDataAccessExecutor(Method method, DataAccessExecutor executor) {
		EXECUTOR_CACHE.put(method, executor);
	}

}
