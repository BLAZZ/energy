package net.energy.utils;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;

/**
 * 类型判断工具类
 * 
 * @author wuqh
 *
 */
public class TypeUtils {
	/**
	 * 判断类型是否为{@link Page}的父类
	 * @param type
	 * @return
	 */
	public static boolean isTypePage(Class<?> type) {
		return (type != null && ClassUtils.isAssignable(type, Page.class));
	}

	/**
	 * 判断类型是否为数组
	 * @param type
	 * @return
	 */
	public static boolean isTypeArray(Class<?> type) {
		return (type != null && type.isArray());
	}

	/**
	 * 判断类型是否为{@link Collection}的实现类
	 * @param type
	 * @return
	 */
	public static boolean isTypeCollection(Class<?> type) {
		return (type != null && ClassUtils.isAssignable(type, Collection.class));
	}

	/**
	 * 判断类型是否为{@link List}的实现类
	 * @param type
	 * @return
	 */
	public static boolean isTypeList(Class<?> type) {
		return (type != null && ClassUtils.isAssignable(type, List.class));
	}

	/**
	 * 判断类型是否为基础类型
	 * @param type
	 * @return
	 */
	public static boolean isTypePrimitive(Class<?> type) {
		return (type != null && type.isPrimitive());
	}

	/**
	 * 判断类型是否为void（返回值）
	 * @param type
	 * @return
	 */
	public static boolean isTypeVoid(Class<?> type) {
		return (type != null && void.class.equals(type));
	}

	/**
	 * 判断类型是否为{@link Number}的父类
	 * @param type
	 * @return
	 */
	public static boolean isTypeNumber(Class<?> type) {
		return (type != null && ClassUtils.isAssignable(type, Number.class));
	}

	/**
	 * 判断类型是否为{@link Map}的实现类
	 * @param type
	 * @return
	 */
	public static boolean isTypeMap(Class<?> type) {
		return (type != null && ClassUtils.isAssignable(type, Map.class));
	}
	
	/**
	 * 判断类型是否为可进行String类操作的类型：
	 * CharSequence的实现类 (包括 StringBuffer、 StringBuilder等)，StringWriter父类。
	 * @param type
	 * @return
	 */
	public static boolean isTypeString(Class<?> type) {
		// Consider any CharSequence (including StringBuffer and StringBuilder) as a String.
		return (CharSequence.class.isAssignableFrom(type) || StringWriter.class.isAssignableFrom(type));
	}

	/**
	 * 判断是否为<code>java.util.Date</code>类型或者数据库的日期类型
	 */
	public static boolean isTypeDate(Class<?> type) {
		return (java.util.Date.class.isAssignableFrom(type) && !(java.sql.Date.class.isAssignableFrom(type)
				|| java.sql.Time.class.isAssignableFrom(type) || java.sql.Timestamp.class.isAssignableFrom(type)));
	}
}
