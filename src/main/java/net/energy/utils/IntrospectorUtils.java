package net.energy.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * PropertyDescriptor处理器，用于Bean的解析处理
 * 
 * @author wuqh
 *
 */
class IntrospectorUtils {
	private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTOR_CACHE = new WeakHashMap<Class<?>, Map<String, PropertyDescriptor>>();

	/**
	 * 获取指定类型所有的PropertyDescriptor
	 * 
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 */
	public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws IntrospectionException {
		Map<String, PropertyDescriptor> propertyDescriptors = PROPERTY_DESCRIPTOR_CACHE.get(clazz);

		if (propertyDescriptors == null) {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			propertyDescriptors = new HashMap<String, PropertyDescriptor>();
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				String property = descriptor.getName();
				propertyDescriptors.put(property, descriptor);
			}
		}

		return propertyDescriptors;
	}
}
