package net.energy.utils;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;

/**
 * 类、类型处理工具类
 * 
 * @author wuqh
 * 
 */
public class EnergyClassUtils extends ClassUtils {
	private static final String CLASS_FILE_SUFFIX = ".class";
	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

	/**
	 * 判断类型是否为{@link Page}的父类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypePage(Class<?> type) {
		return (type != null && isAssignable(type, Page.class));
	}

	/**
	 * 判断类型是否为数组
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeArray(Class<?> type) {
		return (type != null && type.isArray());
	}

	/**
	 * 判断类型是否为{@link Collection}的实现类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeCollection(Class<?> type) {
		return (type != null && isAssignable(type, Collection.class));
	}

	/**
	 * 判断类型是否为{@link List}的实现类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeList(Class<?> type) {
		return (type != null && isAssignable(type, List.class));
	}

	/**
	 * 判断类型是否为基础类型
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypePrimitive(Class<?> type) {
		return (type != null && type.isPrimitive());
	}

	/**
	 * 判断类型是否为void（返回值）
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeVoid(Class<?> type) {
		return (type != null && void.class.equals(type));
	}

	/**
	 * 判断类型是否为{@link Number}的父类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeNumber(Class<?> type) {
		return (type != null && isAssignable(type, Number.class));
	}

	/**
	 * 判断类型是否为{@link Map}的实现类
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeMap(Class<?> type) {
		return (type != null && isAssignable(type, Map.class));
	}

	/**
	 * 判断类型是否为可进行String类操作的类型： CharSequence的实现类 (包括 StringBuffer、
	 * StringBuilder等)，StringWriter父类。
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTypeString(Class<?> type) {
		// Consider any CharSequence (including StringBuffer and StringBuilder)
		// as a String.
		return (CharSequence.class.isAssignableFrom(type) || StringWriter.class.isAssignableFrom(type));
	}

	/**
	 * 判断是否为<code>java.util.Date</code>类型或者数据库的日期类型
	 */
	public static boolean isTypeDate(Class<?> type) {
		return (java.util.Date.class.isAssignableFrom(type) && !(java.sql.Date.class.isAssignableFrom(type)
				|| java.sql.Time.class.isAssignableFrom(type) || java.sql.Timestamp.class.isAssignableFrom(type)));
	}

	/**
	 * 通过class获取对应的.class文件名。比如java.lang.String类会返回"String.class"
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getClassFileName(Class<?> clazz) {
		Assert.notNull(clazz, "Class can not be null");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
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

	/**
	 * 获取类加载器
	 * 
	 * @return
	 */
	public static ClassLoader getClassLoader() {
		ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = contextCL == null ? EnergyClassUtils.class.getClassLoader() : contextCL;
		return loader;
	}

}
