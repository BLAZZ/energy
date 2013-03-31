package net.energy.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DataAccessExecutor的默认调用实现
 * 
 * @author wuqh
 * 
 */
public class MethodExecutor implements DataAccessExecutor {
	private final Method method;

	public MethodExecutor(Method method) {
		this.method = method;
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		try {
			return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

}
