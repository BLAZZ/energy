package net.energy.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.energy.exception.DaoGenerateException;

public abstract class BeanAutoDetect {
	protected final Map<String, Method> writeMethods = new HashMap<String, Method>();
	private final Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();
	protected final Class<?> clazz;

	protected BeanAutoDetect(Class<?> clazz) throws DaoGenerateException {
		this.clazz = clazz;

		Map<String, PropertyDescriptor> descriptors;
		try {
			descriptors = IntrospectorUtils.getPropertyDescriptors(clazz);
		} catch (Exception e) {
			throw new DaoGenerateException("解析类[" + clazz + "]失败：", e);
		}

		for (Entry<String, PropertyDescriptor> entry : descriptors.entrySet()) {
			String property = entry.getKey();
			PropertyDescriptor descriptor = entry.getValue();
			Method writeMethod = descriptor.getWriteMethod();
			Class<?> type = descriptor.getPropertyType();
			if (writeMethod != null) {
				if (type == null || !isAllowAutoMapType(type)) {
					throw new DaoGenerateException("[" + clazz + "]中包含不可识别的属性类型[" + type + "]");
				}
				writeMethod.setAccessible(true);
				writeMethods.put(property, writeMethod);
				propertyTypes.put(property, type);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object convert(Object oldObject, String property) {
		Object result = oldObject;
		Class<?> type = propertyTypes.get(property);
		if (type.isEnum()) {
			result = Enum.valueOf((Class<Enum>) type, (String) oldObject);
		}

		if (type.equals(BigInteger.class)) {
			result = new BigInteger((oldObject).toString());
		}

		if (type.equals(BigDecimal.class) || type.equals(Number.class)) {
			result = new BigDecimal((oldObject).toString());
		}

		return result;
	}

	/**
	 * 判断类型是否为可自动类型转换的类型
	 * 
	 * @param clazz
	 * @return
	 */
	protected abstract boolean isAllowAutoMapType(Class<?> clazz);
}
