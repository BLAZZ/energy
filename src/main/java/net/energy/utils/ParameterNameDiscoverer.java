package net.energy.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 获取Java方法参数名称接口
 *
 */
public interface ParameterNameDiscoverer {
	
	/**
	 * 返回指定方法的参数名称
	 * @param method
	 * @return 
	 */
	String[] getParameterNames(Method method);
	
	/**
	 * 返回指定构造方法的参数名称
	 * @param constructor
	 * @return
	 */
	String[] getParameterNames(Constructor<?> constructor);

}