package net.energy.utils;

import java.util.Collection;

/**
 * 断言判断类
 * 
 * @author wuqh
 *
 */
public class Assert {
	/**
	 * 断言对象必须为null，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 */
	public static void isNull(Object object, String message) {
		if (object != null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言对象不能为null，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * 断言集合必须包含元素，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 */
	public static void notEmpty(Collection<?> collection, String message) {
		if(collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}
}
