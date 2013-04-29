package net.energy.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.energy.definition.BatchDefinition;
import net.energy.exception.DaoGenerateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 反射及相关操作工具类
 * 
 * @author wuqh
 * 
 */
public class ReflectionUtils {
	static final Log LOGGER = LogFactory.getLog(ReflectionUtils.class);

	/**
	 * 查找指定类中的所有方法（包括父类和接口中的方法），并使用方法回调接口对这些方法进行处理。
	 * <p>
	 * 方法被回调前会通过{@link MethodFilter}进行判断，是否需要被回调
	 * 
	 * @param clazz
	 * @param mc
	 * @param mf
	 */
	public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) throws Exception {

		// Keep backing up the inheritance hierarchy.
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (mf != null && !mf.matches(method)) {
				continue;
			}
			try {
				mc.doWith(method);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("非法访问方法'" + method.getName() + "'：" + ex);
			}
		}
		if (clazz.getSuperclass() != null) {
			doWithMethods(clazz.getSuperclass(), mc, mf);
		} else if (clazz.isInterface()) {
			for (Class<?> superIfc : clazz.getInterfaces()) {
				doWithMethods(superIfc, mc, mf);
			}
		}
	}

	/**
	 * 方法回调接口
	 */
	public interface MethodCallback {

		/**
		 * 回调方法
		 * 
		 * @param method
		 */
		void doWith(Method method) throws Exception;
	}

	/**
	 * 方法过滤器，用于判断哪些方法会被方法回调接口执行
	 */
	public interface MethodFilter {

		/**
		 * 判断给定方法是否符合规则（不符合规则的将被过滤）
		 * 
		 * @param method
		 */
		boolean matches(Method method);
	}

	/**
	 * 预设的MethodFilter实现类，用于匹配方法中所有的非桥接方法和所有非<code>java.lang.Object</code>申明的方法
	 */
	public static MethodFilter USER_DECLARED_METHODS = new MethodFilter() {

		public boolean matches(Method method) {
			return (!method.isBridge() && method.getDeclaringClass() != Object.class);
		}
	};

	/**
	 * 通过一组getter方法，提取args中指定位置对象中相应属性的值。
	 * 
	 * @param getterMethods
	 * @param parameterIndexes
	 * @param args
	 * @param parameterNames
	 * @return
	 */
	public static Object[] fetchVlaues(Method[] getterMethods, Integer[] parameterIndexes, Object[] args,
			List<String> parameterNames) {
		Object[] values = new Object[getterMethods.length];
		for (int i = 0; i < getterMethods.length; i++) {
			Method method = getterMethods[i];
			Integer index = parameterIndexes[i];

			Object value = fetchVlaue(method, index, args, parameterNames);
			values[i] = value;
		}
		return values;
	}

	/**
	 * 通过getter方法提取对象中相应属性的值，其实就对args中指定index的对象的getter方法的一次反射调用，或者map的一次get操作。
	 * 
	 * @param getterMethod
	 *            getter方法
	 * @param index
	 *            args中的index
	 * @param args
	 *            方法调用的实际参数
	 * @param parameterName
	 *            传入参数名称，这个在对象为Map时使用
	 * @return 提取到的值
	 */
	@SuppressWarnings("rawtypes")
	public static Object fetchVlaue(Method getterMethod, Integer index, Object[] args, String paramName) {
		if (index == null || index == -1) {
			return null;
		}

		Object arg = args[index];

		if (arg == null) {
			return null;
		}

		if (getterMethod == null) {
			Class<?> clazz = arg.getClass();
			// 如果是枚举类型需要转换为String，因为大多数据库都不识别这个类型。
			if (clazz.isEnum()) {
				return arg.toString();
			}
			// 如果Map，执行"."操作就是执行get操作
			if (EnergyClassUtils.isTypeMap(clazz)) {
				Map map = (Map) arg;
				int pos = paramName.indexOf('.');
				String prop = paramName.substring(pos + 1);
				Object result = map.get(prop);
				if (result.getClass().isEnum()) {
					return result.toString();
				}
				return result;
			}

			return arg;
		} else {
			try {
				Object value = getterMethod.invoke(arg, new Object[0]);
				if (value.getClass().isEnum()) {
					return value.toString();
				} else {
					return value;
				}
			} catch (Throwable e) {
				LOGGER.info("方法调用失败", e);
				return null;
			}
		}
	}

	/**
	 * 通过getter方法提取对象中相应属性的值，其实就对args中指定index的对象的getter方法的一次反射调用，或者map的一次get操作。
	 * 
	 * @param getterMethod
	 *            getter方法
	 * @param index
	 *            args中的index
	 * @param args
	 *            方法调用的实际参数
	 * @param parameterNames
	 *            传入参数名称，这个在对象为Map时使用
	 * @return 提取到的值
	 */
	public static Object fetchVlaue(Method getterMethod, Integer index, Object[] args, List<String> parameterNames) {
		if (index == null || index == -1) {
			return null;
		}

		String paramName = parameterNames.get(index);

		return fetchVlaue(getterMethod, index, args, paramName);
	}

	/**
	 * 解析出bean的getter方法,和parameterNames中个参数对应的args中的索引值
	 * 由于parameterNames包含"."，所以需要indexes记录"."之前的@Param对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@CacheDelete(key="album-d-:albumId-:ownerId")</code>
	 * <code>@VerUpdate(vkey="ALBUM-:owner.id-:album.id-v")</code>
	 * public void updatePhoto(@Param("owner") User owner, @Param("album") Album album);</code>
	 * 将传入：parameterNames=["album.id","owner.id"]，paramIndexes={"album"=1,"owner"=0},paramTypes=[User.class,Album.class]
	 * 生成：[[User.getId,Album.getId],[1,0]]
	 * </pre>
	 * 
	 * 
	 * @param parameterNames
	 * @param paramIndexes
	 * @param paramTypes
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Object[] getGettersAndIndexes(List<String> parameterNames, Map<String, Integer> paramIndexes,
			Class<?>[] paramTypes) throws DaoGenerateException {
		int length = parameterNames.size();
		Method[] getters = new Method[length];
		Integer[] parameterIndexes = new Integer[length];
		for (int i = 0; i < length; i++) {
			String paramName = parameterNames.get(i);
			int pos = paramName.indexOf('.');
			if (pos != -1) {
				String actualName = paramName.substring(0, pos);

				int index = getParameIndex(paramIndexes, actualName);
				parameterIndexes[i] = index;

				String prop = paramName.substring(pos + 1);
				Method getter = findGetterByPropertyName(paramTypes[index], prop);
				getter.setAccessible(true); // no check to upgrade performance
				getters[i] = getter;
			} else { // don't need getters
				getters[i] = null;
				Integer index = getParameIndex(paramIndexes, paramName);
				parameterIndexes[i] = index;
			}
		}
		return new Object[] { getters, parameterIndexes };
	}

	/**
	 * 查找Bean中指定属性的getter方法。如果找不到将抛出DaoGenerateException
	 * 
	 * @param clazz
	 *            Bean的类型
	 * @param prop
	 *            属性名称
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Method findGetterByPropertyName(Class<?> clazz, String prop) throws DaoGenerateException {
		if (EnergyClassUtils.isTypeMap(clazz)) {
			return null;
		}
		String name = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
		String getter = "get" + name;
		String is = "is" + name;
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			Class<?> returnType = method.getReturnType();
			if (method.getParameterTypes().length == 0 && !returnType.equals(void.class)) {
				if (getter.equals(method.getName())) {
					return method;
				}
				if (is.equals(method.getName())
						&& (returnType.equals(boolean.class) || returnType.equals(Boolean.class))) {
					return method;
				}
			}
		}
		throw new DaoGenerateException("无法获取[" + clazz.getName() + "]中[" + prop + "]属性的getter方法");
	}

	/**
	 * 解析出bean的getter方法,和parameterNames中个参数对应的args中的索引值
	 * 由于parameterNames包含"."，所以需要indexes记录"."之前的@Param，@BatchParam对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 将传入：parameterNames=["owner.id","album.id","photo.file"]，paramIndexes={""owner"=0,album"=1},batchParamIndexes={"photo"=2},paramTypes=[User.class,Album.class,Photo.class]
	 * 生成：[[User.getId,Album.getId,Photo.getFile],[0,1,2]]
	 * </pre>
	 * 
	 * 
	 * @param parameterNames
	 * @param paramIndexes
	 * @param paramTypes
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Object[] getGettersAndIndexes(List<String> parameterNames, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes, Class<?>[] paramTypes) throws DaoGenerateException {
		int length = parameterNames.size();
		Method[] getters = new Method[length];
		Integer[] parameterIndexes = new Integer[length];
		for (int i = 0; i < length; i++) {
			String paramName = parameterNames.get(i);
			int pos = paramName.indexOf('.');
			if (pos != -1) {
				String actualName = paramName.substring(0, pos);

				Class<?> componentType = null;
				Integer index = null;

				if (!paramIndexes.containsKey(actualName) && !batchParamIndexes.containsKey(actualName)) {
					throw new DaoGenerateException("方法参数中必须包含@Param(\"" + actualName + "\")注解或者@BatchParam(\""
							+ actualName + "\")注解或者参数名为\"" + actualName + "\"的参数");
				} else if (paramIndexes.containsKey(actualName)) {
					index = paramIndexes.get(actualName);
					componentType = paramTypes[index];
				} else {
					index = batchParamIndexes.get(actualName);

					Class<?> paramType = paramTypes[index];
					if (!paramType.isArray()) {
						throw new DaoGenerateException("@BatchParam(\"" + paramName + "\")只能用于数组类型的参数上");
					}
					componentType = paramTypes[index].getComponentType();
				}

				parameterIndexes[i] = index;
				String prop = paramName.substring(pos + 1);
				Method getter = findGetterByPropertyName(componentType, prop);
				getter.setAccessible(true); // no check to upgrade performance
				getters[i] = getter;

			} else { // don't need getters
				getters[i] = null;
				if (!paramIndexes.containsKey(paramName) && !batchParamIndexes.containsKey(paramName)) {
					throw new DaoGenerateException("方法参数中必须包含@Param(\"" + paramName + "\")注解或者@BatchParam(\""
							+ paramName + "\")注解或者参数名为\"" + paramName + "\"的参数");
				} else if (paramIndexes.containsKey(paramName)) {
					Integer index = paramIndexes.get(paramName);
					parameterIndexes[i] = index;
				} else {
					Integer index = batchParamIndexes.get(paramName);
					parameterIndexes[i] = index;
				}
			}
		}
		return new Object[] { getters, parameterIndexes };
	}

	/**
	 * 提取array[index]的值
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	public static Object fetchArrayValue(Object array, int index) {
		if (array == null) {
			return null;
		}

		Class<?> clazz = array.getClass();
		if (!clazz.isArray()) {
			throw new IllegalArgumentException("array参数必须为数组");
		}
		Class<?> componentType = clazz.getComponentType();
		if (!componentType.isPrimitive()) {
			return ((Object[]) array)[index];
		}

		return fetchPrimitiveArrayValue(array, index, componentType);
	}

	/**
	 * 提取基本类型数组array[index]的值
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	static Object fetchPrimitiveArrayValue(Object array, int index, Class<?> componentType) {
		if (long.class.equals(componentType)) {
			return ((long[]) array)[index];
		}
		if (int.class.equals(componentType)) {
			return ((int[]) array)[index];
		}
		if (boolean.class.equals(componentType)) {
			return ((boolean[]) array)[index];
		}
		if (double.class.equals(componentType)) {
			return ((double[]) array)[index];
		}
		if (char.class.equals(componentType)) {
			return ((char[]) array)[index];
		}
		if (float.class.equals(componentType)) {
			return ((float[]) array)[index];
		}
		if (byte.class.equals(componentType)) {
			return ((byte[]) array)[index];
		}
		if (short.class.equals(componentType)) {
			return ((short[]) array)[index];
		}
		return null;
	}

	/**
	 * 在paramIndexes获取查找key为paramName的值。如果不包含paramName，则会抛出DaoGenerateException
	 * 如传入：paramIndexes={"album"=1,"owner"=0},paramName="owner"，将返回0
	 * 
	 * @param paramIndexes
	 * @param paramName
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Integer getParameIndex(Map<String, Integer> paramIndexes, String paramName)
			throws DaoGenerateException {
		if (!paramIndexes.containsKey(paramName)) {
			throw new DaoGenerateException("方法参数中必须包含@Param(\"" + paramName + "\")注解或者参数名为\"" + paramName + "\"的参数");
		}
		Integer index = paramIndexes.get(paramName);
		return index;
	}

	/**
	 * 获取批量SQL/Shell中需要使用的参数的List
	 * 
	 * @param args
	 * @param batchDefinition
	 * @return
	 */
	public static List<Object[]> generateBatchQueryArguments(Object[] args, BatchDefinition batchDefinition) {
		// 获取批量SQL/Shell的批数，即@BatchParam对应的几个数组的最小length
		int batchSize = -1;
		Integer[] batchParamIndexes = batchDefinition.getBatchParamIndexes();
		for (int i = 0; i < batchParamIndexes.length; i++) {
			int index = batchParamIndexes[i];
			if (batchSize == -1) {
				batchSize = EnergyArrayUtils.getLength(args[index]);
			} else {
				batchSize = Math.min(batchSize, EnergyArrayUtils.getLength(args[index]));
			}
		}

		Method[] getterMethods = batchDefinition.getGetterMethods();
		Integer[] parameterIndexes = batchDefinition.getParameterIndexes();
		List<Object[]> paramArrays = new ArrayList<Object[]>(batchSize);
		List<String> parameterNames = batchDefinition.getParsedExpression().getParameterNames();

		// 每次克隆一组调用参数，然后用批量参数中这个批次中的批量参数值替换这个批量参数
		for (int i = 0; i < batchSize; i++) {
			Object[] cloneArgs = EnergyArrayUtils.clone(args);

			for (int j = 0; j < batchParamIndexes.length; j++) {
				int index = batchParamIndexes[j];
				cloneArgs[index] = fetchArrayValue(cloneArgs[index], i);
			}

			Object[] paramArray = fetchVlaues(getterMethods, parameterIndexes, cloneArgs, parameterNames);
			paramArrays.add(paramArray);
		}

		return paramArrays;
	}

	public static ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * 设置系统参数名解析器，默认为{@link LocalVariableTableParameterNameDiscoverer}
	 * 
	 * @param parameterNameDiscoverer
	 */
	public static void setParameterNameDiscoverer(LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer) {
		PARAMETER_NAME_DISCOVERER = parameterNameDiscoverer;
	}

}
