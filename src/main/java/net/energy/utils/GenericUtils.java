package net.energy.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型操作工具类
 * 
 * @author wuqh
 *
 */
public final class GenericUtils {
	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

	private GenericUtils() {

	}

	/**
	 * 从Generic 类型信息获取传入的实际类信息。例：Map&lt;String,Object&gt;=>[String,Object]
	 * 
	 * @param genericType
	 *            - Generic 类型信息
	 * @return 实际类信息
	 */
	public static Class<?>[] getActualClass(Type genericType) {

		if (genericType instanceof ParameterizedType) {

			Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
			Class<?>[] actualClasses = new Class<?>[actualTypes.length];

			for (int i = 0; i < actualTypes.length; i++) {
				Type actualType = actualTypes[i];
				if (actualType instanceof Class<?>) {
					actualClasses[i] = (Class<?>) actualType;
				} else if (actualType instanceof GenericArrayType) {
					Type componentType = ((GenericArrayType) actualType).getGenericComponentType();
					actualClasses[i] = Array.newInstance((Class<?>) componentType, 0).getClass();
				} else if (actualType instanceof ParameterizedType) {
					actualClasses[i] = (Class<?>) ((ParameterizedType) actualType).getRawType();
				}
			}

			return actualClasses;
		}

		return EMPTY_CLASSES;
	}

	/**
	 * 获取一个类的第一个泛型类型,例:List&lt;String&gt;=>String
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?> getGenericType(Class<?> clazz) {
		Type genericType = clazz.getGenericInterfaces()[0];
		Class<?>[] classes = getActualClass(genericType);
		if (classes.length == 0) {
			return null;
		} else {
			return classes[0];
		}
	}
	
}
